
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

import ptolemy.plot.Plot;

class CalibGraph extends Plot{
    JButton applyButton = new JButton();
    CSVPoint csvPoint = null;

    CalibGraph(int id, String type, String file)
    {
        // Create the left plot by calling methods.
        // Note that most of these methods should be called in
        // the event thread, see the Plot.java class comment.
        // In this case, main() is invoking this constructor in
        // the event thread.
        clear(true);
        setSize(350, 200);
        addXYValueTip(true);
        setButtons(true);
        setXLabel("sensor value [mm]");
        setYLabel("tank level [Lt]");
        setMarksStyle("bigdots");
        setImpulses(false);
        zoomOnlyX(true);
        setAutomaticRescale(true);

        try {
            RandomAccessFile calibFile = new RandomAccessFile(file, "r");
            csvPoint = plotReadCSV(calibFile, id, type, ",");
        } catch (Exception e) {
            
        }
    }

    class CSVPoint{
        RandomAccessFile file;
        int lineNum;
        List<Double> xAxis = new ArrayList<Double>();
        
        CSVPoint(RandomAccessFile file, List<Double> time, int lineNum)
        {
            this.file = file;
            this.lineNum = lineNum;
            this.xAxis = time;
        }
        Double[] getTimeArr()
        {
            Double[] xAxisArr = new Double[xAxis.size()];
            xAxis.toArray(xAxisArr);
            return xAxisArr;
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
    
    CSVPoint plotReadCSV(RandomAccessFile file, int id, String type, String delimiter)
    {
        List<Double> xAxisList = new ArrayList<Double>();
        String line;
        String[] elm = new String[20];
        int row = 0;
        try {
			while ((line = file.readLine()) != null) {
                if(row > 0)
                {
                    elm = line.split(delimiter); // use comma as separator
                    // if(row == 1)
                    //     addPoint(Integer.parseInt(elm[1]), 0, 0, true); // add zero start point
                    if(Integer.parseInt(elm[1]) == id && elm[2].equals(type)){
                        xAxisList.add(Double.parseDouble(elm[1]));
                        addPoint(Integer.parseInt(elm[1]), (double) Double.parseDouble(elm[3]), Double.parseDouble(elm[4]), true);
                    }
                }
                row++;
			}

			// file.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
        
        return new CSVPoint(file, xAxisList, row);
    }

    public static void main(String args[])
    {
        String calibfile = "D:/EASY_SENSE/EasySenseGUI/V2/data/files/TankCalib.csv";
        JFrame frame = new JFrame("Interpolation");
        CalibGraph calibGraph = new CalibGraph(1, "0001", calibfile);
        frame.getContentPane().add("Center", calibGraph);
        frame.setVisible(true);
        // frame.setSize(csvPlot.getSize());
        frame.pack();

        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        double x = 0, y = 0;
        // while(true)
        // {
        //     calibGraph.setXPersistence(1);
        //     calibGraph.addPoint(1, x, x, true); // add zero start point
        //     x++;
        //     try {
        //         Thread.sleep(100);
        //     } catch (InterruptedException e) {
        //         // TODO Auto-generated catch block
        //         e.printStackTrace();
        //     }
        // }
    }
}