package com.stemmeter.stem_meter.Sensors;

import java.util.ArrayList;

/**
 * Created by Josh on 2/19/2017.
 */
public class TEMP_SI7021 extends Sensor {
    private String[] sensorStringArray;
    private float tempF, tempC, humidity;

    public TEMP_SI7021(byte[] data, int sensorPosition) {
        super(data, sensorPosition,3);
    }

    @Override
    public String[] calcSensorData() {

        String[] dataStr = new String[3];

        short humidityRaw = (short)((data[5]<<8)   | (data[6] & 0xFF));
        short tempRaw = (short)((data[7]<<8)   | (data[8] & 0xFF));

        humidity = ((125.0f*(float)humidityRaw) / 65536.0f) - 6.0f;
        tempC = ((175.72f*(float)tempRaw) / 65536.0f) - 46.85f;
        tempF = (float)((tempC * 1.8f) + 32.0f);

        dataStr[0] = String.format(java.util.Locale.US,"%.2f",tempC);
        dataStr[1] = String.format(java.util.Locale.US,"%.2f",tempF);
        dataStr[2] = String.format(java.util.Locale.US,"%.2f",humidity);

        sensorStringArray = dataStr;
        return dataStr;
    }

    @Override
    public ArrayList<Float> getGraphData() {
        ArrayList<Float> graphData = new ArrayList<>();
        graphData.add(tempC);
        graphData.add(tempF);
        graphData.add(humidity);
        return graphData;
    }

    @Override
    public String toString() {
        // TODO add units for humidity
        return "Temperature: " + sensorStringArray[0] + "\u00b0C\n" +
                "Temperature: " + sensorStringArray[1] + "\u00b0F\n" +
                "Humidity: " + sensorStringArray[2] + "\n";
    }
}
