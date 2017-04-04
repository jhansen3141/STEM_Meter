package com.stemmeter.stem_meter;

import com.stemmeter.stem_meter.Sensors.Accel_MPU6050;
import com.stemmeter.stem_meter.Sensors.Gyro_MPU6050;
import com.stemmeter.stem_meter.Sensors.LIGHT_OPT3002;
import com.stemmeter.stem_meter.Sensors.MAG_MAG3110;
import com.stemmeter.stem_meter.Sensors.PRESSURE_MPL3115A2;
import com.stemmeter.stem_meter.Sensors.Sensor;
import com.stemmeter.stem_meter.Sensors.TEMP_SI7021;
import com.stemmeter.stem_meter.Sensors.Temp_MCP9808;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Josh on 3/6/2017.
 */
public class BaseUnit {

    private BaseUnitBattery baseUnitBattery;

    ArrayList<Sensor> sensorList;

    public BaseUnit() {
        baseUnitBattery = new BaseUnitBattery();
        sensorList = new ArrayList<>();
        sensorList.add(null);
        sensorList.add(null);
        sensorList.add(null);
        sensorList.add(null);
    }

    public Sensor getSensor(final int sNum) {
        return sensorList.get(sNum-1);
    }

    public BaseUnitBattery getBaseUnitBattery() {
        return baseUnitBattery;
    }

    public Sensor updateSensorData(final int sNum, final byte sensorData[]) {

        Thread thread = new Thread() {
            @Override
            public void run() {
                if( !(sensorData[0] == SensorConst.INVALID_SENSOR) ) {
                    // check to see which sensor is connected
                    // sensor data type is held in first byte of sensor data
                    switch(sensorData[0]) {
                        case SensorConst.ACCEL_MPU6050:
                            if( !(sensorList.get(sNum-1) instanceof Accel_MPU6050 ) || (sensorList.get(sNum-1) == null)) {
                                sensorList.set( (sNum-1), new Accel_MPU6050(sensorData, sNum));
                            }
                            break;
                        case SensorConst.TEMP_MCP9808:
                            if( !(sensorList.get(sNum-1) instanceof Temp_MCP9808) || (sensorList.get(sNum-1) == null)) {
                                sensorList.set( (sNum-1), new Temp_MCP9808(sensorData, sNum));
                            }
                            break;
                        case SensorConst.GYRO_MPU6050:
                            if( !(sensorList.get(sNum-1) instanceof Gyro_MPU6050) || (sensorList.get(sNum-1) == null)) {
                                sensorList.set( (sNum-1), new Gyro_MPU6050(sensorData, sNum) );
                            }
                            break;
                        case SensorConst.LIGHT_OPT3002:
                            if( !(sensorList.get(sNum-1) instanceof LIGHT_OPT3002) || (sensorList.get(sNum-1) == null)) {
                                sensorList.set( (sNum-1), new LIGHT_OPT3002(sensorData, sNum));
                            }
                            break;
                        case SensorConst.MAG_MAG3110:
                            if( !(sensorList.get(sNum-1) instanceof MAG_MAG3110) || (sensorList.get(sNum-1) == null)) {
                                sensorList.set( (sNum-1), new MAG_MAG3110(sensorData, sNum));
                            }
                            break;
                        case SensorConst.PRESSURE_MPL3115A2:
                            if( !(sensorList.get(sNum-1) instanceof PRESSURE_MPL3115A2) || (sensorList.get(sNum-1) == null)) {
                                sensorList.set( (sNum-1), new PRESSURE_MPL3115A2(sensorData, sNum));
                            }
                            break;
                        case SensorConst.TEMP_SI7021:
                            if( !(sensorList.get(sNum-1) instanceof TEMP_SI7021) || (sensorList.get(sNum-1) == null)) {
                                sensorList.set( (sNum-1), new TEMP_SI7021(sensorData, sNum));
                            }
                            break;
                    }

                    sensorList.get(sNum-1).updateData(sensorData);
                    sensorList.get(sNum-1).calcSensorData();
                }
            }
        };

        thread.start();

        return  sensorList.get(sNum-1);
    }

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
                    "Temp: " + fTemp + "C \n" +
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
}
