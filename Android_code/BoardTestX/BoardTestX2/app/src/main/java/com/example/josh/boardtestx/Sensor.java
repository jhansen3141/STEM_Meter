package com.example.josh.boardtestx;

/**
 * Created by Josh on 11/28/2016.
 */

public abstract class Sensor {
    byte data[];

    public Sensor(byte data[]) {
        this.data = data;
    }

    abstract String[] calcSensorData();
}