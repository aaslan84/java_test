import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import ptolemy.plot.PlotLive;
import ptolemy.util.StringUtilities;

public class CSVPlot extends PlotLive{
    CSVPoint csvPoint = null;
    boolean plotInit;
    CSVPlot(String path, int id) throws Exception {

        // Create the left plot by calling methods.
        // Note that most of these methods should be called in
        // the event thread, see the Plot.java class comment.
        // In this case, main() is invoking this constructor in
        // the event thread.
        clear(true);
        setSize(350, 300);
        addXYValueTip(true);
        setButtons(true);
        // setTitle("History Data");
        // setYRange(0, 200);
        // setXRange(0, 1000);
        setXLabel("time");
        setYLabel("value");
        // addYTick("-6", -6);
        // addYTick("-3", -3);
        // addYTick("0", 0);
        // addYTick("3", 3);
        // addYTick("6", 6);
        setMarksStyle("none");
        setImpulses(false);
        zoomOnlyX(true);
        setAutomaticRescale(true);
        

        //plotReadCSV("D:/EASY_SENSE/EasySenseGUI/V2/data/files/TankDat.csv", ",");
        RandomAccessFile file = new RandomAccessFile(path, "r");

        plotInit = false;
        Thread plotGraph =  new Thread(new Runnable() {
            @Override
            public void run() {
                stop();
                setVisible(false);
                // System.out.println(Thread.currentThread().getName());
                notifyPlotterLoad(false);
                csvPoint = plotReadCSV(file, id, ",");
                notifyPlotterLoad(true);
                setVisible(true);
                // Graphics g = getGraphics();
                // paintComponent(g);
                fillPlot();
                plotInit = true;
            }
        });
        plotGraph.setName("Child Thread");
        plotGraph.start();
    }

    public interface ProggresObserver {
        public void plotterProgress(int percent);
    }

    private final List<ProggresObserver> proggObservers = new ArrayList<>();

    private void notifyProgress(int percent){
        proggObservers.forEach(observer -> observer.plotterProgress(percent));
    }

    public void addProgress(ProggresObserver observer) {
        proggObservers.add(observer);
    }

    /* */
    public interface PlotLoadObserver {
        public void plotterLoad(boolean state);
    }

    private final List<PlotLoadObserver> plotterLoadObservers = new ArrayList<>();

    private void notifyPlotterLoad(boolean state){
        plotterLoadObservers.forEach(observer -> observer.plotterLoad(state));
    }

    public void addPlotterLoad(PlotLoadObserver observer) {
        plotterLoadObservers.add(observer);
    }

    class CSVPoint{
        RandomAccessFile file;
        int lineNum;
        List<Double> timeAxis = new ArrayList<Double>();
        
        CSVPoint(RandomAccessFile file, List<Double> time, int lineNum)
        {
            this.file = file;
            this.lineNum = lineNum;
            this.timeAxis = time;
        }
        Double[] getTimeArr()
        {
            Double[] timeArr = new Double[timeAxis.size()];
            timeAxis.toArray(timeArr);
            return timeArr;
        }
        void close()
        {
            try {
                file.close();
                lineNum = 0;
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }

    /**
     * Add points to the plot. This is called by the base class
     * run() method when the plot is live.
     */
    @Override
    public synchronized void addPoints() {
        if(plotInit)
            addPointCSV(csvPoint, ",");
        else
            stop();
        try {
            // Findbugs:
            // [M M SWL] Method calls Thread.sleep() with a lock held
            // [SWL_SLEEP_WITH_LOCK_HELD]
            // Here it is not a problem since we
            // actually want to block the other threads
            // if they want to paint to slow down the drawing.
            Thread.sleep(50);
        } catch (InterruptedException e) {
        }
    }

    @Override
    public void printXValues(double x)
    {
        if(plotInit && (int)_xAxisRel < csvPoint.timeAxis.size() && (long)(_xAxisRel * 1000) >= 0)
        {
            long time = (long)(csvPoint.getTimeArr()[(int)_xAxisRel] * 1000);
            Date date = new Date(time);
                        
            _xvalueLabel.setText(String.format("x: %s", 
                                new SimpleDateFormat("dd.MM.yyyy hh:mm:ss").format(date)));
        }
    }
    @Override
    public void printYValues(double y)
    {
        _yvalueLabel.setText(String.format("y: %.01f", _yAxisRel));
    }

    static List<List<String>> readCSV(String path, String delimiter) throws IOException {

        List<List<String>> elmLines = new ArrayList<List<String>>();

        String line = "";
        String[] elm = new String[20];
        int row = 0, col = 0;
        try {
            // parsing a CSV file into BufferedReader class constructor
            BufferedReader br = new BufferedReader(new FileReader(path));
            while ((line = br.readLine()) != null) // returns a Boolean value
            {
                elm = line.split(delimiter); // use comma as separator
                col = 0;
                List<String> elms = new ArrayList<String>();
                for (String e : elm) {
                    elms.add(col++, e);
                }
                elmLines.add(row++, elms);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return elmLines;
    }

    CSVPoint plotReadCSV(RandomAccessFile file, int id, String delimiter)
    {
        List<Double> time = new ArrayList<Double>();
        String line;
        String[] elm = new String[20];
        int row = 0;
        try {
			while ((line = file.readLine()) != null) {
                elm = line.split(delimiter); // use comma as separator
                if(Integer.parseInt(elm[0]) == id){
                    time.add(Double.parseDouble(elm[2]));
                    addPoint(Integer.parseInt(elm[0]), (double) row++, Double.parseDouble(elm[4]), true);
                }
                notifyProgress(row);
                Thread.yield();
			}

			// file.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
        
        return new CSVPoint(file, time, row);
    }

    void addPointCSV(CSVPoint csvPoint, String delimiter)
    {
        try {
			String line;
            String[] elm = new String[20];

			while((line = csvPoint.file.readLine()) != null) {
				elm = line.split(delimiter); // use comma as separator
                csvPoint.timeAxis.add(Double.parseDouble(elm[2]));
                addPoint(Integer.parseInt(elm[0]), (double) csvPoint.lineNum++, Double.parseDouble(elm[4]), true);
                // System.out.println(line);
			}

			// file.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    void closeFile(CSVPoint fp)
    {
        fp.close();
    }

    /** Zoom in or out to the specified rectangle.
     *  This method calls repaint().
     *  @param lowx The low end of the new X range.
     *  @param lowy The low end of the new Y range.
     *  @param highx The high end of the new X range.
     *  @param highy The high end of the new Y range.
     */
    @Override
    public synchronized void zoom(double lowx, double lowy, double highx,
            double highy) {
        setXRange(lowx, highx);
        setYRange(lowy, highy);
        repaint();
    }

    public static void main(String[] args) {
        // Run this in the Swing Event Thread.
        Runnable doActions = new Runnable() {
            @Override
            public void run() {
                try {
                    CSVPlot csvPlot = new CSVPlot("D:/EASY_SENSE/EasySenseGUI/V2/data/files/TankDat.csv", 1);
                    csvPlot.setButtons(true);
                    csvPlot.stop();
                    JFrame frame = new JFrame("PlotLiveDemo");
                    frame.addWindowListener(new WindowAdapter() {
                        @Override
                        public void windowClosing(WindowEvent event) {
                            csvPlot.stop();
                            StringUtilities.exit(0);
                            csvPlot.closeFile(csvPlot.csvPoint);
                        }
                    });
                    frame.getContentPane().add("Center", csvPlot);
                    frame.setVisible(true);
                    // frame.setSize(csvPlot.getSize());
                    frame.pack();

                } catch (Exception ex) {
                    System.err.println(ex.toString());
                    ex.printStackTrace();
                }
            }
        };

        try {
            SwingUtilities.invokeAndWait(doActions);
        } catch (Exception ex) {
            System.err.println(ex.toString());
            ex.printStackTrace();
        }
    }
}