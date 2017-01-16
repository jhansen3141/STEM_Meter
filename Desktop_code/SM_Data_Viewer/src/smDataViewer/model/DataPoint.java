package smDataViewer.model;

import java.util.ArrayList;

public class DataPoint {
	private int time;
	private ArrayList<Float> sensorValues;
	private int sensorType;
	private int sensorRate;

	public DataPoint(int sensorType, int time, int sensorRate, ArrayList<Float> sensorValues) {
		this.sensorRate = sensorRate;
		this.sensorType = sensorType;
		this.time = time;
		this.sensorValues = sensorValues;
	}

	public int getTime() {
		return time;
	}

	public int getSensorType() {
		return sensorType;
	}

	public ArrayList<Float> getSensorValues() {
		return sensorValues;
	}

	public int getRate() {
		return sensorRate;
	}

	// returns how many series each of the sensors has
	// Ex: 6-axis IMU has 6 graph series
	public int sensorTypeToSeriesNumber() {
		int seriesNumber = 1;
		switch(sensorType) {
		case Constants.IMU_MPU6050:
			seriesNumber = 3;
			break;
		case Constants.TEMP_MCP9808:
			seriesNumber = 1;
			break;
		}
		return seriesNumber;
	}
}
