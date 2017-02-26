package com.stemmeter.stem_meter.Sensors;

import android.graphics.Color;

import com.stemmeter.stem_meter.GraphSettings;

import java.util.ArrayList;

/**
 * Created by Josh on 11/28/2016.
 */

public class Gyro_MPU6050 extends Sensor {

    private int ACCEL_SENSE_COLOR = Color.BLUE;
    private String[] sensorStringArray;
    private float xGyroF,yGyroF,zGyroF;

    public Gyro_MPU6050(byte[] data, int sensorPosition) {
        super(data, sensorPosition,3);
        this.setColor(ACCEL_SENSE_COLOR);
    }

    @Override
    public String[] calcSensorData() {
        final float GYRO_SENSE = 16.384f; // +-2000 degress/s
        String[] dataStr = new String[3];

        short xGyro  = (short)((data[5]<<8)  | (data[6] & 0xFF));
        short yGyro  = (short)((data[7]<<8)  | (data[8] & 0xFF));
        short zGyro  = (short)((data[9]<<8)  | (data[10] & 0xFF));

        xGyroF = xGyro / GYRO_SENSE;
        yGyroF = yGyro / GYRO_SENSE;
        zGyroF = zGyro / GYRO_SENSE;

        dataStr[0] = String.format(java.util.Locale.US,"%.2f",xGyroF);
        dataStr[1] = String.format(java.util.Locale.US,"%.2f",yGyroF);
        dataStr[2] = String.format(java.util.Locale.US,"%.2f",zGyroF);

        sensorStringArray = dataStr;

        return dataStr;
    }

    @Override
    public ArrayList<Float> getGraphData() {
        ArrayList<Float> graphData = new ArrayList<>();
        graphData.add(xGyroF);
        graphData.add(yGyroF);
        graphData.add(zGyroF);
        return graphData;
    }

    @Override
    public GraphSettings getGraphSettings() {
        return null;
    }

    @Override
    public void setGraphUnits(int units) {

    }

    @Override
    public String toString() {
        if(sensorStringArray != null) {
            return "Rotation\n" +
                    " X:  " + sensorStringArray[0] + "\u00b0/s\n" +
                    " Y:  " + sensorStringArray[1] + "\u00b0/s\n" +
                    " Z:  " + sensorStringArray[2] + "\u00b0/s ";
        }
        else {
            return "NULL";
        }
    }



}
