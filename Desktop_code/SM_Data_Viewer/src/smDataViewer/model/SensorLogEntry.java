package smDataViewer.model;

import java.util.ArrayList;
import java.util.Date;

public class SensorLogEntry {
	private String sensorName;
	private ArrayList<DataPoint> dataPoints;
	private Date startTime;

	public SensorLogEntry(String sensorName, int month, int day, int year, int hours, int minutes, int seconds) {
		this.sensorName = sensorName;
		startTime.setMonth(month);
		startTime.setDate(day);
		startTime.setYear(year+2000);
		startTime.setHours(hours);
		startTime.setMinutes(minutes);
		startTime.setSeconds(seconds);
	}

	public void addDataPoint(float time, float value) {
		DataPoint dataPoint = new DataPoint(time,value);
		dataPoints.add(dataPoint);
	}

	public String getSensorName() {
		return sensorName;
	}

	public ArrayList<DataPoint> getDataPoints() {
		return dataPoints;
	}

	public Date getStartTime() {
		return startTime;
	}
}
