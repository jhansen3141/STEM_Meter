package com.stemmeter.stem_meter.Sensors;

import android.graphics.Color;

import com.stemmeter.stem_meter.GraphSettings;
import com.stemmeter.stem_meter.SensorConst;

import java.util.ArrayList;

/**
 * Created by Josh on 11/28/2016.
 */

public class Gyro_MPU6050 extends Sensor {

    private int ACCEL_SENSE_COLOR = Color.BLUE;
    private String[] sensorStringArray;
    private float xGyroF,yGyroF,zGyroF;

    private float xGyroZero = 0;
    private float yGyroZero = 0;
    private float zGyroZero = 0;

    private boolean shouldZero = false;

    private GraphSettings graphSettings;
    private int units = SensorConst.GYRO_UNIT_DS;
    private ArrayList<String> unitList;
    private ArrayList<String> dataPointList;


    public Gyro_MPU6050(byte[] data, int sensorPosition) {
        super(data, sensorPosition,3);

        unitList = new ArrayList<>();
        dataPointList = new ArrayList<>();

        unitList.add("°s");
        unitList.add("rad/s");

        dataPointList.add("Gyro X");
        dataPointList.add("Gyro Y");
        dataPointList.add("Gyro Z");

        graphSettings = new GraphSettings(unitList,dataPointList);

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

        if(shouldZero) {
            xGyroZero = -(xGyroF);
            yGyroZero = -(yGyroF);
            zGyroZero = -(zGyroF);

            shouldZero = false;
        }

        xGyroF += xGyroZero;
        yGyroF += yGyroZero;
        zGyroF += zGyroZero;

        switch(units) {
            // radians per second
            case SensorConst.GYRO_UNIT_RS:
                xGyroF *= 0.0174533f;
                yGyroF *= 0.0174533f;
                zGyroF *= 0.0174533f;
                break;
        }

        dataStr[0] = String.format(java.util.Locale.US,"%.2f",xGyroF);
        dataStr[1] = String.format(java.util.Locale.US,"%.2f",yGyroF);
        dataStr[2] = String.format(java.util.Locale.US,"%.2f",zGyroF);

        sensorStringArray = dataStr;

        return dataStr;
    }

    @Override
    public SensorReading getGraphData() {
        SensorReading sensorReading= new SensorReading(this.getSensorTime());
        sensorReading.addGraphData(xGyroF);
        sensorReading.addGraphData(yGyroF);
        sensorReading.addGraphData(zGyroF);
        return sensorReading;
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
    public void zeroSensor() {
        shouldZero = true;
    }

    @Override
    public void resetZero() {
        xGyroZero = 0;
        yGyroZero = 0;
        zGyroZero = 0;

        shouldZero = false;
    }

    @Override
    public String toString() {
        if(sensorStringArray != null) {
            String unitString = unitList.get(units);

            return "Rotation\n" +
                    " X:  " + sensorStringArray[0] + unitString + "\n" +
                    " Y:  " + sensorStringArray[1] + unitString + "\n" +
                    " Z:  " + sensorStringArray[2] + unitString;
        }
        else {
            return "NULL";
        }
    }

    @Override
    public String getSensorOffString() {
        return "Gyroscope - OFF";
    }

    @Override
    public int getSensorType() {return SensorConst.GYRO_MPU6050; }

}
