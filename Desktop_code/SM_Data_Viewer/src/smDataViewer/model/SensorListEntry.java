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
	//private XYChart.Series<Number,Number> series;
	private LineChart <Number,Number> mainChart;
	private Date date;
	private String rateString;
	private int numberOfSeries;
	private ArrayList<XYChart.Series<Number, Number>> seriesList;

	public SensorListEntry(int sensorType, Date date,LineChart <Number,Number> mainChart) {
		this.mainChart = mainChart;
		this.sensorType = sensorType;
		this.date = date;
		seriesList = new ArrayList<XYChart.Series<Number, Number>>();
		checkBox = new CheckBox();
		checkBox.selectedProperty().addListener(checkChange);
	}

	ChangeListener checkChange = new ChangeListener<Boolean>() {
		@Override
		public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
			if (newValue) {
				for(int i=0;i<numberOfSeries;i++) {
					mainChart.getData().add(seriesList.get(i));
				}
			}
			else {
				// remove the series from the graph
				for(int i=0;i<numberOfSeries;i++) {
					mainChart.getData().removeAll(seriesList.get(i));
				}
			}
		}
	};

	public String getRateString() {
		return rateString;
	}

	public String sensorTypeToString() {
		String sensorStr = null;
		switch(sensorType) {
		case Constants.IMU_MPU6050:
			sensorStr = "IMU";
			break;
		case Constants.TEMP_MCP9808:
			sensorStr = "TEMP";
			break;
		}

		return sensorStr;
	}

	public void addLogToSeries(SensorLogEntry logEntry) {
		boolean rateStringSet = false;
		System.out.println("Adding log to series. Type: " + sensorTypeToString());
		ArrayList<DataPoint> dataPointsList = logEntry.getDataPoints();
		for(int i=0; i<dataPointsList.size();i++) {
			DataPoint currentDataPoint = dataPointsList.get(i);
			if(currentDataPoint.getSensorType() == sensorType) {
				if(!rateStringSet) {
					setInitialValues(currentDataPoint);
					rateStringSet = true;
				}
				for(int k=0;k<numberOfSeries;k++) {
					seriesList.get(k).getData().add(
							new Data<Number, Number>(currentDataPoint.getStartTime(),currentDataPoint.getSensorValues().get(k)));
				}
			}
		}
	}

	private void setInitialValues(DataPoint dataPoint) {
		rateString = rateToString(dataPoint.getRate());
		checkBox.setText(sensorTypeToString() + " : " + rateString);
		numberOfSeries = dataPoint.sensorTypeToSeriesNumber();
		System.out.println("Number of series: " + numberOfSeries);
		for(int j=0;j<numberOfSeries;j++) {
			seriesList.add(new XYChart.Series<Number, Number>());
		}
	}

	public String rateToString(int rate) {
		String rateStr = "";
		switch(rate) {
		case Constants.RATE_OFF:
			rateStr = "OFF";
			break;
		case Constants.RATE_FIVE_HZ:
			rateStr = "5Hz";
			break;
		case Constants.RATE_TEN_HZ:
			rateStr = "10Hz";
			break;
		case Constants.RATE_ONE_HZ:
			rateStr = "1Hz";
			break;
		case Constants.RATE_ONE_MIN:
			rateStr = "1Min";
			break;
		case Constants.RATE_TEN_MIN:
			rateStr = "10Min";
			break;
		case Constants.RATE_THRITY_MIN:
			rateStr = "30Min";
			break;
		case Constants.RATE_ONE_HOUR:
			rateStr = "1Hr";
			break;
		}

		return rateStr;
	}

	private float getRateMultiplier(int rate) {
		float mult = 1;
		switch(rate) {
			case Constants.RATE_OFF:
				mult = 0;
				break;
			case Constants.RATE_FIVE_HZ:
				mult = (float) 0.20;
				break;
			case Constants.RATE_TEN_HZ:
				mult = (float) 0.10;
				break;
			case Constants.RATE_ONE_HZ:
				mult = 1;
				break;
			case Constants.RATE_ONE_MIN:
				mult = 60;
				break;
			case Constants.RATE_TEN_MIN:
				mult = 10 * 60;
				break;
			case Constants.RATE_THRITY_MIN:
				mult = 30 * 60;
				break;
			case Constants.RATE_ONE_HOUR:
				mult = 60 * 60;
				break;
		}

		return mult;
	}

	public int getSensorType() {
		return sensorType;
	}

	public ArrayList<XYChart.Series<Number, Number>> getSeries() {
		return seriesList;
	}

	public CheckBox getCheckBox() {
		return checkBox;
	}



}
