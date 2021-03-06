package com.stemmeter.stem_meter.Sensors;

import android.util.Log;

import com.stemmeter.stem_meter.GraphSettings;
import com.stemmeter.stem_meter.SensorConst;

import java.util.ArrayList;

/**
 * Created by Josh on 2/19/2017.
 */
public class MAG_MAG3110 extends Sensor {

    private String[] sensorStringArray;
    private float xMagF,yMagF,zMagF;
    private final float MAG_SENSE = 10.0f;

    private float xMagZero = 0;
    private float yMagZero = 0;
    private float zMagZero = 0;

    private boolean shouldZero = false;

    private GraphSettings graphSettings;
    private int units = SensorConst.MAG_UNIT_T;
    private ArrayList<String> unitList1;
    private ArrayList<String> dataPointList;

    public MAG_MAG3110(byte[] data, int sensorPosition) {
        super(data, sensorPosition,3);

        unitList1 = new ArrayList<>();
        dataPointList = new ArrayList<>();

        // micro tesla
        unitList1.add("uT");

        dataPointList.add("Mag X");
        dataPointList.add("Mag Y");
        dataPointList.add("Mag Z");

        graphSettings = new GraphSettings(unitList1, null, dataPointList);
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

        if(shouldZero) {
            xMagZero = -(xMagF);
            yMagZero = -(yMagF);
            zMagZero = -(zMagF);

            shouldZero = false;
        }

        xMagF += xMagZero;
        yMagF += yMagZero;
        zMagF += zMagZero;


        dataStr[0] = String.format(java.util.Locale.US,"%.2f",xMagF);
        dataStr[1] = String.format(java.util.Locale.US,"%.2f",yMagF);
        dataStr[2] = String.format(java.util.Locale.US,"%.2f",zMagF);

        sensorStringArray = dataStr;

        return new String[0];
    }

    @Override
    public String toString() {
        if(this.getSensorRate() == SensorConst.RATE_OFF ||
                this.getSensorRate() == SensorConst.RATE_INFO ) {
            return this.getSensorOffString();
        }

        if(sensorStringArray != null) {
            String unitString = unitList1.get(units);
            return "Magnetometer\n" +
                    " X:  " + sensorStringArray[0] + unitString + "\n" +
                    " Y:  " + sensorStringArray[1] + unitString + "\n" +
                    " Z:  " + sensorStringArray[2] + unitString;
        }
        else {
            return "NULL";
        }
    }

    @Override
    public SensorReading getGraphData() {
        SensorReading sensorReading= new SensorReading(this.getSensorTime());
        sensorReading.addGraphData(xMagF);
        sensorReading.addGraphData(yMagF);
        sensorReading.addGraphData(zMagF);
        return sensorReading;
    }

    @Override
    public GraphSettings getGraphSettings() {
        return graphSettings;
    }

    @Override
    public void setGraphUnits1(int units) {
        this.units = units;
    }

    @Override
    public void setGraphUnits2(int units) { }

    @Override
    public void zeroSensor() {
        shouldZero = true;
    }

    @Override
    public void resetZero() {
        xMagZero = 0;
        yMagZero = 0;
        zMagZero = 0;

        shouldZero = false;
    }

    @Override
    public String getSensorOffString() {
        return "Magnetometer - OFF";
    }

    @Override
    public int getSensorType() {return SensorConst.MAG_MAG3110; }

    @Override
    public String getSensorName() { return "Magnetometer"; }
}
