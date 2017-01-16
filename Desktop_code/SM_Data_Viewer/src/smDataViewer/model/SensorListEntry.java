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
	private LineChart <Number,Number> mainChart;
	private Date date;
	private String rateString;
	private XYChart.Series<Number, Number> series;
	private String sensorStr;
	private int timeOffset = 0;

	public SensorListEntry(int sensorType, Date date,LineChart <Number,Number> mainChart) {
		this.mainChart = mainChart;
		this.sensorType = sensorType;
		this.date = date;
		series = new XYChart.Series<Number, Number>();
		checkBox = new CheckBox();
		checkBox.selectedProperty().addListener(checkChange);
	}

	ChangeListener checkChange = new ChangeListener<Boolean>() {
		@Override
		public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
			if (newValue) {
				series.setName(sensorStr);
				mainChart.getData().add(series);
			}
			else {
				// remove the series from the graph
				mainChart.getData().removeAll(series);
			}
		}
	};

	public String getRateString() {
		return rateString;
	}

	public String sensorTypeToString(int baseSensorType, int subSensorType) {
		switch(baseSensorType) {
		case Constants.IMU_MPU6050:
			sensorStr = "IMU";
			switch(subSensorType) {
				case Constants.IMU_ACCEL_X:
					sensorStr = "ACCEL: X";
					break;
				case Constants.IMU_ACCEL_Y:
					sensorStr = "ACCEL: Y";
					break;
				case Constants.IMU_ACCEL_Z:
					sensorStr = "ACCEL: Z";
					break;
				case Constants.IMU_GYRO_X:
					sensorStr = "ACCEL: X";
					break;
				case Constants.IMU_GYRO_Y:
					sensorStr = "GYRO: Y";
					break;
				case Constants.IMU_GYRO_Z:
					sensorStr = "GYRO: Z";
					break;
			}
			break;
		case Constants.TEMP_MCP9808:
			sensorStr = "TEMP";
			break;
		}

		return sensorStr;
	}

	public void addLogToSeries(SensorLogEntry logEntry, int valueIndex) {
		boolean rateStringSet = false;
		ArrayList<DataPoint> dataPointsList = logEntry.getDataPoints();
		for(int i=0; i<dataPointsList.size();i++) {
			DataPoint currentDataPoint = dataPointsList.get(i);
			if(currentDataPoint.getSensorType() == sensorType) {
				if(!rateStringSet) {
					setInitialValues(currentDataPoint,valueIndex);
					rateStringSet = true;
				}
				int timeWithOffset = currentDataPoint.getTime() - timeOffset;
				series.getData().add(new Data<Number, Number>(timeWithOffset,
						currentDataPoint.getSensorValues().get(valueIndex)));
			}
		}
	}

	private void setInitialValues(DataPoint dataPoint, int subSensorType) {
		rateString = rateToString(dataPoint.getRate());
		timeOffset = dataPoint.getTime();
		System.out.println("Time offset: " + timeOffset);
		// set the sensor type string
		sensorTypeToString(dataPoint.getSensorType(),subSensorType);
		// set the check box text
		checkBox.setText(sensorStr + " : " + rateString);
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

	public XYChart.Series<Number, Number> getSeries() {
		return series;
	}

	public CheckBox getCheckBox() {
		return checkBox;
	}



}
