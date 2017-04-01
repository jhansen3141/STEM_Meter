package com.stemmeter.stem_meter.Sensors;

import android.util.Log;

import com.stemmeter.stem_meter.GraphSettings;
import com.stemmeter.stem_meter.SensorConst;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by Josh on 2/19/2017.
 */
public class LIGHT_OPT3002 extends Sensor {
    private String[] sensorStringArray;
    private double opticalPower;

    private double opticalPowerZero = 0.0;
    private boolean shouldZero = false;

    private GraphSettings graphSettings;
    private int units = SensorConst.LIGHT_UNIT_UW;
    private ArrayList<String> unitList;
    private ArrayList<String> dataPointList;
    private double opticalPowerLast = 0;

    public LIGHT_OPT3002(byte[] data, int sensorPosition) {
        super(data, sensorPosition,1);

        unitList = new ArrayList<>();
        dataPointList = new ArrayList<>();

        unitList.add("\u00B5" + "W/cm" +  "\u00B2");
        unitList.add("Lux");

        dataPointList.add("Optical Power");

        graphSettings = new GraphSettings(unitList,dataPointList);
    }

    @Override
    public String[] calcSensorData() {
        String[] dataStr = new String[1];

        byte[] byteString = new byte[15];

        // copy data minus string and new line term into byte array
        for(int i=0;i<15;i++) {
            if( (data[5 + i] == 0) || (data[5 + i] == '\n')) {
                break;
            }
            byteString[i] = data[5 + i];
        }

        try {
            // convert byte array into string
            dataStr[0] =  new String(byteString, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Log.i("LightSensor", "String format incorrect");
        }
        try {
            opticalPower = Double.parseDouble(dataStr[0]);
        }
        catch (NumberFormatException nfe) {
            opticalPower = opticalPowerLast;
        }

        opticalPowerLast = opticalPower;

        if(shouldZero) {
            opticalPowerZero = -(opticalPower);
            shouldZero = false;
        }

        // add offset
        opticalPower += opticalPowerZero;

        switch(units) {
            // Lux
            case SensorConst.LIGHT_UNIT_LUX:
                opticalPower *= 6.83;
                break;
        }

        dataStr[0] = String.format(Locale.US,"%.2f",opticalPower);
        sensorStringArray = dataStr;

        return dataStr;
    }

    @Override
    public String toString() {
        if(sensorStringArray != null) {
            String unitString = unitList.get(units);
            return "Optical Power\n" +
                    sensorStringArray[0] + unitString;
        }
        else {
            return "NULL";
        }
    }

    @Override
    public SensorReading getGraphData() {
        SensorReading sensorReading= new SensorReading(this.getSensorTime());
        sensorReading.addGraphData((float)opticalPower);
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
        opticalPowerZero = 0;
        shouldZero = false;
    }

    @Override
    public String getSensorOffString() {
        return "Light Sensor - OFF";
    }

    @Override
    public int getSensorType() {return SensorConst.LIGHT_OPT3002; }
}
