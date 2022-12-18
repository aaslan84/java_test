import java.io.BufferedReader;
import java.io.File; // Import the File class
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException; // Import the IOException class to handle errors
import java.util.ArrayList;
import java.util.List;

public class fileio {
  static void writeCSV(String path, String msg) {
    try {
      File f = new File(path);

      if (f.createNewFile()) {
        System.out.println("File created: " + f.getName());
      } else {
        System.out.println("File already exists.");
      }

      FileWriter fw = new FileWriter(f, true);
      fw.append(msg);
      fw.close();

      // Scanner fr = new Scanner(f);
      // while(fr.hasNextLine())
      // {
      // System.out.println(fr.nextLine());
      // }
      // fr.close();
    } catch (IOException e) {
      System.out.println("File error" + e);
      e.printStackTrace();
    }
  }

  // static String[][] readCSV(String path, String delimiter, int elementNum) {
  // String[][] outStr = new String[1000][50];
  // int lineNum = 0;
  // try {
  // File f = new File(path);

  // if (f.createNewFile()) {
  // System.out.println("File created: " + f.getName());
  // } else {
  // System.out.println("File already exists.");
  // }

  // Scanner fr = new Scanner(f);
  // while (fr.hasNextLine()) {
  // System.out.println(fr.nextLine());
  // String[] objStr = fr.nextLine().split(delimiter);
  // if (objStr.length > elementNum) {
  // System.out.println("Line element num error " + objStr.length);
  // }
  // else
  // {
  // outStr[lineNum++] = objStr;
  // }
  // }
  // fr.close();

  // } catch (IOException e) {
  // System.out.println("File error" + e);
  // e.printStackTrace();
  // }

  // return outStr;
  // }
  static List<List<String>> readCSV(String path, String delimiter) {

    List<List<String>> elmLines = new ArrayList<List<String>>();
    List<String> elms = new ArrayList<String>();

    String line = "";
    // List<String> l = new ArrayList<String>();
    String[] elm = new String[20];
    int row = 0, col = 0;
    try {
      // parsing a CSV file into BufferedReader class constructor
      BufferedReader br = new BufferedReader(new FileReader(path));
      while ((line = br.readLine()) != null) // returns a Boolean value
      {
        elm = line.split(delimiter); // use comma as separator
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

  public static void main(String[] args) {
    writeCSV("csvFile.csv", String.format("a,b,%d,%f\n", 6, 7.890));
    writeCSV("csvFile.csv", String.format("a,b,%d,%f\n", 1, 6.890));
    writeCSV("csvFile.csv", String.format("a,b,%d,%f\n", 2, 2.890));
    List<List<String>> csvfile = readCSV("csvFile.csv", ",");

    for (int ii = 0; ii < csvfile.size(); ii++) {
      System.out.println(csvfile.get(ii));
    }
    System.out.println(csvfile);
  }
}