package com.stemmeter.stem_meter.Sensors;

import android.graphics.Color;

import java.util.ArrayList;

/**
 * Created by Josh on 11/28/2016.
 */

public class Accel_MPU6050 extends Sensor {


    private int ACCEL_SENSE_COLOR = Color.BLUE;
    private String[] sensorStringArray;
    private float xAccelF,yAccelF,zAccelF;


    public Accel_MPU6050(byte[] data, int sensorPosition) {
        super(data, sensorPosition,3);
        this.setColor(ACCEL_SENSE_COLOR);
    }

    @Override
    public String[] calcSensorData() {
        final float ACCEL_SENSE = 2048.0f; // +-16g
        String[] dataStr = new String[3];

        short xAccel = (short)((data[5]<<8)   | (data[6] & 0xFF));
        short yAccel = (short)((data[7]<<8)   | (data[8] & 0xFF));
        short zAccel = (short)((data[9]<<8)   | (data[10] & 0xFF));

        xAccelF = xAccel / ACCEL_SENSE;
        yAccelF = yAccel / ACCEL_SENSE;
        zAccelF = zAccel / ACCEL_SENSE;

        dataStr[0] = String.format(java.util.Locale.US,"%.2f",xAccelF);
        dataStr[1] = String.format(java.util.Locale.US,"%.2f",yAccelF);
        dataStr[2] = String.format(java.util.Locale.US,"%.2f",zAccelF);

        sensorStringArray = dataStr;

        return dataStr;
    }

    @Override
    public ArrayList<Float> getGraphData() {
        ArrayList<Float> graphData = new ArrayList<>();
        graphData.add(xAccelF);
        graphData.add(yAccelF);
        graphData.add(zAccelF);
        return graphData;
    }

    @Override
    public String toString() {
        if(sensorStringArray != null) {
            return "Acceleration\n" +
                    " X:  " + sensorStringArray[0] + "g\n" +
                    " Y:  " + sensorStringArray[1] + "g\n" +
                    " Z:  " + sensorStringArray[2] + "g";
        }
        else {
            return "NULL";
        }
    }

}