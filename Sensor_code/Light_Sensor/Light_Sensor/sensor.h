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

// Sensor Specific Register Addresses
#define SENSOR_I2C_ADDRESS 0x44

#define RESULT_REG		0x00
#define CONFIG_REG		0x01
#define LOW_LIMIT_REG	0x02
#define HIGH_LIMIT_REG	0x03
#define MF_ID			0x7E

#define AUTO_RANGE_BM	0xC000
#define CONT_CONV_BM	0x0600
#define ONE_SHOT_BM		0x0200



void initBoard(void);
void initSensor(void);
void readSensor(sensorData_t * data);
void moduleLED(ledState_t state);

#endif /* SENSOR_H_ */