package com.stemmeter.stem_meter;

/**
 * Created by monro on 2/14/2017.
 */

public class GraphConfig {

    private int state;
    private int selectedSensor;
    private int visibleDataNum;

    public GraphConfig (int sensor, int visibleDataNum)
    {
        state = 0;
        this.selectedSensor = selectedSensor;
        this.visibleDataNum = visibleDataNum;
    }

    public GraphConfig ()
    {
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
}
