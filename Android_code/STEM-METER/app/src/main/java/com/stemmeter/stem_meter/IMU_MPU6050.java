package com.stemmeter.stem_meter;

import android.graphics.Color;

/**
 * Created by Josh on 11/28/2016.
 */

public class IMU_MPU6050 extends Sensor{

    private float ACCEL_SENSE = 2048.0f; // +-16g
    private float GYRO_SENSE = 16.384f; // +-2000 degress/s
    private int ACCEL_SENSE_COLOR = Color.BLUE;
    private String[] sensorStringArray;
    private float xAccelF,yAccelF,zAccelF,xGyroF,yGyroF,zGyroF;


    public IMU_MPU6050(byte[] data, int sensorPosition) {
        super(data, sensorPosition);
        this.setColor(ACCEL_SENSE_COLOR);
    }

    @Override
    String[] calcSensorData() {
        String[] dataStr = new String[6];
        short xAccel = (short)((data[5]<<8)   | (data[6] & 0xFF));
        short yAccel = (short)((data[7]<<8)   | (data[8] & 0xFF));
        short zAccel = (short)((data[9]<<8)   | (data[10] & 0xFF));
        short xGyro  = (short)((data[11]<<8)   | (data[12] & 0xFF));
        short yGyro  = (short)((data[13]<<8)   | (data[14] & 0xFF));
        short zGyro  = (short)((data[15]<<8)  | (data[16] & 0xFF));

        xAccelF = xAccel / ACCEL_SENSE;
        yAccelF = yAccel / ACCEL_SENSE;
        zAccelF = zAccel / ACCEL_SENSE;

        xGyroF = xGyro / GYRO_SENSE;
        yGyroF = yGyro / GYRO_SENSE;
        zGyroF = zGyro / GYRO_SENSE;

        dataStr[0] = String.format(java.util.Locale.US,"%.2f",xAccelF);
        dataStr[1] = String.format(java.util.Locale.US,"%.2f",yAccelF);
        dataStr[2] = String.format(java.util.Locale.US,"%.2f",zAccelF);

        dataStr[3] = String.format(java.util.Locale.US,"%.2f",xGyroF);
        dataStr[4] = String.format(java.util.Locale.US,"%.2f",yGyroF);
        dataStr[5] = String.format(java.util.Locale.US,"%.2f",zGyroF);

        sensorStringArray = dataStr;

        return dataStr;
    }

    @Override
        float getGraphData() {
            return xAccelF;
    }

    @Override
    public String toString() {
        if(sensorStringArray != null) {
            return "Acceleration     Rotation\n" +
                    " X:  " + sensorStringArray[0] + "g        \t\t" + sensorStringArray[3] + "\u00b0/s\n" +
                    " Y:  " + sensorStringArray[1] + "g        \t\t" + sensorStringArray[4] + "\u00b0/s\n" +
                    " Z:  " + sensorStringArray[2] + "g        \t\t" + sensorStringArray[5] + "\u00b0/s ";
        }
        else {
            return "NULL";
        }
    }

    public float getxAccelF() {
        return xAccelF;
    }

    public float getyAccelF() {
        return yAccelF;
    }

    public float getzAccelF() {
        return zAccelF;
    }

    public float getxGyroF() {
        return xGyroF;
    }

    public float getyGyroF() {
        return yGyroF;
    }

    public float getzGyroF() {
        return zGyroF;
    }

}
