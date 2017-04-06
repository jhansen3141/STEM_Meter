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
    private int units1 = SensorConst.TEMP_UNIT_C;
    private int units2 = 0;
    private ArrayList<String> unitList1;
    private ArrayList<String> unitList2;
    private ArrayList<String> dataPointList;

    public TEMP_SI7021(byte[] data, int sensorPosition) {
        super(data, sensorPosition,2);

        unitList1 = new ArrayList<>();
        unitList2 = new ArrayList<>();
        dataPointList = new ArrayList<>();

        unitList1.add("°C");
        unitList1.add("°F");

        unitList2.add("% RH");

        dataPointList.add("Temp");
        dataPointList.add("Humidity");

        graphSettings = new GraphSettings(unitList1, unitList2, dataPointList);
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
        humidity += humidityZero;

        switch(units1) {
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
    public SensorReading getGraphData() {
        SensorReading sensorReading= new SensorReading(this.getSensorTime());
        sensorReading.addGraphData(temp);
        sensorReading.addGraphData(humidity);
        return sensorReading;
    }

    @Override
    public GraphSettings getGraphSettings() {
        return graphSettings;
    }

    @Override
    public void setGraphUnits1(int units) {
        this.units1 = units;
    }

    @Override
    public void setGraphUnits2(int units) {
        this.units2 = units;
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
    public String getSensorOffString() {
        return "Temperature Sensor - OFF";
    }

    @Override
    public String toString() {
        if(this.getSensorRate() == SensorConst.RATE_OFF ||
                this.getSensorRate() == SensorConst.RATE_INFO ) {
            return this.getSensorOffString();
        }

        String unitsString1 = unitList1.get(units1);
        String unitsString2 = unitList2.get(units2);
        return "Temperature: " + sensorStringArray[0] + unitsString1 + "\n" +
                "Humidity: " + sensorStringArray[1] + unitsString2;
    }

    @Override
    public int getSensorType() {return SensorConst.TEMP_SI7021; }

    @Override
    public String getSensorName() { return "Temperature"; }
}
