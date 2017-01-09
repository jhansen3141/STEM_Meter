package smDataViewer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import smDataViewer.model.LogParser;
import smDataViewer.model.SensorLogEntry;

public class MainApp extends Application {

	private Stage primaryStage;
	private AnchorPane rootLayout;

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
	private ScrollPane sensorListPane;

	@FXML
	protected void initialize() {
		xAxis.setLabel("Time");
		yAxis.setLabel("Value");

		mainChart.setCreateSymbols(false);
		mainChart.setTitle("Sensor Data1");
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
		if(fileChooser != null) {
			File file = fileChooser.showOpenDialog(primaryStage);
			if(file != null) {
				LogParser logParser = new LogParser(file);
				if(logParser.isValidLogFile()) {
					ArrayList<SensorLogEntry> logEntries = logParser.getLogEntries();
				}
				else {
					// TODO handle invalid or corrupt file
				}
			}
		}

	}

	@FXML
	protected void convertCVSAction() {

	}

	@FXML
	protected void exitAction() {
		System.out.println("Exit");
		Platform.exit();
	}

    /**
     * Initializes the root layout.
     */
    public void initRootLayout() {
        try {
            // Load root layout from fxml file.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/RootLayout.fxml"));
            rootLayout = (AnchorPane) loader.load();
            // Show the scene containing the root layout.
            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);
            primaryStage.setMaximized(true);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the main stage.
     * @return
     */
    public Stage getPrimaryStage() {
        return primaryStage;
    }

	public static void main(String[] args) {
		launch(args);
	}
}
