package com.stemmeter.stem_meter;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Josh on 1/22/2017.
 */

public class BaseUnitBattery {

    private float fVoltage;
    private float fCurrentDraw;
    private float fTemp;
    private float fPercentage;
    private String voltageStr;
    private String currentStr;
    private String tempStr;
    private String percentageStr;

    public BaseUnitBattery() {
        updateBatteryValues("0.0;0.0;0.0");
    }

    public void updateBatteryValues(String batteryValues) {
        List<String> strList = Arrays.asList(batteryValues.split(";[ ]*"));
        if(strList.size() == 3) {
            voltageStr = strList.get(0);
            currentStr = strList.get(1);
            tempStr = strList.get(2);

            fVoltage = Float.parseFloat(voltageStr);
            fCurrentDraw = Float.parseFloat(currentStr);
            fTemp = Float.parseFloat(tempStr);

            if(fVoltage <= 3.4) {
                fPercentage = (float)0.0;
            }
            else {
                fPercentage = (100.0f * (1.0f - (4.2f - fVoltage)));
            }

            percentageStr =  String.format(java.util.Locale.US,"%d",(int)fPercentage);
        }

    }

    public String getBatStr() {
        return "Voltage: " + voltageStr + "V \n" +
                "Current: " + currentStr + "mA \n" +
                "Percentage: " + percentageStr + "%";
    }

    public String getVoltageStr() {
        return voltageStr;
    }

    public String getCurrentStr() {
        return currentStr;
    }

    public String getTempStr() {
        return tempStr;
    }

    public String getPercentageStr() {
        return percentageStr;
    }
}

