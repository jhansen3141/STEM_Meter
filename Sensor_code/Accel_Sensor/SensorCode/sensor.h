/*
 * sensor.h
 *
 * Created: 11/3/2016 12:15:27 PM
 *  Author: Josh
 */ 


#ifndef SENSOR_H_
#define SENSOR_H_


#include "sensorCommon.h"

#define SENSOR_NUMBER IMU_ACCEL_MPU6050
#define SENSOR_DEFAULT_RATE RATE_ONE_HZ
#define NUMBER_DATA_POINTS	3
#define SENSOR_STRING "Accel,X,Y,Z"
#define SENSOR_STR_LEN	15


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
#define ACCEL_SENSE 2048.0f
#define GYRO_SENSE 16.384f

void initBoard(void);
void initSensor(void);
void readSensor(sensorData_t * data);
void moduleLED(ledState_t state);


#endif /* SENSOR_H_ */