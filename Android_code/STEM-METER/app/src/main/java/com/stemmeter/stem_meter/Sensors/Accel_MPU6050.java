package com.stemmeter.stem_meter.Sensors;

import android.graphics.Color;

import com.stemmeter.stem_meter.GraphSettings;
import com.stemmeter.stem_meter.SensorConst;

import java.util.ArrayList;

/**
 * Created by Josh on 11/28/2016.
 */

public class Accel_MPU6050 extends Sensor {


    private int ACCEL_SENSE_COLOR = Color.BLUE;
    private String[] sensorStringArray;
    private float xAccelF,yAccelF,zAccelF;
    private GraphSettings graphSettings;
    private int units = SensorConst.ACCEL_UNIT_G;
    private ArrayList<String> unitList;
    private ArrayList<String> dataPointList;

    public Accel_MPU6050(byte[] data, int sensorPosition) {
        super(data, sensorPosition,3);

        unitList = new ArrayList<>();
        dataPointList = new ArrayList<>();

        unitList.add("Gs");
        unitList.add("m/s" + "\u00B2");
        unitList.add("f/s" + "\u00B2");

        dataPointList.add("X");
        dataPointList.add("Y");
        dataPointList.add("Z");

        graphSettings = new GraphSettings(unitList,dataPointList);

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

        switch (units) {
            case SensorConst.ACCEL_UNIT_MS:
                xAccelF *= 9.80665f;
                yAccelF *= 9.80665f;
                zAccelF *= 9.80665f;
                break;
            case SensorConst.ACCEL_UNIT_FS:
                xAccelF *= 32.1740f;
                yAccelF *= 32.1740f;
                zAccelF *= 32.1740f;
                break;

        }

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
    public GraphSettings getGraphSettings() {
        return graphSettings;
    }

    @Override
    public void setGraphUnits(int units) {
        this.units = units;
    }


    @Override
    public String toString() {
        if(sensorStringArray != null) {
            String unitString = unitList.get(units);


            return  "Acceleration\n" +
                    " X:  " + sensorStringArray[0] + unitString +"\n" +
                    " Y:  " + sensorStringArray[1] + unitString + "\n" +
                    " Z:  " + sensorStringArray[2] + unitString;
        }
        else {
            return "NULL";
        }
    }

}