package com.stemmeter.stem_meter.Sensors;

import com.stemmeter.stem_meter.GraphSettings;

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
        syncNumber = (((data[2] & 0xFF)<<16) | ((data[3] & 0xFF)<<8) | (data[4] & 0xFF));

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

    public abstract GraphSettings getGraphSettings();

    public abstract void setGraphUnits(int units);

    public int getNumberDataPoints() {
        return numberDataPoints;
    }
}