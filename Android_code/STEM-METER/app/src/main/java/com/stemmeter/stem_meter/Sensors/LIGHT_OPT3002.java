package com.stemmeter.stem_meter.Sensors;

/**
 * Created by Josh on 2/19/2017.
 */
public class LIGHT_OPT3002 extends Sensor {

    public LIGHT_OPT3002(byte[] data, int sensorPosition) {
        super(data, sensorPosition);
    }

    @Override
    public String[] calcSensorData() {
        return new String[0];
    }

    @Override
    public float getGraphData() {
        return 0;
    }
}
