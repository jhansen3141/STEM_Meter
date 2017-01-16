package smDataViewer.model;

import java.util.ArrayList;

public abstract class Sensor {

	byte data[];

    private int syncNumber;
    private int sensorRate;
    private boolean SDLog;

    public Sensor(byte data[]) {
        this.data = data;
        //sensor rate is held is second byte
        sensorRate = (int)data[1];
        // the sync number (24 bits) is held in bytes 2-4
        syncNumber = (int)(((data[2]<<16) & 0xFF0000) | ((data[3]<<8) & 0xFF00) | (data[4] & 0xFF));
    }

    public void updateData(byte data[]) {
        this.data = data;
        sensorRate = (int)data[1];
        syncNumber = (int)(((data[2]<<16) & 0xFF0000) | ((data[3]<<8) & 0xFF00) | (data[4] & 0xFF));
    }

    public int getSyncNumber() {
        return syncNumber;
    }

    public int getSensorRate() {
        return sensorRate;
    }

    public boolean isSDLog() {
        return SDLog;
    }

    abstract String[] calcSensorData();

    abstract ArrayList<Float> getGraphData();


}
