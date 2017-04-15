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
#define NUMBER_DATA_POINTS	(1)
#define SENSOR_STRING "Temp"
#define SENSOR_STR_LEN	(15)

typedef struct {
	float tempC;
	uint32_t rawData;
	uint32_t byte0;
	uint32_t byte1;
	uint32_t byte2;
	uint32_t byte3;
	uint8_t fault;
}MAX31855_Data;


void initBoard(void);
void initSensor(void);
void readSensor(sensorData_t * data);
void moduleLED(ledState_t state);


#endif /* SENSOR_H_ */