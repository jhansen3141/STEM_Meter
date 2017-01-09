package smDataViewer.model;

import java.io.File;
import java.util.ArrayList;

public class LogParser {
	private ArrayList<SensorLogEntry> sensorLogEntries;
	private boolean isValidLogFile = false;

	public LogParser(File logFile) {
		parseFile(logFile);
	}

	private void parseFile(File logFile) {

	}
	public ArrayList<SensorLogEntry> getLogEntries() {
		return sensorLogEntries;
	}

	public boolean isValidLogFile() {
		return isValidLogFile;
	}
}
