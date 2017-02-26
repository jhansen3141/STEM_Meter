package com.stemmeter.stem_meter;

import java.util.ArrayList;

/**
 * Created by Josh on 2/26/2017.
 */
public class GraphSettings {
    private ArrayList<String> units;
    private ArrayList<String> dataPoints;

    public GraphSettings(ArrayList<String> units, ArrayList<String> dataPoints) {
        this.units = units;
        this.dataPoints = dataPoints;
    }

    public ArrayList<String> getUnits() {
        return units;
    }

    public ArrayList<String> getDataPoints() {
        return dataPoints;
    }
}
