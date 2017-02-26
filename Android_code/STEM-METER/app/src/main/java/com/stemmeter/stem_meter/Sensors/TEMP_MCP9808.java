package com.stemmeter.stem_meter.Sensors;

import com.stemmeter.stem_meter.GraphSettings;
import com.stemmeter.stem_meter.SensorConst;

import java.util.ArrayList;

/**
 * Created by Josh on 11/28/2016.
 */

public class Temp_MCP9808 extends Sensor {

    private String[] sensorStringArray;
    private float temp;

    private GraphSettings graphSettings;
    private int units = SensorConst.TEMP_UNIT_C;
    private ArrayList<String> unitList;
    private ArrayList<String> dataPointList;

    public Temp_MCP9808(byte[] data, int sensorPosition) {
        super(data, sensorPosition,2);

        unitList = new ArrayList<>();
        dataPointList = new ArrayList<>();

        unitList.add("°C");
        unitList.add("°F");

        dataPointList.add("Temp");

        graphSettings = new GraphSettings(unitList,dataPointList);
    }

    @Override
    public String[] calcSensorData() {
        double temperature;
        String[] dataStr = new String[1];
        short tempRaw = (short)((data[5]<<8)   | (data[6] & 0xFF));

        temperature = tempRaw & 0x0FFF;
        temperature /=  16.0f;
        if ((tempRaw & 0x1000) != 0) {
            temperature -= 256;
        }
        temp = (float)temperature;

        switch(units) {
            case SensorConst.TEMP_UNIT_F:
                temp = (float)((temperature * 1.8) + 32.0);
                break;
        }

        dataStr[0] = String.format(java.util.Locale.US,"%.2f",temp);

        sensorStringArray = dataStr;
        return dataStr;
    }

    @Override
    public ArrayList<Float> getGraphData() {
        ArrayList<Float> graphData = new ArrayList<>();
        graphData.add(temp);
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
        String unitsString = unitList.get(units);
        return "Temperature: " + sensorStringArray[0] + unitsString;
    }



}