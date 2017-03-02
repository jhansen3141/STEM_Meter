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

        airPressure = (float)data[7] / 4.0f;

        airPressure += (float)pressure;

        switch(units) {
            case SensorConst.PRESSURE_UNIT_HPA:
                airPressure /= 100;
                break;
        }
        altitude = 7000.0f * Math.log(101325.0f/airPressure);

        altitude *= 3.28084;

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
    public ArrayList<Float> getGraphData() {
        ArrayList<Float> graphData = new ArrayList<>();
        graphData.add(airPressure);
        graphData.add((float)altitude);
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
}
