package com.stemmeter.stem_meter.Sensors;

import java.util.ArrayList;

/**
 * Created by Josh on 2/19/2017.
 */
public class Light_OPT3002 extends Sensor {
    private String[] sensorStringArray;
    private float opticalPower;

    public Light_OPT3002(byte[] data, int sensorPosition) {
        super(data, sensorPosition,1);
    }

    @Override
    public String[] calcSensorData() {
        String[] dataStr = new String[1];
        short rawData;
        short exponent;
        short mantissa;
        float multiplier;

        rawData = (short)((short)(data[5]<<8)   | (data[6] & 0xFF));

        // Exponent is held in B15:B12
        exponent = (short)((rawData >> 12) & 0x000F);

        // Mantissa is held in B11:B0
        mantissa = (short)(rawData & 0x0FFF);

        multiplier = ((short)(1<<exponent) * 1.2f);

        // optical power =
        // 2^(B15:B12) * (B11:B0) * 1.2 nW/cm^2
        opticalPower = (float)mantissa * multiplier;

        dataStr[0] = String.format(java.util.Locale.US,"%.2f",opticalPower);
        sensorStringArray = dataStr;

        return dataStr;
    }

    @Override
    public String toString() {
        if(sensorStringArray != null) {
            return "Optical Power\n" +
                    sensorStringArray[0] + "nW/cm^2\n";
        }
        else {
            return "NULL";
        }
    }

    @Override
    public ArrayList<Float> getGraphData() {
        ArrayList<Float> graphData = new ArrayList<>();
        graphData.add(opticalPower);
        return graphData;
    }
}
