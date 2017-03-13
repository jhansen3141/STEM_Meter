package com.stemmeter.stem_meter.Sensors;

import java.util.ArrayList;

/**
 * Created by Josh on 3/12/2017.
 */
public class SensorReading {
    private ArrayList<Float> graphData;
    private float sensorReadingTime;

    public SensorReading(float sensorReadingTime) {
        this.sensorReadingTime = sensorReadingTime;
        graphData = new ArrayList<>();
    }

    public ArrayList<Float> getGraphData() {
        return graphData;
    }

    public float getSensorReadingTime() {
        return sensorReadingTime;
    }

    public void addGraphData(float data) {
        graphData.add(data);
    }
}
