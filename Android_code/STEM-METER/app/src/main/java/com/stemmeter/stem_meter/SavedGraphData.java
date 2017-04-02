package com.stemmeter.stem_meter;

import com.github.mikephil.charting.data.LineData;

public class SavedGraphData {

    private String name;
    private LineData data;
    private int rate;
    private String units;

    public SavedGraphData(String name, LineData data, int rate, String units)
    {
        this.rate = rate;
        this.data = data;
        this.name = name;
        this.units = units;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LineData getData() {
        return data;
    }

    public void setData(LineData data) {
        this.data = data;
    }

    public int getRate() {
        return rate;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }
}