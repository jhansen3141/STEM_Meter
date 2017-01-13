package smDataViewer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.FontUIResource;

import org.controlsfx.control.CheckListView;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import smDataViewer.model.Constants;
import smDataViewer.model.DataPoint;
import smDataViewer.model.LogParser;
import smDataViewer.model.SensorListEntry;
import smDataViewer.model.SensorLogEntry;

public class MainApp extends Application {

	private Stage primaryStage;
	private AnchorPane rootLayout;
	private ArrayList<SensorListEntry> sensorListEntries = new ArrayList<SensorListEntry>();

	@FXML
	private MenuItem loadFileItem;

	@FXML
	private MenuItem convertItem;

	@FXML
	private MenuItem exitItem;

	private NumberAxis xAxis = new NumberAxis(0,400,0.5);
	private NumberAxis yAxis = new NumberAxis(0,150,0.5);

	@FXML
	private LineChart <Number,Number>mainChart = new LineChart<Number,Number>(xAxis,yAxis);

	@FXML
	private ScrollPane sensorListScrollPane;

	@FXML
	private VBox sensorListVBox;

	@FXML
	protected void initialize() {
		xAxis.setLabel("Time");
		yAxis.setLabel("Value");

		mainChart.setCreateSymbols(false);
		mainChart.setTitle("Sensor Data");
		//mainChart.getData().add(series);
	}

	@Override
	public void start(Stage primaryStage) {
		this.primaryStage = primaryStage;
		this.primaryStage.setTitle("STEM-Meter Data Viewer");

		initRootLayout();
	}

	@FXML
	protected void loadFileAction() {
		FileChooser fileChooser = new FileChooser();
		FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("Binary Files (*.bin)", "*.bin");
		fileChooser.getExtensionFilters().add(filter);
		if(fileChooser != null) {
			File file = fileChooser.showOpenDialog(primaryStage);
			if(file != null) {
				// create a new log parser object with opened file
				LogParser logParser = new LogParser(file);
				// check to make sure file is valid (has date stamp))
				if(logParser.isValidLogFile()) {
					System.out.println("Valid Log File");
					// create a list of log entries
					ArrayList<SensorLogEntry> logEntries = logParser.getLogEntries();
					System.out.println("Num entries: " + logEntries.size());
					// for each log entry generate an entry into the list on left side of app
					for(int i=0;i<logEntries.size();i++) {
						generateListEntries(logEntries.get(i));
					}

				}
				else {
					// TODO handle invalid or corrupt file
				}
			}
		}
	}

	private void generateListEntries(SensorLogEntry logEntry) {
		System.out.println("Generating List Entires...");
		// get the start time for this entry
		Date startTime = logEntry.getStartTime();
		// get the list of data points for this entry
		ArrayList<DataPoint> dataPointsList =  logEntry.getDataPoints();
		System.out.println("DataPointList Size: " + dataPointsList.size());
		// go through all of the data points and separate them into different sensors
		for(int i=0; i<dataPointsList.size(); i++) {
			DataPoint currentDataPoint = dataPointsList.get(i);
			// if the list is empty then add the first entry
			if(sensorListEntries.size() == 0) {
				SensorListEntry firstEntry = new SensorListEntry(currentDataPoint.getSensorType(),startTime,mainChart);
				firstEntry.addLogToSeries(logEntry);
				sensorListEntries.add(firstEntry);
			}
			// if not empty list then only add if sensor doesn't already exists
			else {
				final int sensorListEntriesSize = sensorListEntries.size();
				for(int j=0; j<sensorListEntriesSize; j++) {
					final int dataPointType = currentDataPoint.getSensorType();
					// check to see if this type of sensor already exists in the list
					if(!(sensorExistsInList(dataPointType))) {
						SensorListEntry entry = new SensorListEntry(currentDataPoint.getSensorType(),startTime,mainChart);
						entry.addLogToSeries(logEntry);
						sensorListEntries.add(entry);
					}
				}
			}
		}
		// set the text with the date for this log entry
		Text dateText = new Text(logEntry.dateToString());
		dateText.setFont(Font.font("Verdana", FontWeight.BOLD, 20));
		// add the date text to the vbox on left side of app
		sensorListVBox.getChildren().add(dateText);

		// add a separator under the date
		Separator separator = new Separator();
		sensorListVBox.getChildren().add(separator);

		// add check boxes for each sensor type under this log entry
		final int numEntries = sensorListEntries.size();
		for(int i=0; i< numEntries; i++) {
			CheckBox cb = sensorListEntries.get(i).getCheckBox();
			cb.setPadding(new Insets(10,0,10,0));
			sensorListVBox.getChildren().add(cb);
		}
	}

	private boolean sensorExistsInList(final int sensorType) {
		boolean exists = false;
		for(int i=0; i< sensorListEntries.size(); i++) {
			int sensorListType = sensorListEntries.get(i).getSensorType();
			if(sensorListType == sensorType) {
				exists = true;
			}
		}
		return exists;
	}

	@FXML
	protected void convertCVSAction() {
		// TODO add function to convert data to CVS
	}

	@FXML
	protected void exitAction() {
		System.out.println("Exit");
		Platform.exit();
	}

    public void initRootLayout() {
        try {
            // Load root layout from fxml file.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/RootLayout.fxml"));
            rootLayout = (AnchorPane) loader.load();
            // Show the scene containing the root layout.
            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);
           // primaryStage.setMaximized(true);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

	public static void main(String[] args) {
		launch(args);
	}
}
