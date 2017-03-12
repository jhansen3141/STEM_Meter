/*
 * sensor.h
 *
 * Created: 11/3/2016 12:15:27 PM
 *  Author: Josh
 */ 


#ifndef SENSOR_H_
#define SENSOR_H_


#include "sensorCommon.h"

#define SENSOR_NUMBER TEMP_SI7021
#define NUMBER_DATA_POINTS	(2)
#define SENSOR_STRING "HR,SO2"
#define SENSOR_STR_LEN	(15)

// Sensor Specific Register Addresses
#define SENSOR_I2C_ADDRESS 0x40

#define INT_STATUS_REG	0x00
#define INT_ENABLE_REG	0x01
#define FIFO_REG		0x02
#define MODE_CONFIG_REG 0x06
#define SP02_CONFIG_REG	0x07
#define LED_CONFIG_REG	0x09
#define TEMP_REG		0x16




void initBoard(void);
void initSensor(void);
void readSensor(sensorData_t * data);
void moduleLED(ledState_t state);


#endif /* SENSOR_H_ */