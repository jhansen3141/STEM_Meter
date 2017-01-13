package smDataViewer.model;

import java.util.ArrayList;

public class DataPoint {
	private int time;
	private ArrayList<Float> sensorValues;
	private int sensorType;

	public DataPoint(int sensorType, int time, ArrayList<Float> sensorValues) {
		this.sensorType = sensorType;
		this.time = time;
		this.sensorValues = sensorValues;
	}

	public int getStartTime() {
		return time;
	}

	public int getSensorType() {
		return sensorType;
	}

	public ArrayList<Float> getSensorValues() {
		return sensorValues;
	}
}
