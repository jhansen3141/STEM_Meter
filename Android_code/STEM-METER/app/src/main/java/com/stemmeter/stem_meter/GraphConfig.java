package com.stemmeter.stem_meter;

import java.util.ArrayList;

/**
 * Created by monro on 2/14/2017.
 */

public class GraphConfig {

    private int state;
    private int selectedSensor;
    private int visibleDataNum;
    private ArrayList<Boolean> dataPoints;

    public GraphConfig (int sensor, int visibleDataNum)
    {
        dataPoints = new ArrayList<>();
        dataPoints.add(true);
        dataPoints.add(true);
        dataPoints.add(true);
        state = 0;
        this.selectedSensor = selectedSensor;
        this.visibleDataNum = visibleDataNum;
    }

    public GraphConfig ()
    {
        dataPoints = new ArrayList<>();
        dataPoints.add(true);
        dataPoints.add(true);
        dataPoints.add(true);
        state = 0;
        this.selectedSensor = 0;
        this.visibleDataNum = 10;
    }

    public int getSelectedSensor() {
        return selectedSensor;
    }

    public void setSelectedSensor(int selectedSensor) {
        this.selectedSensor = selectedSensor;
    }

    public int getVisibleDataNum() {
        return visibleDataNum;
    }

    public void setVisibleDataNum(int visibleDataNum) {
        this.visibleDataNum = visibleDataNum;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public ArrayList<Boolean> getDataPoints() {
        return dataPoints;
    }

    public void setDataPoints(ArrayList<Boolean> dataPoints) {
        this.dataPoints = dataPoints;
    }
}
