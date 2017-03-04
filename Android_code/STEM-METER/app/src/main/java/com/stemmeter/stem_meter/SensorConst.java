package com.stemmeter.stem_meter;

import android.graphics.Color;

/**
 * Created by Josh on 11/28/2016.
 */

public final class SensorConst {
    public static final int SENSOR_1 = 1;
    public static final int SENSOR_2 = 2;
    public static final int SENSOR_3 = 3;
    public static final int SENSOR_4 = 4;

    public static final int INVALID_SENSOR = 0;
    public static final int TEMP_MCP9808 = 1;
    public static final int ACCEL_MPU6050 = 2;
    public static final int GYRO_MPU6050 = 3;
    public static final int PRESSURE_MPL3115A2 = 4;
    public static final int LIGHT_OPT3002 = 5;
    public static final int MAG_MAG3110 = 6;
    public static final int TEMP_SI7021 = 7;

    public static final int RATE_OFF = 0;
    public static final int RATE_TEN_HZ = 1;
    public static final int RATE_FIVE_HZ = 2;
    public static final int RATE_ONE_SEC = 3;
    public static final int RATE_FIVE_SEC = 4;
    public static final int RATE_TEN_SEC = 5;
    public static final int RATE_THIRTY_SEC = 6;
    public static final int RATE_ONE_MIN = 7;
    public static final int RATE_TEN_MIN = 8;
    public static final int RATE_THIRTY_MIN = 9;
    public static final int RATE_ONE_HOUR = 10;

    public static final int ACCEL_UNIT_G = 0;
    public static final int ACCEL_UNIT_MS = 1;
    public static final int ACCEL_UNIT_FS = 2;
    public static final int ACCEL_DP_X = 0;
    public static final int ACCEL_DP_Y = 1;
    public static final int ACCEL_DP_Z = 2;

    public static final int GYRO_UNIT_DS = 0; // degree/s
    public static final int GYRO_UNIT_RS = 1; // rads/s

    public static final int GYRO_DP_X = 0;
    public static final int GYRO_DP_Y = 1;
    public static final int GYRO_DP_Z = 2;

    public static final int LIGHT_UNIT_UW = 0; // uW/cm^2
    public static final int LIGHT_UNIT_LUX = 1; // Lux

    public static final int LIGHT_DP_ONLY = 0;

    public static final int MAG_UNIT_T = 0; // Telsa

    public static final int MAG_DP_X = 0;
    public static final int MAG_DP_Y = 1;
    public static final int MAG_DP_Z = 2;

    public static final int TEMP_UNIT_C = 0;
    public static final int TEMP_UNIT_F = 1;

    public static final int TEMP_DP_T = 0;  // Temperature
    public static final int TEMP_DP_H = 1; // Humidity

    public static final int PRESSURE_UNIT_PA = 0;
    public static final int PRESSURE_UNIT_HPA = 1;

    public static final int PRESSURE_DP_PRESS = 0;
    public static final int PRESSURE_DP_ALT = 1;

    public static final int SELECTION_COLOR = Color.rgb(66, 215, 244); // Light blue
}
