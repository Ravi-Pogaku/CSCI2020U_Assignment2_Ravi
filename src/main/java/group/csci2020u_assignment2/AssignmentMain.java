package group.csci2020u_assignment2;

import java.io.*;
import java.lang.reflect.Array;
import java.nio.file.*;
import java.util.*;
import javax.xml.parsers.*;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ScrollPane;
import javafx.stage.Stage;
import org.w3c.dom.*;


// CSCI2020U Assignment 2
// Ravichandra Pogaku, 100784105

public class AssignmentMain extends Application {

    // class variable for csv data
    public static List<List<String>> airlineData = new ArrayList<>();

    // read data from csv file
    public static List<List<String>> readCSV(File file) throws IOException {
        List<List<String>> lines = new ArrayList<>();
        try {
            // CSV in resources folder
            BufferedReader br = new BufferedReader(new FileReader(file));

            String line = "";
            while ((line = br.readLine()) != null) {
                String[] temp = line.split(",");
                lines.add(Arrays.asList(temp));
            }
            br.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
        return lines;
    }

    // write new column to csv file
    public static void writeCSV(File file, List<String> newData) throws IOException {
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));

            String line = "";
            String newLines = "";
            int i = 0;
            while ((line = br.readLine()) != null) {
                newLines += line + "," + newData.get(i) + "\n";
                i++;
            }
            br.close();

            BufferedWriter bw = new BufferedWriter(
                    new FileWriter(file, false)
            );
            bw.write(newLines);
            bw.flush();
            bw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // turn csv data to xml
    public static void csvToXML(List<List<String>> data, Path outputPath) throws ParserConfigurationException {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.newDocument();

            Element rootElement = doc.createElement("Root");
            doc.appendChild(rootElement);

            List<List<String>> copy = new ArrayList<>(data);
            copy.remove(0); // I assume the column names should not be in the xml?

            for (List<String> info: copy) {
                // Each row of data
                Element row = doc.createElement("Row");
                rootElement.appendChild(row);

                // Each element in row
                Element airline = doc.createElement("airline");
                airline.appendChild(doc.createTextNode(info.get(0)));
                row.appendChild(airline);

                Element avail_seats = doc.createElement("avail_seat_km_per_week");
                avail_seats.appendChild(doc.createTextNode(info.get(1)));
                row.appendChild(avail_seats);

                Element incidents_85_99 = doc.createElement("incidents_85_99");
                incidents_85_99.appendChild(doc.createTextNode(info.get(2)));
                row.appendChild(incidents_85_99);

                Element fatal_accidents_85_99 = doc.createElement("fatal_accidents_85_99");
                fatal_accidents_85_99.appendChild(doc.createTextNode(info.get(3)));
                row.appendChild(fatal_accidents_85_99);

                Element fatalities_85_99 = doc.createElement("fatalities_85_99");
                fatalities_85_99.appendChild(doc.createTextNode(info.get(4)));
                row.appendChild(fatalities_85_99);

                Element incidents_00_14 = doc.createElement("incidents_00_14");
                incidents_00_14.appendChild(doc.createTextNode(info.get(5)));
                row.appendChild(incidents_00_14);

                Element fatal_accidents_00_14 = doc.createElement("fatal_accidents_00_14");
                fatal_accidents_00_14.appendChild(doc.createTextNode(info.get(6)));
                row.appendChild(fatal_accidents_00_14);

                Element fatalities_00_14 = doc.createElement("fatalities_00_14");
                fatalities_00_14.appendChild(doc.createTextNode(info.get(7)));
                row.appendChild(fatalities_00_14);

                Element total_incidents = doc.createElement("total_incidents");
                total_incidents.appendChild(doc.createTextNode(info.get(8)));
                row.appendChild(total_incidents);
            }

            TransformerFactory tranFac = TransformerFactory.newInstance();
            Transformer tran = tranFac.newTransformer();
            // formatting the xml file hierarchy
            tran.setOutputProperty(OutputKeys.INDENT, "yes");
            tran.setOutputProperty(OutputKeys.METHOD, "xml");
            tran.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(outputPath.toString()+"/converted_airline_safety.xml"));
            tran.transform(source, result);
        } catch (ParserConfigurationException | TransformerException e) {
            e.printStackTrace();
        }
    }

    // summarize csv data
    public static List<List<String>> summarize(List<List<String>> airlineData) {

        List<List<String>> data = new ArrayList<>(airlineData); // copy of airlineData

        List<String> columns = new ArrayList<>();
        // min, max, and average of every column except airline name
        for (int i = 1; i < data.get(0).size(); i++) {
            columns.add("min_" + data.get(0).get(i));
            columns.add("max_" + data.get(0).get(i));
            columns.add("avg_" + data.get(0).get(i));
        }

        // 2b and 2c
        columns.add("avg_all_incidents_85_99");
        columns.add("avg_all_incidents_00_14");

        String[] avgs = new String[data.get(0).size() - 1];
        String[] mins = data.get(1).toArray(new String[0]);
        String[] maxs = data.get(1).toArray(new String[0]);
        float calc = 0;

        for (int i = 1; i < data.get(0).size(); i++) {
            for (int j = 1; j < data.size(); j++) {
                calc += Float.parseFloat(data.get(j).get(i));
            }
            calc = calc / data.size();
            avgs[i-1] = String.valueOf(calc);
            calc = 0;
        }

        for (int i = 1; i < data.size(); i++) {
            for (int j = 1; j < data.get(0).size(); j++) {
                if (Long.parseLong(data.get(i).get(j)) < Long.parseLong(mins[j])) {
                    mins[j] = data.get(i).get(j);
                }

                if (Long.parseLong(data.get(i).get(j)) > Long.parseLong(maxs[j])) {
                    maxs[j] = data.get(i).get(j);
                }
            }
        }

        String[] calcs1 = new String[columns.size()];
        for (int i  = 1; i < maxs.length; i++) {
            calcs1[i*3-3] = mins[i];  // weird indexing because they aren't all the same size and to skip first row
            calcs1[i*3-2] = maxs[i];
            calcs1[i*3-1] = avgs[i-1];
        }

        // 2b and 2c mean average number of all incidents (incidents and fatal accidents) so I will just add
        // the avg_incidents and avg_fatal_accidents columns together for 2b and 2c
        // 2b: 5 and 8, 2c: 14 and 17
        Float twoB = Float.parseFloat(calcs1[5]) + Float.parseFloat(calcs1[8]);
        Float twoC = Float.parseFloat(calcs1[14]) + Float.parseFloat(calcs1[17]);

        calcs1[calcs1.length-2] = String.valueOf(twoB);
        calcs1[calcs1.length-1] = String.valueOf(twoC);

        List<String> calcs2 = Arrays.asList(calcs1);

        List<List<String>> summary = new ArrayList<>();
        summary.add(columns);
        summary.add(calcs2);

        return summary;
    }

    // turn summary data into xml
    public static void summaryXML(List<String> columns, List<String> summary, Path outputPath) throws ParserConfigurationException {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.newDocument();

            Element rootElement = doc.createElement("Summary");
            doc.appendChild(rootElement);

            // start at 1 to skip airline name column
            for (int i = 1; i < columns.size(); i++) {
                // Each row of data
                Element stat = doc.createElement("Stat");
                rootElement.appendChild(stat);

                // Each name of column, and then min, max, and average
                Element name1 = doc.createElement("Name");
                name1.appendChild(doc.createTextNode(columns.get(i)));
                stat.appendChild(name1);

                Element min = doc.createElement("Min");
                min.appendChild(doc.createTextNode(summary.get(i*3-3))); // indexing to iterate by 3s
                stat.appendChild(min);

                Element max = doc.createElement("Max");
                max.appendChild(doc.createTextNode(summary.get(i*3-2)));
                stat.appendChild(max);

                Element avg = doc.createElement("Avg");
                avg.appendChild(doc.createTextNode(summary.get(i*3-1)));
                stat.appendChild(avg);
            }

            // 2b
            Element stat1 = doc.createElement("Stat");
            rootElement.appendChild(stat1);

            Element name1 = doc.createElement("Name");
            name1.appendChild(doc.createTextNode("avg_all_85_99"));
            stat1.appendChild(name1);

            Element min1 = doc.createElement("Min");
            stat1.appendChild(min1);

            Element max1 = doc.createElement("Max");
            stat1.appendChild(max1);

            Element avg1 = doc.createElement("Avg");
            avg1.appendChild(doc.createTextNode(summary.get(summary.size()-2)));
            stat1.appendChild(avg1);

            // 2c
            Element stat2 = doc.createElement("Stat");
            rootElement.appendChild(stat2);

            Element name2 = doc.createElement("Name");
            name2.appendChild(doc.createTextNode("avg_all_00_14"));
            stat2.appendChild(name2);

            Element min2 = doc.createElement("Min");
            stat2.appendChild(min2);

            Element max2 = doc.createElement("Max");
            stat2.appendChild(max2);

            Element avg2 = doc.createElement("Avg");
            avg2.appendChild(doc.createTextNode(summary.get(summary.size()-1)));
            stat2.appendChild(avg2);

            // output xml file
            TransformerFactory tranFac = TransformerFactory.newInstance();
            Transformer tran = tranFac.newTransformer();
            // formatting the xml file hierarchy
            tran.setOutputProperty(OutputKeys.INDENT, "yes");
            tran.setOutputProperty(OutputKeys.METHOD, "xml");
            tran.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(outputPath.toString()+"/airline_summary_statistic.xml"));
            tran.transform(source, result);
        }
        catch (ParserConfigurationException | TransformerException e) {
            e.printStackTrace();
        }
    }

    // Graph javafx
    @Override
    public void start(Stage primaryStage) throws Exception {
        List<String> airlines = new ArrayList<>();
        List<Integer> fatal_accidents_85_99 = new ArrayList<>();
        List<Integer> fatal_accidents_00_14 = new ArrayList<>();

        List<List<String>> copy = new ArrayList<>(airlineData);
        copy.remove(0);

        for (List<String> airline: copy) {
            airlines.add(airline.get(0)); // airline names
            fatal_accidents_85_99.add(Integer.parseInt(airline.get(3)));
            fatal_accidents_00_14.add(Integer.parseInt(airline.get(6)));
        }

        primaryStage.setTitle("CSCI2020U - Assignment 2 - Ravichandra Pogaku 100784105");
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> bc = new BarChart<>(xAxis, yAxis);
        bc.setTitle("Fatal Incidents 1985 - 2014");
        xAxis.setLabel("Airlines");
        yAxis.setLabel("Number of Fatal Accidents");

        XYChart.Series FA_85_99 = new XYChart.Series();
        FA_85_99.setName("Fatal Accidents 1985 - 1999");
        XYChart.Series FA_00_14 = new XYChart.Series();
        FA_00_14.setName("Fatal Accidents 2000 - 2014");

        for (int i = 0; i < airlines.size(); i++) {
            FA_85_99.getData().add(new XYChart.Data(airlines.get(i), fatal_accidents_85_99.get(i)));
            FA_00_14.getData().add(new XYChart.Data(airlines.get(i), fatal_accidents_00_14.get(i)));
        }

        bc.getData().addAll(FA_85_99, FA_00_14);

        ScrollPane sp = new ScrollPane(); // too many airlines so I put it on a scroll pane
        bc.setPrefWidth(airlines.size()*50); // 25 pixels per bar seems good
        sp.setContent(bc);
        sp.setFitToHeight(true);

        Scene scene = new Scene(sp, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) throws IOException, ParserConfigurationException {
        Path resourcePath = Paths.get("src/main/resources/group/csci2020u_assignment2").toAbsolutePath();
        Path csvPath = Paths.get("src/main/resources/group/csci2020u_assignment2/airline_safety.csv");
        File csvFile = new File(String.valueOf(csvPath.toAbsolutePath()));

        airlineData = readCSV(csvFile);
        //System.out.println(airlineData.get(0)); // columns 2 and 5 are needed for part 1. 3,4,6,7 are for part 2

        // Calculate total number of incidents from 1985-2014 for each airline
        List<String> totalIncidents_85to14 = new ArrayList();
        totalIncidents_85to14.add("total_incidents");
        int num = 0;
        for (int i = 1; i < airlineData.size(); i++) {
            num = Integer.parseInt(airlineData.get(i).get(2)) + Integer.parseInt(airlineData.get(i).get(5));

            totalIncidents_85to14.add(
                    String.valueOf(num)
            );
        }

        // To prevent same column being added when program is run multiple times
        if (8 == airlineData.get(0).size()) {
            writeCSV(csvFile, totalIncidents_85to14);
            airlineData = readCSV(csvFile); // update with new column
        }

        csvToXML(airlineData, resourcePath);

        List<List<String>> summary = summarize(airlineData);

        /*
        for (int i = 0; i < summary.size(); i++) {
            System.out.println(summary.get(i));
        }
        */

        summaryXML(airlineData.get(0), summary.get(1), resourcePath);

        // Bar graph
        launch(args);
    }
}
