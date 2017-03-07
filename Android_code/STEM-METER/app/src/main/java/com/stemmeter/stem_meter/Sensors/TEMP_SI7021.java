package com.stemmeter.stem_meter.Sensors;

import com.stemmeter.stem_meter.GraphSettings;
import com.stemmeter.stem_meter.SensorConst;

import java.util.ArrayList;

/**
 * Created by Josh on 2/19/2017.
 */
public class TEMP_SI7021 extends Sensor {
    private String[] sensorStringArray;
    private float temp, humidity;

    private float tempZero = 0;
    private float humidityZero = 0;

    private boolean shouldZero = false;

    private GraphSettings graphSettings;
    private int units = SensorConst.TEMP_UNIT_C;
    private ArrayList<String> unitList;
    private ArrayList<String> dataPointList;

    public TEMP_SI7021(byte[] data, int sensorPosition) {
        super(data, sensorPosition,2);

        unitList = new ArrayList<>();
        dataPointList = new ArrayList<>();

        unitList.add("°C");
        unitList.add("°F");

        dataPointList.add("Temp");
        dataPointList.add("Humidity");

        graphSettings = new GraphSettings(unitList,dataPointList);
    }

    @Override
    public String[] calcSensorData() {

        String[] dataStr = new String[2];

        short humidityRaw = (short)( ( (data[5] & 0xFF) <<8 ) | (data[6] & 0xFF) );
        short tempRaw = (short)((data[7]<<8) | (data[8] & 0xFF));

        humidity = ( ( 125.0f*(float)humidityRaw ) / 65536.0f ) - 6.0f;

        if(humidity < 0) {
            humidity = 0;
        }
        else if(humidity > 100) {
            humidity = 100;
        }
        temp = ((175.72f*(float)tempRaw) / 65536.0f) - 46.85f;

        if(shouldZero) {
            tempZero = -(temp);
            humidityZero = -(humidity);
            shouldZero = false;
        }

        temp += tempZero;
         humidity += -(humidityZero);

        switch(units) {
            case SensorConst.TEMP_UNIT_F:
                temp = ((temp * 1.8f) + 32.0f);
                break;
        }

        dataStr[0] = String.format(java.util.Locale.US,"%.2f",temp);
        dataStr[1] = String.format(java.util.Locale.US,"%.2f",humidity);

        sensorStringArray = dataStr;
        return dataStr;
    }

    @Override
    public ArrayList<Float> getGraphData() {
        ArrayList<Float> graphData = new ArrayList<>();
        graphData.add(temp);
        graphData.add(humidity);
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
    public void zeroSensor() {
        shouldZero = true;
    }

    @Override
    public void resetZero() {
        tempZero = 0;
        humidityZero = 0;

        shouldZero = false;
    }

    @Override
    public String toString() {
        // TODO add units for humidity
        String unitsString = unitList.get(units);
        return "Temperature: " + sensorStringArray[0] + unitsString + "\n" +
                "Humidity: " + sensorStringArray[1] + "% RH";
    }
}
