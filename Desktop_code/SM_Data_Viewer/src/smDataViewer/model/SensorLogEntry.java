package smDataViewer.model;

import java.util.ArrayList;
import java.util.Date;

import javafx.scene.chart.XYChart;

public class SensorLogEntry {
	private ArrayList<DataPoint> dataPoints;
	private Date startTime;


	public SensorLogEntry(Date startTime) {
		this.startTime = startTime;
		dataPoints = new ArrayList<DataPoint>();
	}

	public void addDataPoint(DataPoint dataPoint) {
		dataPoints.add(dataPoint);
	}


	public ArrayList<DataPoint> getDataPoints() {
		return dataPoints;
	}

	public Date getStartTime() {
		return startTime;
	}

	@SuppressWarnings("deprecation")
	public String dateToString() {
		String month = Integer.toString(startTime.getMonth());
		String day = Integer.toString(startTime.getDate());
		String year = Integer.toString(startTime.getYear());
		
		String hour = String.format("%02d", startTime.getHours());
		String minute = String.format("%02d", startTime.getMinutes());
		String second = String.format("%02d", startTime.getSeconds());
	
		return month + "/" + day + "/" + year + "\n" 
			+ hour + ":" + minute + ":" + second;
	}
}
