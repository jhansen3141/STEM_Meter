package smDataViewer.model;

import java.util.ArrayList;

public class TEMP_MCP9808 extends Sensor {

    private String[] sensorStringArray;
    private float tempF, tempC;

    public TEMP_MCP9808(byte[] data) {
        super(data);
    }

    @Override
    String[] calcSensorData() {
        double temperature = 0;
        String[] dataStr = new String[2];
        short tempRaw = (short)((data[5]<<8)   | (data[6] & 0xFF));

        temperature = tempRaw & 0x0FFF;
        temperature /=  16.0f;
        if ((tempRaw & 0x1000) != 0) {
            temperature -= 256;
        }
        tempC = (float)temperature;
        tempF = (float)((temperature * 1.8) + 32.0);

        dataStr[0] = String.format(java.util.Locale.US,"%.2f",tempC);
        dataStr[1] = String.format(java.util.Locale.US,"%.2f",tempF);

        sensorStringArray = dataStr;
        return dataStr;
    }

    @Override
    ArrayList<Float> getGraphData() {
    	ArrayList<Float> graphData = new ArrayList<>();
    	graphData.add(tempF);
        return graphData;
    }

    @Override
    public String toString() {
        return "Temperature: " + sensorStringArray[1] + "\u00b0F";
    }

    public float getTempF() {
        return tempF;
    }

    public float getTempC() {
        return tempC;
    }

}
