/*
 * sensor.h
 *
 * Created: 11/3/2016 12:15:27 PM
 *  Author: Josh
 */ 


#ifndef SENSOR_H_
#define SENSOR_H_
#include "sensorCommon.h"

#define SENSOR_NUMBER IMU_MPU6050

// Sensor Specific Register Addresses
#define MPU6050_ADDRESS 0x68
#define MPU6050_WRITE_ADDRESS 0xD0
#define MPU6050_READ_ADDRESS 0xD1
#define ACCEL_XOUT_H 0x3B
#define ACCEL_XOUT_L 0x3C
#define ACCEL_YOUT_H 0x3D
#define ACCEL_YOUT_L 0x3E
#define ACCEL_ZOUT_H 0x3F
#define ACCEL_ZOUT_L 0x40
#define GYRO_XOUT_H 0x43
#define GYRO_XOUT_L 0x44
#define GYRO_YOUT_H 0x45
#define GYRO_YOUT_L 0x46
#define GYRO_ZOUT_H 0x47
#define GYRO_ZOUT_L 0x48
#define WHO_AM_I 0x75
#define PWR_MGMT_1 0x6B
#define PWR_MGMT_2 0x6C
#define SMPRT_DIV 0x19 // Sample Rate Divider
#define MPU_CONFIG 0x1A
#define GYRO_CONFIG 0x1B
#define ACCEL_CONFIG 0x1C
#define MOT_THR 0x1F
#define FIFO_EN 0x23
#define I2C_MST_CTRL 0x24
#define ACCEL_SENSE 16384.0f
#define GYRO_SENSE 131.0f


// Sensor Specific Byte Locations
#define ACCEL_X_HIGH_BYTE 0
#define ACCEL_X_LOW_BYTE 1

#define ACCEL_Y_HIGH_BYTE 2
#define ACCEL_Y_LOW_BYTE 3

#define ACCEL_Z_HIGH_BYTE 4
#define ACCEL_Z_LOW_BYTE 5

#define GYRO_X_HIGH_BYTE 8
#define GYRO_X_LOW_BYTE 9

#define GYRO_Y_HIGH_BYTE 10
#define GYRO_Y_LOW_BYTE 11

#define GYRO_Z_HIGH_BYTE 12
#define GYRO_Z_LOW_BYTE 13

void initBoard(void);
void initSensor(void);
void readSensor(sensorData_t * data);
void moduleLED(ledState_t state);


#endif /* SENSOR_H_ */