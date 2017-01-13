package smDataViewer.model;
import java.util.ArrayList;
import java.util.Date;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.control.CheckBox;

public class SensorListEntry {
	private int sensorType;
	private CheckBox checkBox;
	private XYChart.Series<Number,Number> series;
	private LineChart <Number,Number> mainChart;
	private Date date;

	public SensorListEntry(int sensorType, Date date,LineChart <Number,Number> mainChart) {
		this.mainChart = mainChart;
		this.sensorType = sensorType;
		this.date = date;
		series = new XYChart.Series<Number, Number>();
		checkBox = new CheckBox();
		checkBox.setText(sensorTypeToString());
		checkBox.selectedProperty().addListener(checkChange);
	}

	ChangeListener checkChange = new ChangeListener<Boolean>() {
		@Override
		public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
			if (newValue) {
				System.out.println("Check box checked: " + sensorTypeToString());
				mainChart.getData().add(series);
			}
			else {
				System.out.println("Check box unchecked " + sensorTypeToString());
				mainChart.getData().removeAll(series);
			}
		}
	};

	public String sensorTypeToString() {
		String sensorStr = null;
		switch(sensorType) {
		case Constants.IMU_MPU6050:
			sensorStr = "Accelerometer";
			break;
		case Constants.TEMP_MCP9808:
			sensorStr = "Temperature";
			break;
		}

		return sensorStr;
	}

	public void addLogToSeries(SensorLogEntry logEntry) {
		System.out.println("Adding log to series. Type: " + sensorTypeToString());
		ArrayList<DataPoint> dataPointsList = logEntry.getDataPoints();
		for(int i=0; i<dataPointsList.size();i++) {
			DataPoint currentDataPoint = dataPointsList.get(i);
			if(currentDataPoint.getSensorType() == sensorType) {
				series.getData().add(new Data<Number, Number>(currentDataPoint.getStartTime(),currentDataPoint.getSensorValues().get(0)));
			}
		}
	}

	public int getSensorType() {
		return sensorType;
	}

	public XYChart.Series<Number,Number> getSeries() {
		return series;
	}

	public CheckBox getCheckBox() {
		return checkBox;
	}



}
