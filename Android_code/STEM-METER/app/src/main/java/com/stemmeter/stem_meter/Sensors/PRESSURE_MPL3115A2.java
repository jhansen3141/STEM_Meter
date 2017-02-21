package com.stemmeter.stem_meter.Sensors;

import java.util.ArrayList;

/**
 * Created by Josh on 2/19/2017.
 */
public class Pressure_MPL3115A2 extends Sensor {

    private String[] sensorStringArray;
    private float airPressure;

    public Pressure_MPL3115A2(byte[] data, int sensorPosition) {
        super(data, sensorPosition,1);
    }

    @Override
    public String[] calcSensorData() {
        String[] dataStr = new String[1];
        int pressure;

        // Combine the bytes together
        pressure = ((int)(data[5]<<16)) | ((int)(data[6]<<8)) | (data[7] & 0xFF);

        // Pressure is an 18 bit number with 2 bits of decimal. Get rid of decimal portion
        pressure >>= 6;

        // Bits 5,4 fractional component
        data[7] &= 0x30;

        // Align it
        data[7] >>= 4;

        airPressure = (float)data[7] / 4.0f;

        airPressure += (float)pressure;

        dataStr[0] = String.format(java.util.Locale.US,"%.2f",airPressure);
        sensorStringArray = dataStr;

        return dataStr;
    }

    @Override
    public String toString() {
        if(sensorStringArray != null) {
            // TODO add units
            return "Air Pressure\n" +
                    sensorStringArray[0] + "\n";
        }
        else {
            return "NULL";
        }
    }

    @Override
    public ArrayList<Float> getGraphData() {
        ArrayList<Float> graphData = new ArrayList<>();
        graphData.add(airPressure);
        return graphData;
    }
}