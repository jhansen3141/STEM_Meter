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



void initBoard(void);
void initSensor(void);
void readSensor(sensorData_t * data);
void moduleLED(ledState_t state);


#endif /* SENSOR_H_ */