/*
 * sensor.h
 *
 * Created: 11/3/2016 12:15:27 PM
 *  Author: Josh
 */ 


#ifndef SENSOR_H_
#define SENSOR_H_


#include "sensorCommon.h"

#define SENSOR_NUMBER AIR_PRES_MPL3115A2
#define SENSOR_DEFAULT_RATE RATE_ONE_HZ
#define NUMBER_DATA_POINTS	1

// Sensor Specific Register Addresses
#define SENSOR_I2C_ADDRESS		0x60

#define STATUS_REG				0x00
#define OUT_P_MSB_REG			0x01
#define OUT_P_CSB_REG			0x02
#define OUT_P_LSB_REG			0x03
#define OUT_T_MSB_REG			0x04
#define OUT_T_LSB_REG			0x05
#define DR_STATUS_REG			0x06
#define OUT_P_DELTA_MSB_REG		0x07
#define OUT_P_DELTA_CSB_REG		0x08
#define OUT_P_DELTA_LSB_REG		0x09
#define OUT_T_DELTA_MSB_REG		0x0A
#define OUT_T_DELTA_LSB_REG		0x0B
#define WHO_AM_I_REG			0x0C
#define F_STATUS_REG			0x0D
#define F_DATA_REG				0x0E
#define F_SETUP_REG				0x0F
#define TIME_DLY_REG			0x10
#define SYSMOD_REG				0x11
#define INT_SOURCE_REG			0x12
#define PT_DATA_CFG_REG			0x13
#define BAR_IN_MSB_REG			0x14
#define BAR_IN_LSB_REG			0x15
#define P_TGT_MSB_REG			0x16
#define P_TGT_LSB_REG			0x17
#define T_TGT_REG				0x18
#define P_WND_MSB_REG			0x19
#define P_WND_LSB_REG			0x1A
#define T_WND_REG				0x1B
#define P_MIN_MSB_REG			0x1C
#define P_MIN_CSB_REG			0x1D
#define P_MIN_LSB_REG			0x1E
#define T_MIN_MSB_REG			0x1F
#define T_MIN_LSB_REG			0x20
#define P_MAX_MSB_REG			0x21
#define P_MAX_CSB_REG			0x22
#define P_MAX_LSB_REG			0x23
#define T_MAX_MSB_REG			0x24
#define T_MAX_LSB_REG			0x25
#define CTRL_REG1				0x26
#define CTRL_REG2				0x27
#define CTRL_REG3				0x28
#define CTRL_REG4				0x29
#define CTRL_REG5				0x2A
#define OFF_P_REG				0x2B
#define OFF_T_REG				0x2C
#define OFF_H_REG				0x2D


void initBoard(void);
void initSensor(void);
void readSensor(sensorData_t * data);
void moduleLED(ledState_t state);


#endif /* SENSOR_H_ */