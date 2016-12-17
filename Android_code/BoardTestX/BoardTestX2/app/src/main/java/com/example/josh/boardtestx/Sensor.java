package com.example.josh.boardtestx;

/**
 * Created by Josh on 11/28/2016.
 */

public abstract class Sensor {
    byte data[];
    private int syncNumber;
    private int sensorRate;

    public Sensor(byte data[]) {
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

    abstract String[] calcSensorData();
}