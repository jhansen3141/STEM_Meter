package com.stemmeter.stem_meter;

/**
 * Created by monro on 2/12/2017.
 */

public class SensorConfig {

    private int freq;
    private boolean isSDLogging;
    private boolean selected;

    public SensorConfig(int freq, boolean isSDLogging) {
        this.freq = freq;
        this.isSDLogging = isSDLogging;
        selected = false;
    }

    public SensorConfig() {
        this.freq = SensorList.RATE_OFF;
        this.isSDLogging = false;
        selected = false;
    }

    public int getFreq() {
        return freq;
    }

    public void setFreq(int freq) {
        this.freq = freq;
    }

    public boolean isSDLogging() {
        return isSDLogging;
    }

    public void setSDLogging(boolean SDLogging) {
        isSDLogging = SDLogging;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

}
