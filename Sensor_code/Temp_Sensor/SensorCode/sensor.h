/*
 * sensor.h
 *
 * Created: 11/3/2016 12:15:27 PM
 *  Author: Josh
 */ 


#ifndef SENSOR_H_
#define SENSOR_H_
#include "sensorList.h"

#define SENSOR_NUMBER TEMP_MCP9808
#define TEMP_SENESOR_ADDRESS 0x18
#define TEMP_SENSEOR_TEMPATURE_REG_ADDR 0x05

void initBoard(void);
void initSensor(void);
void readSensor(sensorData_t *data);
void moduleLED(ledState_t state);


#endif /* SENSOR_H_ */