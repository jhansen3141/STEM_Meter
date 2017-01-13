package smDataViewer.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import smDataViewer.model.Constants;

public class LogParser {
	private ArrayList<SensorLogEntry> sensorLogEntries;
	private boolean isValidLogFile = false;

	public LogParser(File logFile) {
		sensorLogEntries = new ArrayList<SensorLogEntry>();
		parseFile(logFile);

	}

	@SuppressWarnings("deprecation")
	private void parseFile(File logFile) {
		SensorLogEntry logEntry = null;
		Date startTime = new Date();
		byte sevenByteArray[] = new byte[10];
		byte twentyByteArray[] = new byte[20];
		byte threeByteArray[] = new byte[3];

		try {
			FileInputStream fileStream = new FileInputStream(logFile);
			int bytesRead = 0;
			//int byteOffset = 0;
			long fileLength = logFile.length();


			System.out.println("File length: " + fileLength);
			//System.out.println(String.format("0x%02X, 0x%02X, 0x%02X", byteArray[0+byteOffset], byteArray[1+byteOffset], byteArray[2+byteOffset]));
			while(bytesRead != -1) {

				bytesRead = fileStream.read(threeByteArray, 0, 3);
				// check to see if a valid time marker has been found
				if((threeByteArray[0] == Constants.TIME_MARKER_B0) &&
						(threeByteArray[1] == Constants.TIME_MARKER_B1) &&
						(threeByteArray[2] == Constants.TIME_MARKER_B2))
				{
					System.out.println("Time stamp found");
					bytesRead = fileStream.read(sevenByteArray,0,7);
					// one last check to see if end marker is correct
					if(sevenByteArray[6] == Constants.TIME_MARKER_END) {
						System.out.println("Time maker confirmed");
						// increase the byteOffset past the 10 bytes of date data
						// assign the start time to the found time stamp
						startTime.setMonth(sevenByteArray[0]);
						startTime.setDate(sevenByteArray[1]);
						startTime.setYear(2000+sevenByteArray[2]);
						startTime.setHours(sevenByteArray[3]);
						startTime.setMinutes(sevenByteArray[4]);
						startTime.setSeconds(sevenByteArray[5]);
						// if this is the first time stamp found
						if(logEntry == null) {
							// a valid time stamp was found so the log file
							// is probably valid
							isValidLogFile = true;
							// first time stamp found so create new log entry
							System.out.println("Creating new log entry");
							logEntry = new SensorLogEntry(startTime);
						}
						else {
							// if its not the first time stamp then add the previous
							// to the list
							sensorLogEntries.add(logEntry);
							// then create a new entry using the new time stamp
							logEntry = new SensorLogEntry(startTime);
						}
					}

				}
				else if(threeByteArray[0] == Constants.LOG_MARKER_B0 &&
						threeByteArray[1] == Constants.LOG_MARKER_B1 &&
						threeByteArray[2] == Constants.LOG_MARKER_B2)
				{
					//System.out.println("Data point found");
					bytesRead = fileStream.read(twentyByteArray,0,20);
					// add the data point to the log entry list of data points
					DataPoint dataPoint = rawToDataPoint(twentyByteArray);
					if(dataPoint != null) {
						logEntry.addDataPoint(dataPoint);
					}

				}
			}
			// add the last log entry before finishing parser
			sensorLogEntries.add(logEntry);
			System.out.println("Closing File Stream");
			fileStream.close();

		} catch (FileNotFoundException e) {
			isValidLogFile = false;
			System.out.println("Unable to open file stream");
			return;
		} catch (IOException e) {
			System.out.println("Unable to close file stream");
		}
	}

	public DataPoint rawToDataPoint(byte[] sensorData) {
		DataPoint dataPoint = null;
		Sensor sensor;
		// check to see which sensor the data is from
		switch((int)sensorData[0]) {
		case Constants.IMU_MPU6050:
			sensor = new IMU_MPU6050(sensorData);
			sensor.calcSensorData();
			dataPoint = new DataPoint(Constants.IMU_MPU6050,sensor.getSyncNumber(),sensor.getGraphData());
			break;
		case Constants.TEMP_MCP9808:
			sensor = new TEMP_MCP9808(sensorData);
			sensor.calcSensorData();
			dataPoint = new DataPoint(Constants.TEMP_MCP9808,sensor.getSyncNumber(),sensor.getGraphData());
			break;
		}
		return dataPoint;
	}

	public ArrayList<SensorLogEntry> getLogEntries() {
		return sensorLogEntries;
	}

	public boolean isValidLogFile() {
		return isValidLogFile;
	}
}
