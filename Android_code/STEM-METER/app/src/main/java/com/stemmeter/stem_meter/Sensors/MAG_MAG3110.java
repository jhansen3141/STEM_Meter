package com.stemmeter.stem_meter.Sensors;

import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Josh on 2/19/2017.
 */
public class MAG_MAG3110 extends Sensor {

    private String[] sensorStringArray;
    private float xMagF,yMagF,zMagF;
    private double heading;
    private final float MAG_SENSE = 10.0f;

    public MAG_MAG3110(byte[] data, int sensorPosition) {
        super(data, sensorPosition,4);
    }

    @Override
    public String[] calcSensorData() {
        String[] dataStr = new String[4];

        int xMag = (short)((data[5]<<8)   | (data[6] & 0xFF));
        int yMag = (short)((data[7]<<8)   | (data[8] & 0xFF));
        int zMag = (short)((data[9]<<8)   | (data[10] & 0xFF));

        xMagF = (float)xMag / MAG_SENSE;
        yMagF = (float)yMag / MAG_SENSE;
        zMagF = (float)zMag / MAG_SENSE;



//        heading = Math.atan2((double)xMagF, (double)-yMagF);
//
//        if(heading < 0) {
//            heading += 2*Math.PI;
//        }
//
//        heading *= 180 / Math.PI;


        dataStr[0] = String.format(java.util.Locale.US,"%.2f",xMagF);
        dataStr[1] = String.format(java.util.Locale.US,"%.2f",yMagF);
        dataStr[2] = String.format(java.util.Locale.US,"%.2f",zMagF);
       // dataStr[3] = String.format(java.util.Locale.US,"%.2f",heading);

        sensorStringArray = dataStr;

        return new String[0];
    }

    @Override
    public String toString() {
        if(sensorStringArray != null) {
            // TODO add units
            return "Magnetometer\n" +
                    " X:  " + sensorStringArray[0] + " \u00B5" + "T\n" +
                    " Y:  " + sensorStringArray[1] + " \u00B5" + "T\n" +
                    " Z:  " + sensorStringArray[2] + " \u00B5" + "T";
                   // " Heading: " + sensorStringArray[3];
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
        graphData.add((float)heading);
        return graphData;
    }
}
