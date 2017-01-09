package smDataViewer.model;

public abstract class Sensor {

	byte data[];
    private int sensorNumber;
    private int syncNumber;
    private int sensorRate;
    private boolean SDLog;

    public Sensor(byte data[], int sensorNumber) {
        this.sensorNumber = sensorNumber;
        this.data = data;
        //sensor rate is held is second byte
        sensorRate = (int)data[1];
        // the sync number (24 bits) is held in bytes 2-4
        syncNumber = ((int)data[2]<<16) | ((int)data[3]<<8) | ((int)data[4]);
    }

    public void updateData(byte data[]) {
        this.data = data;
        sensorRate = (int)data[1];
        syncNumber = ((int)data[2]<<16) | ((int)data[3]<<8) | ((int)data[4]);
    }

    public int getSyncNumber() {
        return syncNumber;
    }

    public int getSensorRate() {
        return sensorRate;
    }

    public int getSensorNumber() {
        return sensorNumber;
    }

    public boolean isSDLog() {
        return SDLog;
    }

    abstract String[] calcSensorData();

    abstract float getGraphData();


}
