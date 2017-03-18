package com.stemmeter.stem_meter.Sensors;

import android.util.Log;

import com.stemmeter.stem_meter.GraphSettings;
import com.stemmeter.stem_meter.SensorConst;

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
    private int syncNumberOffset = 0;
    private float rateMult;

    public Sensor(byte data[], int sensorNumber, int numberDataPoints) {
        this.numberDataPoints = numberDataPoints;
        this.sensorNumber = sensorNumber;
        this.data = data;
        //sensor rate is held is second byte
        sensorRate = (int)data[1];
        // the sync number (24 bits) is held in bytes 2-4
        syncNumber = (((data[2] & 0xFF)<<16) | ((data[3] & 0xFF)<<8) | (data[4] & 0xFF));

    }

    public void updateData(byte data[]) {
        this.data = data;
        //sensor rate is held is second byte
        sensorRate = (int)data[1];
        // the sync number (24 bits) is held in bytes 2-4
        syncNumber = (((data[2] & 0xFF)<<16) | ((data[3] & 0xFF)<<8) | (data[4] & 0xFF));
    }
    public int getSyncNumber() {
        return syncNumber - syncNumberOffset;
    }

    public void zeroX() {
        syncNumberOffset = syncNumber + 1;
    }

    public float getSensorTime() {
        rateMult = 0;

        switch(sensorRate) {
            case SensorConst.RATE_OFF:
                rateMult = 0;
                break;
            case SensorConst.RATE_FIVE_HZ:
                rateMult = 0.2f;
                break;
            case SensorConst.RATE_TEN_HZ:
                rateMult = 0.1f;
                break;
            case SensorConst.RATE_ONE_SEC:
                rateMult = 1.0f;
                break;
            case SensorConst.RATE_FIVE_SEC:
                rateMult = 5.0f;
                break;
            case SensorConst.RATE_TEN_SEC:
                rateMult = 10.0f;
                break;
            case SensorConst.RATE_THIRTY_SEC:
                rateMult = 30.0f;
                break;
            case SensorConst.RATE_ONE_MIN:
                rateMult = 60.0f;
                break;
            case SensorConst.RATE_TEN_MIN:
                rateMult = 60.0f * 10;
                break;
            case SensorConst.RATE_THIRTY_MIN:
                rateMult = 60.0f * 30;
                break;
            case SensorConst.RATE_ONE_HOUR:
                rateMult = 60.0f * 60;
                break;
        }

        return ( (float)getSyncNumber() * rateMult );
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

    public abstract SensorReading getGraphData();

    public abstract GraphSettings getGraphSettings();

    public abstract void setGraphUnits(int units);

    public abstract void zeroSensor();

    public abstract void resetZero();

    public int getNumberDataPoints() {
        return numberDataPoints;
    }

    public float getRateMult() { return rateMult; }
}