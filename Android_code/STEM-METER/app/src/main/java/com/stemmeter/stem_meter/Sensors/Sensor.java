package com.stemmeter.stem_meter.Sensors;

import java.util.ArrayList;

/**
 * Created by Josh on 11/28/2016.
 */

public abstract class Sensor {
    byte data[];
    private int sensorNumber;
    private int syncNumber;
    private int sensorRate;
    private int color;
    private int numberDataPoints;

    public Sensor(byte data[], int sensorNumber, int numberDataPoints) {
        this.numberDataPoints = numberDataPoints;
        this.sensorNumber = sensorNumber;
        this.data = data;
        //sensor rate is held is second byte
        sensorRate = (int)data[1];
        // the sync number (24 bits) is held in bytes 2-4
        syncNumber = ((int)data[2]<<16) | ((int)data[3]<<8) | ((int)data[4]);
    }

    public void updateData(byte data[]) {
        /*
        Frame format:
        Byte[0] = Sensor number
        Byte[1] = Sensor Rate
        Byte[2] = Sync number high byte
        Byte[3] = Sync number middle byte
        Byte[4] = Sync number low byte
        Byte[5-19] = Raw data (depends on sensor type)
        */
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

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public abstract String[] calcSensorData();

    public abstract ArrayList<Float> getGraphData();

    public int getNumberDataPoints() {
        return numberDataPoints;
    }
}