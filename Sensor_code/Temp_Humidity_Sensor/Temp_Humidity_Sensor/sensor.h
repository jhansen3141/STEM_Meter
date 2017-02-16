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
#define SENSOR_DEFAULT_RATE RATE_ONE_HZ
#define NUMBER_DATA_POINTS	(2)
#define SENSOR_STRING "Temp,Humidity"
#define SENSOR_STR_LEN	(15)

// Sensor Specific Register Addresses
#define SENSOR_I2C_ADDRESS 0x40

#define MSR_HUMD_HOLD		0xE5
#define MSR_HUMD_NO_HOLD	0xF5
#define MSR_TEMP_HOLD		0xE3
#define MSR_TEMP_NO_HOLD	0xF3
#define RD_TEMP_PAST_MSR	0xE0
#define RESET_CMD			0xFE


void initBoard(void);
void initSensor(void);
void readSensor(sensorData_t * data);
void moduleLED(ledState_t state);


#endif /* SENSOR_H_ */