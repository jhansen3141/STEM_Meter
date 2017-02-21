package com.stemmeter.stem_meter.Sensors;

import java.util.ArrayList;

/**
 * Created by Josh on 2/19/2017.
 */
public class Mag_MAG3110 extends Sensor {
    // HI MONROE
    private String[] sensorStringArray;
    private float xMagF,yMagF,zMagF;
    private final float MAG_SENSE = 10.0f;

    public Mag_MAG3110(byte[] data, int sensorPosition) {
        super(data, sensorPosition,3);
    }

    @Override
    public String[] calcSensorData() {
        String[] dataStr = new String[3];

        short xMag = (short)((data[5]<<8)   | (data[6] & 0xFF));
        short yMag = (short)((data[7]<<8)   | (data[8] & 0xFF));
        short zMag = (short)((data[9]<<8)   | (data[10] & 0xFF));

        xMagF = (float)xMag / MAG_SENSE;
        yMagF = (float)yMag / MAG_SENSE;
        zMagF = (float)zMag / MAG_SENSE;

        dataStr[0] = String.format(java.util.Locale.US,"%.2f",xMagF);
        dataStr[1] = String.format(java.util.Locale.US,"%.2f",yMagF);
        dataStr[2] = String.format(java.util.Locale.US,"%.2f",zMagF);

        sensorStringArray = dataStr;

        return new String[0];
    }

    @Override
    public String toString() {
        if(sensorStringArray != null) {
            // TODO add units
            return "Magnetometer\n" +
                    " X:  " + sensorStringArray[0] + "\n" +
                    " Y:  " + sensorStringArray[1] + "\n" +
                    " Z:  " + sensorStringArray[2] + "";
        }
        else {
            return "NULL";
        }
    }

    @Override
    public ArrayList<Float> getGraphData() {
        ArrayList<Float> graphData = new ArrayList<>();
        graphData.add(xMagF);
        graphData.add(yMagF);
        graphData.add(zMagF);
        return graphData;
    }
}
