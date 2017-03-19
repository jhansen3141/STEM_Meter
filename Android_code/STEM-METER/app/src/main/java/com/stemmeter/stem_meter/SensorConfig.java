package com.stemmeter.stem_meter;

/**
 * Created by monro on 2/12/2017.
 */

public class SensorConfig {

    private int freq;
    private int sensorNumber;
    private boolean isSDLogging;
    private boolean selected;

    public SensorConfig(int sensorNumber, int freq, boolean isSDLogging) {
        this.sensorNumber = sensorNumber;
        this.freq = freq;
        this.isSDLogging = isSDLogging;
        selected = false;
    }

    public SensorConfig(int sensorNumber) {
        this.sensorNumber = sensorNumber;
        this.freq = SensorConst.RATE_OFF;
        this.isSDLogging = false;
        selected = false;
    }

    public int getSensorNumber() {
        return sensorNumber;
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

    @Override
    protected SensorConfig clone() throws CloneNotSupportedException {
        return new SensorConfig(this.sensorNumber,this.freq,this.isSDLogging);
    }
}
