package com.stemmeter.stem_meter.Sensors;

import com.stemmeter.stem_meter.GraphSettings;
import com.stemmeter.stem_meter.SensorConst;

import java.util.ArrayList;

/**
 * Created by Josh on 2/19/2017.
 */
public class PRESSURE_MPL3115A2 extends Sensor {

    private String[] sensorStringArray;
    private float airPressure;
    private double altitude;

    private double altitudeOffsetWeather = 0;

    private float pressureZero = 0;
    private double altitudeZero = 0;

    private boolean shouldZero = false;

    private GraphSettings graphSettings;
    private int units = SensorConst.PRESSURE_UNIT_PA;
    private ArrayList<String> unitList;
    private ArrayList<String> dataPointList;

    public PRESSURE_MPL3115A2(byte[] data, int sensorPosition) {
        super(data, sensorPosition,2);

        unitList = new ArrayList<>();
        dataPointList = new ArrayList<>();

        // Pascals
        unitList.add("Pa");
        // 100 Pascals
        unitList.add("hPa");

        dataPointList.add("Pressure");
        dataPointList.add("Altitude");

        graphSettings = new GraphSettings(unitList,dataPointList);
    }

    @Override
    public String[] calcSensorData() {
        String[] dataStr = new String[2];
        int pressure;

        // Combine the bytes together
        pressure = ((data[5] << 16)) | (((data[6] & 0xFF) << 8)) | (data[7] & 0xFF);

        // Pressure is an 18 bit number with 2 bits of decimal. Get rid of decimal portion
        pressure >>= 6;

        // Bits 5,4 fractional component
        data[7] &= 0x30;

        // Align it
        data[7] >>= 4;

        // get the fractional part
        airPressure = (float)data[7] / 4.0f;

        // add the integer part
        airPressure += (float)pressure;

       // altitude = 7000.0f * Math.log(101325.0f/airPressure);

        altitude = ( 44330.77 * ( 1- Math.pow( (airPressure / 101326), 0.1902632f) ) ) + altitudeOffsetWeather;

        altitude *= 3.28084;

        if(shouldZero) {
            pressureZero = -(airPressure);
            altitudeZero = -(altitude);
            shouldZero = false;
        }

        altitude += altitudeZero;
        airPressure += pressureZero;

        switch(units) {
            case SensorConst.PRESSURE_UNIT_HPA:
                airPressure /= 100;
                break;
        }

        dataStr[0] = String.format(java.util.Locale.US,"%.2f",airPressure);
        dataStr[1] = String.format(java.util.Locale.US,"%.2f",altitude);
        sensorStringArray = dataStr;

        return dataStr;
    }

    @Override
    public String toString() {
        if(sensorStringArray != null) {
            String unitsString = unitList.get(units);
            return "Air Pressure\n" +
                    sensorStringArray[0] + unitsString + "\n" +
                    sensorStringArray[1] + " Ft";
        }
        else {
            return "NULL";
        }
    }

    @Override
    public SensorReading getGraphData() {
        SensorReading sensorReading= new SensorReading(this.getSensorTime());
        sensorReading.addGraphData(airPressure);
        sensorReading.addGraphData((float) altitude);
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
        altitudeZero = 0;
        pressureZero = 0;

        shouldZero = false;
    }

    @Override
    public String getSensorOffString() {
        return "Air Pressure Sensor - OFF";
    }
}
