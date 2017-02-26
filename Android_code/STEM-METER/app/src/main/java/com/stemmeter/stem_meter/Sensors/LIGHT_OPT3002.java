package com.stemmeter.stem_meter.Sensors;

import android.util.Log;

import com.stemmeter.stem_meter.GraphSettings;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/**
 * Created by Josh on 2/19/2017.
 */
public class LIGHT_OPT3002 extends Sensor {
    private String[] sensorStringArray;
    private float opticalPower;
    private String TAG = "Light OPT3002";

    public LIGHT_OPT3002(byte[] data, int sensorPosition) {
        super(data, sensorPosition,1);
    }

    @Override
    public String[] calcSensorData() {
        String[] dataStr = new String[1];

        byte[] byteString = new byte[15];

        for(int i=0;i<15;i++) {
            if( (data[5 + i] == 0) || (data[5 + i] == '\n')) {
                break;
            }
            byteString[i] = data[5 + i];
        }

        try {
            dataStr[0] =  new String(byteString, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Log.i(TAG,"String format incorrect");
        }

        opticalPower = Float.parseFloat(dataStr[0]);

        sensorStringArray = dataStr;

        return dataStr;
    }

    @Override
    public String toString() {
        if(sensorStringArray != null) {
            return "Optical Power\n" +
                    sensorStringArray[0] + "uW/cm" +  "\u00B2";
        }
        else {
            return "NULL";
        }
    }

    @Override
    public ArrayList<Float> getGraphData() {
        ArrayList<Float> graphData = new ArrayList<>();
        graphData.add(opticalPower);
        return graphData;
    }

    @Override
    public GraphSettings getGraphSettings() {
        return null;
    }

    @Override
    public void setGraphUnits(int units) {

    }
}
