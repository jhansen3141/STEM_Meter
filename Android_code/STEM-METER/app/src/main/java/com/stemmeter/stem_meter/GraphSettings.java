package com.stemmeter.stem_meter;

import java.util.ArrayList;

/**
 * Created by Josh on 2/26/2017.
 */
public class GraphSettings {
    private ArrayList<String> dataSet1Units;
    private ArrayList<String> dataSet2Units;
    private ArrayList<String> dataPoints;

    public GraphSettings(ArrayList<String> units1, ArrayList<String> units2, ArrayList<String> dataPoints) {
        this.dataSet1Units = units1;
        this.dataSet2Units = units2;
        this.dataPoints = dataPoints;
    }

    public ArrayList<String> getDataSet1Units() {
        return dataSet1Units;
    }

    public ArrayList<String> getDataSet2Units() {
        return dataSet2Units;
    }

    public ArrayList<String> getDataPoints() {
        return dataPoints;
    }

    public boolean sensorHasUniqueDataSetUnits()
    {
        return this.dataSet2Units != null;
    }
}
