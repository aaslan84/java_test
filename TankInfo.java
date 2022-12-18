import javax.swing.*;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import ptolemy.util.StringUtilities;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowListener;
import java.awt.image.*;
// import java.awt.Dimension;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

public class TankInfo implements ActionListener{
  int tankID;
  String tankType;
  JFrame frame = new JFrame("Tank Config/Statistic");
  JLabel sensorValueLabel = new JLabel();
  JTextArea configTArea = new JTextArea();
  JScrollPane configSPane = new JScrollPane();
  JTable configJTable = new JTable();
  String[] cfgHeader = new String[] { "No", "Id", "Type", "Name", "PicPath", "Properties", "RecPeriod" };
  JComboBox<String> tankTypeCBox = new JComboBox<>();
  List<Path> imagePath;
  BufferedImage img = null;
  JLabel tankPicLabel;
  JPanel tankCalibPanel1, tankCalibPanel2;
  JTextArea calibTArea = new JTextArea();
  JScrollPane calibSPanel = new JScrollPane();
  JTable calibJTable = new JTable();
  String[] calibHeader = new String[] { "No", "Id", "Type", "Height", "Volume" };
  JButton calibAddRowBtn = new JButton();
  JButton calibAddRowBeforeBtn = new JButton();
  JButton calibAddRowAfterBtn = new JButton();
  JButton calibRemoveRowBtn = new JButton();
  JButton calibApplyBtn = new JButton();
  JTextArea eventTArea = new JTextArea();
  JTextPane eventTPane = new JTextPane();
  JScrollPane eventSPanel = new JScrollPane();
  JTable eventJTable = new JTable();
  JPanel leftPanelSub1, leftPanelSub2;
  String configFilePath = "D:/EASY_SENSE/EasySenseGUI/V2/data/files/TankProb.csv";
  String calibFilePath = "D:/EASY_SENSE/EasySenseGUI/V2/data/files/TankCalib.csv";
  String eventFilePath = "D:/EASY_SENSE/EasySenseGUI/V2/data/files/TankEvent.csv";
  String dataFilePath = "D:/EASY_SENSE/EasySenseGUI/V2/data/files/TankDat.csv";
  CSVPlot csvPlot = null;
  CalibGraph calibGraph = null;

  JPanel rightPanel = new JPanel();
  JLabel progLabel = new JLabel();
  JProgressBar progressBar = new JProgressBar();
  static int calibMarkStyle = 3; // TODO: more than 1

  boolean mousePressed = false;
  boolean cellClear = false;
  List<Pair> interpolationPts;

  double sensorValue, convertedValue, tankVolume;

  TankInfo(int tankID) {
    this.tankID = tankID;
    convertedValue = 0;
    /* Sensor Value Label */
    JPanel sensorValuePanel = new JPanel();
    sensorValuePanel.setLayout(new BoxLayout(sensorValuePanel, BoxLayout.LINE_AXIS));
    JLabel sensorLabel = new JLabel("Sensor Value:");
    sensorValuePanel.add(sensorLabel);
    sensorValuePanel.add(Box.createRigidArea(new Dimension(5, 0)));
    sensorValueLabel = new JLabel("0.0");
    sensorValueLabel.setMaximumSize(new Dimension(90, sensorValueLabel.getPreferredSize().height));
    sensorValueLabel.setOpaque(true);
    // sensorValueLabel.setBackground(new Color(150, 200, 0));
    sensorValuePanel.add(sensorValueLabel);
    sensorValuePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

    /* Tank Configuration Label */
    JLabel configLabel = new JLabel("Configuration");
    configLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

    /* Config Panel */
    configSPane = new JScrollPane();

    configJTable = getTable(configFilePath, tankID, tankType, cfgHeader, false, false);
    configJTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

    configSPane.setViewportView(configJTable);
    configSPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    configSPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
    configSPane.setPreferredSize(new Dimension(350, configSPane.getPreferredSize().height));
    configSPane.setMaximumSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));
    configSPane.setAlignmentX(Component.LEFT_ALIGNMENT);
    /****** */

    /**** Tank Calibration Box Layout ****/
    tankCalibPanel1 = new JPanel();
    tankCalibPanel1.setLayout(new BoxLayout(tankCalibPanel1, BoxLayout.X_AXIS));
    // tankCalibPanel1.setBorder(BorderFactory.createLineBorder(new Color(0)));

    /* Tank Label */
    JPanel tankTypePanel = new JPanel();
    // tankTypePanel.setLayout(new GridLayout(1, 2, 0, 0));
    tankTypePanel.setLayout(new BoxLayout(tankTypePanel, BoxLayout.X_AXIS));
    JLabel tankLabel = new JLabel("Type:");
    // tankLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    tankTypePanel.add(tankLabel);
    tankTypePanel.add(Box.createRigidArea(new Dimension(10, 0)));
    tankTypeCBox = new JComboBox<String>();
    tankTypeCBox.setOpaque(true);
    tankTypeCBox.setBackground(Color.WHITE);
    tankTypeCBox.setPreferredSize(new Dimension(150, tankTypeCBox.getPreferredSize().height));
    tankTypeCBox.setActionCommand("tankTypeCBoxAct");
    tankTypeCBox.addActionListener(this);
    
    imagePath = new ArrayList<Path>();
    for (int ii = 0; ii < configJTable.getRowCount(); ii++) {
      imagePath.add(Paths.get(configJTable.getValueAt(ii, 4).toString()));
      String fileName = imagePath.get(ii).getFileName().toString();
      tankTypeCBox.addItem(fileName.substring(0, fileName.lastIndexOf('.')));
    }
    // tankTypeCBox.setAlignmentX(Component.LEFT_ALIGNMENT);

    tankTypePanel.add(tankTypeCBox);
    tankTypePanel.setMaximumSize(new Dimension(200, 30));
    tankTypePanel.setAlignmentX(Component.CENTER_ALIGNMENT);

    tankCalibPanel1.add(new Box.Filler(new Dimension(0, 0),
        new Dimension(0, 0),
        new Dimension(Short.MAX_VALUE, 0)));
    tankCalibPanel1.add(tankTypePanel);
    tankCalibPanel1.add(Box.createRigidArea(new Dimension(10, 0)));

    /* Tank Picture */
    
    try {
      // img = ImageIO.read(new
      // File("D:/EASY_SENSE/EasySenseGUI/V2/data/images/tankMavi.png"));
      try {
        img = ImageIO.read(new File(imagePath.get(tankTypeCBox.getSelectedIndex()).toString()));
      } catch (IOException e1) {
        // TODO Auto-generated catch block
        e1.printStackTrace();
        img = ImageIO.read(new File("D:/EASY_SENSE/EasySenseGUI/V2/data/images/no_img.png"));
      }
      tankPicLabel = new JLabel();
      tankPicLabel.setOpaque(true);
      tankPicLabel.setBackground(new Color(0, 0, 0));
      tankPicLabel.setPreferredSize(new Dimension(60, 75));
      tankPicLabel.setMaximumSize(new Dimension(60, 75));
      ImageIcon icon = new ImageIcon(img.getScaledInstance(tankPicLabel.getPreferredSize().width,
          tankPicLabel.getPreferredSize().height, Image.SCALE_SMOOTH));
      tankPicLabel.setIcon(icon);
      // tankPicLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
      tankCalibPanel1.add(tankPicLabel);
      // tankCalibPanel1.setAlignmentX(Component.CENTER_ALIGNMENT);
    } catch (IOException e) {
      e.printStackTrace();
    }
    tankCalibPanel1.add(new Box.Filler(new Dimension(0, 0),
        new Dimension(0, 0),
        new Dimension(Short.MAX_VALUE, 0)));

    tankCalibPanel2 = new JPanel();
    tankCalibPanel2.setLayout(new BoxLayout(tankCalibPanel2, BoxLayout.Y_AXIS));
    tankCalibPanel2.setBorder(BorderFactory.createTitledBorder("Calibration"));
    // tankCalibPanel2.setLayout(new BorderLayout());
    tankType = tankTypeCBox.getSelectedItem().toString();
    calibJTable = getTable(calibFilePath, tankID, tankType, calibHeader, true, true);
    
    TableColumnModel model = calibJTable.getColumnModel();
    for (int ii = 0; ii < calibJTable.getColumnCount(); ii++) {
      TableColumn tc = model.getColumn(ii);
      tc.setCellEditor(new SelectingEditor(new JTextField()));
    }
    
    calibSPanel = new JScrollPane();
    calibSPanel.setViewportView(calibJTable);
    calibSPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    // calibSPanel.setMinimumSize(new Dimension(300, 200));
    calibSPanel.setPreferredSize(new Dimension(300, 100));
    calibSPanel.setMaximumSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));
    // calibSPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    tankCalibPanel2.add(calibSPanel);

    JPanel rowBtnPanel = new JPanel();
    rowBtnPanel.setLayout(new BoxLayout(rowBtnPanel, BoxLayout.X_AXIS));
    calibAddRowBtn.setText("Add");
    calibAddRowBtn.setMargin(new Insets(2, 2, 2, 2));
    calibAddRowBtn.setPreferredSize(new Dimension(calibAddRowBeforeBtn.getPreferredSize().width, 20));
    calibAddRowBtn.setActionCommand("calibAddRowAct");
    calibAddRowBtn.addActionListener(this);
    calibAddRowBeforeBtn.setText("Add Before");
    calibAddRowBeforeBtn.setMargin(new Insets(2, 2, 2, 2));
    calibAddRowBeforeBtn.setPreferredSize(new Dimension(calibAddRowBeforeBtn.getPreferredSize().width, 20));
    calibAddRowBeforeBtn.setActionCommand("calibAddRowBeforeAct");
    calibAddRowBeforeBtn.addActionListener(this);
    calibAddRowAfterBtn.setText("Add After");
    calibAddRowAfterBtn.setMargin(new Insets(2, 2, 2, 2));
    calibAddRowAfterBtn.setPreferredSize(new Dimension(calibAddRowAfterBtn.getPreferredSize().width, 20));
    calibAddRowAfterBtn.setActionCommand("calibAddRowAfterAct");
    calibAddRowAfterBtn.addActionListener(this);
    calibRemoveRowBtn.setText("Remove");
    calibRemoveRowBtn.setMargin(new Insets(2, 2, 2, 2));
    calibRemoveRowBtn.setPreferredSize(new Dimension(calibRemoveRowBtn.getPreferredSize().width, 20));
    calibRemoveRowBtn.addActionListener(this);
    calibRemoveRowBtn.setActionCommand("calibRemoveRowAct");
    calibApplyBtn.setText("Apply");
    calibApplyBtn.setMargin(new Insets(2, 2, 2, 2));
    calibApplyBtn.setPreferredSize(new Dimension(calibApplyBtn.getPreferredSize().width, 20));
    calibApplyBtn.setActionCommand("calibApplyAct");
    calibApplyBtn.addActionListener(this);

    rowBtnPanel.add(calibAddRowBtn);
    rowBtnPanel.add(calibAddRowBeforeBtn);
    rowBtnPanel.add(calibAddRowAfterBtn);
    rowBtnPanel.add(Box.createRigidArea(new Dimension(5, 0)));
    rowBtnPanel.add(calibRemoveRowBtn);
    rowBtnPanel.add(new Box.Filler(new Dimension(0, 0),
        new Dimension(0, 0),
        new Dimension(Short.MAX_VALUE, 0)));
    rowBtnPanel.add(calibApplyBtn);
    tankCalibPanel2.add(rowBtnPanel);

    calibGraph = new CalibGraph(tankID, tankType, calibFilePath);
    calibGraph.setMarksStyle("dots", tankID);
    calibGraph.setMarksStyle("bigdots", calibMarkStyle);
    calibGraph.setColor(true);
    calibGraph.setXLabel("sensor value [mm]");
    calibGraph.setYLabel("tank level [lt]");
    calibGraph.fillPlot();
    tankCalibPanel2.add(calibGraph);

    interpolationPts = getCalibParams(tankID, tankType);
    tankVolume = getTankVol();
    /****************************************************************** */
    /* Tank Configuration Label */
    JLabel eventLabel = new JLabel("Event List");
    eventLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

    /* Event List TextArea */
    eventTArea = new JTextArea();
    // eventTArea.setEditable(false);
    // eventTArea.setFont(new Font("Calibri", Font.PLAIN, 12));
    // eventTArea.setLineWrap(true);
    // eventTArea.setWrapStyleWord(true);

    eventTPane = new JTextPane();
    eventTPane.setEditable(false);

    eventSPanel = new JScrollPane();
    // eventSPanel.setViewportView(eventTArea);
    eventSPanel.setViewportView(eventTPane);
    eventSPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    eventSPanel.setPreferredSize(new Dimension(300, 300));
    eventSPanel.setMaximumSize(new Dimension(Short.MAX_VALUE, 300));
    eventSPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

    /* History Label */
    JLabel historyLabel = new JLabel("History");
    historyLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

    // History plot
    try {
      // Run this in the Swing Event Thread.
      Runnable runPlot = new Runnable() {
        @Override
        public void run() {
          try {
            csvPlot = new CSVPlot(dataFilePath, tankID);

            csvPlot.addProgress(percent -> {
              // System.out.println("row: " + percent);
              if (percent % 100 == 0) {
                progLabel.setForeground(new Color(250, 50, 100));
                progLabel.setText("Graphic is Loading (Data Read: " + percent + ")");
              }
            });

            csvPlot.addPlotterLoad(state -> {
              // System.out.println("state: " + state);
              if (!state) {

              } else {
                rightPanel.remove(progLabel);
                rightPanel.remove(progressBar);
              }
            });

            frame.addWindowListener(new WindowListener() {
              @Override
              public void windowClosing(WindowEvent event) {
                csvPlot.stop();
                StringUtilities.exit(0);
                csvPlot.closeFile(csvPlot.csvPoint);
              }

              @Override
              public void windowOpened(WindowEvent e) {
                // TODO Auto-generated method stub

              }

              @Override
              public void windowClosed(WindowEvent e) {
                // TODO Auto-generated method stub
                frame.dispose();
              }

              @Override
              public void windowIconified(WindowEvent e) {
                // TODO Auto-generated method stub

              }

              @Override
              public void windowDeiconified(WindowEvent e) {
                // TODO Auto-generated method stub

              }

              @Override
              public void windowActivated(WindowEvent e) {
                // TODO Auto-generated method stub

              }

              @Override
              public void windowDeactivated(WindowEvent e) {
                // TODO Auto-generated method stub

              }
            });
            csvPlot.setPreferredSize(new Dimension(300, 400));
            csvPlot.setMaximumSize(new Dimension(Short.MAX_VALUE, 500));
            csvPlot.setAlignmentX(Component.LEFT_ALIGNMENT);
            csvPlot.setButtons(true);
            csvPlot.stop();

          } catch (Exception ex) {
            System.err.println(ex.toString());
            ex.printStackTrace();
          }
        }
      };

      try {
        SwingUtilities.invokeAndWait(runPlot);
      } catch (Exception ex) {
        System.err.println(ex.toString());
        ex.printStackTrace();
      }
    } catch (Exception e) {
      System.err.println(e.toString());
      e.printStackTrace();
    }

    /************** */

    /* LEFT PANEL SUB-1 */
    leftPanelSub1 = new JPanel();
    leftPanelSub1.setLayout(new BoxLayout(leftPanelSub1, BoxLayout.Y_AXIS));
    // leftPanelSub1.setMaximumSize(new Dimension(frame.getWidth() / 4,
    // frame.getHeight()));

    leftPanelSub1.add(sensorValuePanel);
    leftPanelSub1.add(configLabel);
    leftPanelSub1.add(configSPane);

    /* LEFT PANEL SUB-2 */
    leftPanelSub2 = new JPanel();
    leftPanelSub2.setLayout(new BoxLayout(leftPanelSub2, BoxLayout.Y_AXIS));
    // leftPanelSub2.setMaximumSize(new Dimension(frame.getWidth() / 4,
    // frame.getHeight()));

    leftPanelSub2.add(tankCalibPanel1);
    leftPanelSub2.add(tankCalibPanel2);

    /* LEFT PANEL - VERTICAL */
    JPanel leftPanel = new JPanel();
    leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.X_AXIS));
    leftPanel.setBorder(BorderFactory.createTitledBorder("Tank Selection"));
    leftPanel.setMaximumSize(new Dimension(frame.getWidth() / 2, Short.MAX_VALUE));

    leftPanel.add(leftPanelSub1);
    leftPanel.add(leftPanelSub2);

    /* RIGHT PANEL */
    rightPanel = new JPanel();
    rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
    rightPanel.setBorder(BorderFactory.createTitledBorder("Statistics"));

    rightPanel.add(eventLabel);
    rightPanel.add(eventSPanel);
    rightPanel.add(historyLabel);

    progLabel.setText("Graphic Loading (Data Read: " + 0 + ")");
    rightPanel.add(progLabel);

    // progressBar = new JProgressBar(0, 100);
    progressBar.setIndeterminate(true);
    progressBar.setStringPainted(false);
    progressBar.setString(null);
    rightPanel.add(progressBar);
    try {
      rightPanel.add(csvPlot);
    } catch (Exception e) {
      // TODO: handle exception
    }

    /* MAIN PANEL */
    JPanel mainPanel = new JPanel();
    // mainPanel.setLayout(new BorderLayout());
    mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.X_AXIS));
    mainPanel.setBorder(BorderFactory.createTitledBorder("Statistics"));

    mainPanel.add(leftPanel);
    mainPanel.add(rightPanel);

    frame = new JFrame();
    frame.setSize(1100, 600);
    frame.getContentPane().add(mainPanel);
    // frame.getContentPane().add(rightPanel, BorderLayout.EAST);
    frame.setVisible(true);
    // frame.pack();
    // frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
  }
  
  JTable getTable(String path, int id, String type, String[] header, boolean editable, boolean typeFilter)
  {
    JTable table = null;
    try {
      table = getTableFromCsv(path, id, type, header, editable, typeFilter);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();

      if (e.getMessage().equals("file is empty")) {
        StringBuilder builder = new StringBuilder();
        for (int ii = 0; ii < header.length; ii++) {
          builder.append(header[ii]);
          if (ii < header.length - 1)
            builder.append(",");
        }
        addToCSV(path, builder.toString());
        try {
          table = getTableFromCsv(path, id, type, header, editable, typeFilter);
        } catch (Exception ex) {
          // TODO Auto-generated catch block
          ex.printStackTrace();
        }
      } else if (e.getMessage().equals("invalid header")) {
        // rename invalid file and create new one
        File f = new File(path);
        File frename = new File(path + ".invalid");
        if (f.renameTo(frename)) {
          System.out.println("invalid file backup");
        } else {
          System.out.println("Failed to rename the file.");
        }
        StringBuilder builder = new StringBuilder();
        for (int ii = 0; ii < header.length; ii++) {
          builder.append(header[ii]);
          if (ii < header.length - 1)
            builder.append(",");
        }
        addToCSV(path, builder.toString());
        try {
          table = getTableFromCsv(path, id, type, header, true, typeFilter);
        } catch (Exception e1) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    }
    return table;
  }

  JTable getTableFromCsv(String path, int id, String type, String[] header, boolean editable, boolean typeFilter) throws Exception {
    JTable table = null;

    List<List<String>> csv = CSVPlot.readCSV(path, ",");
    // check format
    if (csv.size() == 0) {
      throw new IIOException("file is empty");
    } else if (!Arrays.equals(header, csv.get(0).toArray(new String[0]))) {
      throw new IIOException("invalid header");
    }

    // get table header
    Vector<String> line = new Vector<String>(csv.get(0));
    table = new JTable(new Vector(1), line);
    int idCol = 0, typeCol = 0;
    for(int ii = 0; ii < line.size(); ii++)
    {
      if(line.get(ii).equals("Id"))
        idCol = ii;
      if(line.get(ii).equals("Type"))
        typeCol = ii;
    }

    table.setCellSelectionEnabled(true);
    Font font = table.getFont();
    table.getTableHeader().setFont(new Font(font.getName(), font.getStyle(), 10));
    table.getTableHeader().setReorderingAllowed(false);
    table.setFont(new Font(font.getName(), font.getStyle(), 10));
    table.setEnabled(editable);
    DefaultTableModel model = (DefaultTableModel) table.getModel();

    for (int ii = 1; ii < csv.size(); ii++) {
      line = new Vector<String>(csv.get(ii));
      if (line.size() != header.length)
        break;
      if (Integer.parseInt(line.get(idCol)) == id && (line.get(typeCol).equals(type) || !typeFilter))
        model.addRow(line);
    }
    return table;
  }

  void reloadTable(JTable table, String path, int id, String type, String[] header, boolean typeFilter) throws IOException
  {
    List<List<String>> csv = CSVPlot.readCSV(path, ",");
    // check format
    if (csv.size() == 0) {
      throw new IIOException("file is empty");
    } else if (!Arrays.equals(header, csv.get(0).toArray(new String[0]))) {
      throw new IIOException("invalid header");
    }
    
    Vector<String> line = new Vector<String>(csv.get(0));
    DefaultTableModel model = (DefaultTableModel) table.getModel();
    JTable ntable = new JTable(new Vector(1), line); // we should use setcolumnmodel like func. of table obj
    DefaultTableColumnModel colModel = (DefaultTableColumnModel) ntable.getColumnModel();
    table.setColumnModel(colModel);
    // find id and type column index
    int idInd = 0, typeInd = 0;
    for(int ii = 0; ii < line.size(); ii++)
    {
      if(line.get(ii).equals("Id"))
        idInd = ii;
      if(line.get(ii).equals("Type"))
        typeInd = ii;
    }

    // clear table
    for (int ii = model.getRowCount(); 0 < ii; ii--)
      model.removeRow(0);
    // load table
    for (int ii = 1; ii < csv.size(); ii++) {
      line = new Vector<String>(csv.get(ii));
      if (line.size() != header.length)
        break;
      if (Integer.parseInt(line.get(idInd)) == id && (line.get(typeInd).equals(type) || !typeFilter))
        model.addRow(line);
    }
  }

  void addToCSV(String path, String msg) {
    try {
      File f = new File(path);

      if (f.createNewFile()) {
        // System.out.println("File created: " + f.getName());
      } else {
        // System.out.println("File already exists.");
      }

      FileWriter fw = new FileWriter(f, true);
      fw.append(msg);
      fw.close();

    } catch (IOException e) {
      System.out.println("File error" + e);
      e.printStackTrace();
    }
  }

  void listToCSV(String path, List<List<String>> obj)
  {
    try {
      new FileWriter(path, false).close();
      File f = new File(path);
      if (f.createNewFile()) {
        // System.out.println("File created: " + f.getName());
      } else {
        // System.out.println("File already exists.");
  
      }
  
      FileWriter fw = new FileWriter(f, true);
      for (List<String> row : obj) {
        for (int col = 0; col < row.size(); col++) {
          if(col < row.size() - 1)
            fw.append(row.get(col)).append(",");
          else
            fw.append(row.get(col));
        }
        fw.append("\n");
      }
      fw.close();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } // clear file content
  }

  void writeTableToCSV(String path, String[] header, JTable table) {
    try {
      new FileWriter(path, false).close(); // clear file content
      File f = new File(path);

      if (f.createNewFile()) {
        // System.out.println("File created: " + f.getName());
      } else {
        // System.out.println("File already exists.");

      }

      FileWriter fw = new FileWriter(f, true);
      StringBuilder builder = new StringBuilder();
      for (int ii = 0; ii < header.length; ii++) {
        builder.append(header[ii]);
        if (ii < header.length - 1)
          builder.append(",");
      }
      fw.append(builder + "\n");
      if (table != null) {
        for (int row = 0; row < table.getRowCount(); row++) {
          for (int col = 0; col < table.getColumnCount(); col++) {
            // os.print(table.getColumnName(col));
            // os.print(": ");

            if (table.getValueAt(row, col) == null)
              break;
            fw.append(table.getValueAt(row, col).toString());
            if (col < table.getColumnCount() - 1)
              fw.append(",");
          }
          fw.append("\n");
        }
      }
      fw.close();

    } catch (IOException e) {
      System.out.println("File error" + e);
      e.printStackTrace();
    }
  }

  void updateCalibTable(JTable table, int id, String type, List<List<String>> list) throws IIOException
  {
    // delete rows which associated with specified id and tank type
    int rowNum = list.size();
    
    int idCol = 0, typeCol = 0;
    for(int ii = 0; ii < list.get(0).size(); ii++)
    {
      if(list.get(0).get(ii).equals("Id"))
        idCol = ii;
      if(list.get(0).get(ii).equals("Type"))
        typeCol = ii;
    }
        
    if(idCol >= list.get(0).size() || typeCol >= list.get(0).size())
      throw new IIOException("Invalid format");

    for (int row = 1; row <list.size(); row++) {
      if(Integer.parseInt(list.get(row).get(idCol)) == id && 
              list.get(row).get(typeCol).toString().equals(type))
      {
        list.remove(row);
        row--;
      }
    }

    DefaultTableModel model = (DefaultTableModel) table.getModel();
    rowNum = model.getRowCount();
    int colNum = model.getColumnCount();
    for(int row = 0; row < rowNum; row++)
    {
      List<String> line = new ArrayList<String>();
      for(int col = 0; col < colNum; col++)
        line.add(col, model.getValueAt(row, col).toString());
      list.add(line);
    }
  }

  void configAddStr(String str, boolean nline) {
    configTArea.append(str);
    if (nline)
      configTArea.append("\n");

    // Keep scroll bar position at bottom if user wish
    int extent = configSPane.getVerticalScrollBar().getModel().getExtent();
    if (configSPane.getVerticalScrollBar().getValue()
        + extent > (configSPane.getVerticalScrollBar().getMaximum() - 100))
      configTArea.setCaretPosition(configTArea.getDocument().getLength());
  }

  void configClrStr() {
    configTArea.setText(null);
  }

  void calibAddStr(String str, boolean nline) {
    calibTArea.append(str);
    if (nline)
      calibTArea.append("\n");
    // JTextPane tPane = new JTextPane();

    // Keep scroll bar position at bottom if user wish
    int extent = calibSPanel.getVerticalScrollBar().getModel().getExtent();
    if (calibSPanel.getVerticalScrollBar().getValue()
        + extent > (calibSPanel.getVerticalScrollBar().getMaximum() - 100))
      calibTArea.setCaretPosition(calibTArea.getDocument().getLength());
  }

  void calibClrStr() {
    calibTArea.setText(null);
  }

  void eventAddStr(String str, Color c, boolean nline) {
    // eventTArea.append(str);
    // if(nline)
    // eventTArea.append("\n");

    appendToPane(eventTPane, str, c);
    if (nline)
      appendToPane(eventTPane, "\n", c);

    // Keep scroll bar position at bottom if user wish
    int extent = eventSPanel.getVerticalScrollBar().getModel().getExtent();
    if (eventSPanel.getVerticalScrollBar().getValue()
        + extent > (eventSPanel.getVerticalScrollBar().getMaximum() - 100))
      eventTPane.setCaretPosition(eventTPane.getDocument().getLength());
  }

  void eventClrStr() {
    eventTPane.setText(null);
  }

  private void appendToPane(JTextPane tp, String msg, Color c) {
    StyleContext sc = StyleContext.getDefaultStyleContext();
    AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, c);

    aset = sc.addAttribute(aset, StyleConstants.FontFamily, "Calibri");
    aset = sc.addAttribute(aset, StyleConstants.Alignment, StyleConstants.ALIGN_JUSTIFIED);

    StyledDocument doc = tp.getStyledDocument();
    try {
      doc.insertString(doc.getLength(), msg, aset);
    } catch (BadLocationException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    int len = tp.getDocument().getLength();
    tp.setCaretPosition(len);
    tp.setCharacterAttributes(aset, false);
    tp.replaceSelection(msg);
  }

  public void actionPerformed(ActionEvent e) {
    if ("tankTypeCBoxAct".equals(e.getActionCommand())) {
      try {
        if (tankPicLabel != null) {
          tankType = tankTypeCBox.getSelectedItem().toString();
          // reload image
          img = ImageIO.read(new File(imagePath.get(tankTypeCBox.getSelectedIndex()).toString()));
          ImageIcon icon = new ImageIcon(img.getScaledInstance(tankPicLabel.getPreferredSize().width,
              tankPicLabel.getPreferredSize().height, Image.SCALE_SMOOTH));
          tankPicLabel.setIcon(icon);
          // reload table
          reloadTable(calibJTable, calibFilePath, tankID, tankType, calibHeader, true);
          TableColumnModel model = calibJTable.getColumnModel();
          for (int ii = 0; ii < calibJTable.getColumnCount(); ii++) {
            TableColumn tc = model.getColumn(ii);
            tc.setCellEditor(new SelectingEditor(new JTextField()));
          }
          // reload plot
          loadCalibGraph(calibFilePath, tankID,  tankType);
        }
      } catch (IOException e1) {
        // TODO Auto-generated catch block
        e1.printStackTrace();
      }
    } else if ("calibAddRowAct".equals(e.getActionCommand())) {
      DefaultTableModel model = (DefaultTableModel) calibJTable.getModel();
      model.addRow(new Object[] { null });
      int rowNum;
      for (rowNum = 0; rowNum < calibJTable.getRowCount(); rowNum++)
        model.setValueAt(rowNum + 1, rowNum, 0);
      int selectedRow = calibJTable.getRowCount() - 1;
      model.setValueAt(tankID, selectedRow, 1);
      model.setValueAt(tankType, selectedRow, 2);
      model.setValueAt(sensorValue, selectedRow, 3);
    } else if ("calibAddRowBeforeAct".equals(e.getActionCommand())) {

      DefaultTableModel model = (DefaultTableModel) calibJTable.getModel();
      int[] selection = calibJTable.getSelectedRows();

      if (selection.length == 0)
        selection = new int[] { 0 };

      if (model.getRowCount() > 0) {
        model.insertRow(selection[0], new Object[] { null });
        calibJTable.setRowSelectionInterval(selection[0], selection[0]);
      } else {
        model.addRow(new Object[] { null });
        calibJTable.setRowSelectionInterval(0, 0);
      }
      int rowNum;
      for (rowNum = 0; rowNum < calibJTable.getRowCount(); rowNum++)
      {
        model.setValueAt(rowNum + 1, rowNum, 0);
      }
      int selectedRow = calibJTable.getSelectedRow();
      if(calibJTable.getRowCount() == 1)
        selectedRow = 1;
      model.setValueAt(tankID, selectedRow - 1, 1);
      model.setValueAt(tankType, selectedRow - 1, 2);
      model.setValueAt(sensorValue, selectedRow - 1, 3);
    } else if ("calibAddRowAfterAct".equals(e.getActionCommand())) {
      DefaultTableModel model = (DefaultTableModel) calibJTable.getModel();
      int[] selection = calibJTable.getSelectedRows();

      if (model.getRowCount() > 0 && selection.length != 0) {
        model.insertRow(selection[0] + 1, new Object[] { null });
        calibJTable.setRowSelectionInterval(selection[0], selection[0]);
      } else {
        model.addRow(new Object[] { null });
        // calibJTable.setRowSelectionInterval(model.getRowCount() - 1, model.getRowCount() - 1);
      }
      int rowNum;
      for (rowNum = 0; rowNum < calibJTable.getRowCount(); rowNum++)
      {
        model.setValueAt(rowNum + 1, rowNum, 0);
      }
      int selectedRow = calibJTable.getSelectedRow();
      if(selection.length == 0)
        selectedRow = calibJTable.getRowCount() - 1;
      else
        selectedRow++;
      model.setValueAt(tankID, selectedRow, 1);
      model.setValueAt(tankType, selectedRow, 2);
      model.setValueAt(sensorValue, selectedRow, 3);
    } else if ("calibRemoveRowAct".equals(e.getActionCommand())) {
      DefaultTableModel model = (DefaultTableModel) calibJTable.getModel();
      int[] selection = calibJTable.getSelectedRows();
      if (model.getRowCount() > 0) {
        if (selection.length == 0)
          selection = new int[] { 0 };
        if ((selection[0] == model.getRowCount() - 1) && model.getRowCount() > 1)
          calibJTable.setRowSelectionInterval(selection[selection.length - 1] - 1, selection[selection.length -1] - 1);
        else
          calibJTable.setRowSelectionInterval(selection[selection.length - 1], selection[selection.length - 1]);
        for (int ii = 0; ii < selection.length; ii++) {
          model.removeRow(selection[ii] - ii);
        }
      }
      for (int ii = 0; ii < calibJTable.getRowCount(); ii++)
        model.setValueAt(ii + 1, ii, 0);
    } else if ("calibApplyAct".equals(e.getActionCommand())) {
      try{
        List<List<String>> dataList = CSVPlot.readCSV(calibFilePath, ",");
        updateCalibTable(calibJTable, tankID, tankType, dataList);
        listToCSV(calibFilePath, dataList);
        loadCalibGraph(calibFilePath, tankID, tankType);
        interpolationPts = getCalibParams(tankID, tankType);
        tankVolume = getTankVol();
      }catch(Exception e1)
      {
        e1.printStackTrace();
      }
    } else {
      System.out.println(e.getActionCommand());
    }
  }

  public void loadCalibGraph(String file, int id, String type) {
    try {
      Runnable runPlot = new Runnable() {
        @Override
        public void run() {
          try {
            calibGraph.clear(id);
            calibGraph.plotReadCSV(new RandomAccessFile(file, "r"), id, type, ",");
            calibGraph.fillPlot();
          } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
        }
      };
      SwingUtilities.invokeLater(runPlot);
    } catch (Exception e2) {
      e2.printStackTrace();
    }
  }
  public class Pair
  {
    double x, y;
    Pair(double x, double y)
    {
      this.x = x;
      this.y = y;
    }
    public double getX()
    {
      return x;
    }
    public double getY()
    {
      return y;
    }

    public void setXY(double x, double y)
    {
      this.x = x;
      this.y = y;
    }
  } 
  List<Pair> getCalibParams(int id, String type)
  {
    List<Pair> temp = new ArrayList<Pair>();
    List<Pair> points = new ArrayList<Pair>();
    try {
      List<List<String>> data = CSVPlot.readCSV(calibFilePath, ",");
      int lineNum = 0; // for avoiding table header line
      for (List<String> col : data) {
        if(lineNum > 0 && Integer.parseInt(col.get(1)) == id && col.get(2).equals(type))
        temp.add(new Pair(Double.parseDouble(col.get(3)), Double.parseDouble(col.get(4))));
        lineNum++;
      }
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    while(0 < temp.size())
    {
      double minX = Double.MAX_VALUE;
      double y = 0;
      int minInd = 0;
      for(int ii = 0; ii < temp.size(); ii++)
      {
        if(temp.get(ii).getX() < minX)
        {
          minX = temp.get(ii).getX();
          y = temp.get(ii).getY();
          minInd = ii;
        }
      }
      points.add(new Pair(minX, y));
      temp.remove(minInd);
    }

    return points;
  }

  /*
   * y = (y0(x1 - x) + y1(x - x0))/(x1 - x0)
   */
  public double getInterpolationOut(List<Pair> params, double x)
  {
    double y = 0;

    for (int ii = 0; ii < params.size() - 1; ii++) {
      if(params.get(ii).getX() > x)
      {
        y = params.get(ii).getY();
        break;
      }
      else if(params.get(ii).getX() <= x && params.get(ii + 1).getX() >= x)
      {
        y = (params.get(ii).getY() * (params.get(ii + 1).getX() - x) + 
        params.get(ii + 1).getY() * (x - params.get(ii).getX())) / 
        (params.get(ii + 1).getX() - params.get(ii).getX());

        break;
      }
      else
      {
        y = params.get(ii + 1).getY();
      }
    }
    // y = params.get(params.size() - 1).getY();

    return y;
  }

  public double convertSensorValue(double sensorValue)
  {
    this.sensorValue = sensorValue;
    sensorValueLabel.setText(String.format("%.02f/c:%.02f", sensorValue, convertedValue));
    double y = getInterpolationOut(interpolationPts, sensorValue);
    calibGraph.clear(calibMarkStyle);
    calibGraph.addPoint(calibMarkStyle, sensorValue, y, true); // add zero start point
    convertedValue = y;
    // System.out.println(y);

    return y;
  }

  public double getTankVol()
  {
    double max = Double.MIN_VALUE;
    for (Pair pair : interpolationPts) {
      if(max < pair.y)
        max = pair.y;
    }
    return max;
  }

  public static void main(String args[]) {
    TankInfo tankInfo1 = new TankInfo(0);
    tankInfo1.frame.setResizable(true);
    tankInfo1.frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    tankInfo1.frame.pack();

    try {
      Thread.sleep(1000);
    } catch (InterruptedException e1) {
      e1.printStackTrace();
    }

    TankInfo tankInfo2 = new TankInfo(1);
    tankInfo2.frame.setResizable(true);
    tankInfo2.frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    tankInfo2.frame.pack();

    boolean dir = false;
    int ii = 0;
    while(true)
    {
      
      if(ii >= 100)
        dir = false;
      else if(ii <= 0)
        dir= true;

      if(dir)
        ii++;
      else
        ii--;

      tankInfo1.convertSensorValue((double)ii * 10);
      tankInfo2.convertSensorValue((double)ii * 10);
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }  
}