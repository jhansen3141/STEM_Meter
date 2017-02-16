/*
 * sensor.h
 *
 * Created: 11/3/2016 12:15:27 PM
 *  Author: Josh
 */ 


#ifndef SENSOR_H_
#define SENSOR_H_


#include "sensorCommon.h"

#define SENSOR_NUMBER MAG_MAG3110
#define SENSOR_DEFAULT_RATE RATE_ONE_HZ
#define NUMBER_DATA_POINTS	(3)
#define SENSOR_STRING "M:X,M:Y,M:Z"
#define SENSOR_STR_LEN	(15)

// Sensor Specific Register Addresses
#define SENSOR_I2C_ADDRESS	0x0E
#define DR_STATUS_REG		0x00
#define OUT_X_MSB_REG		0x01
#define OUT_X_LSB_REG		0x02
#define OUT_Y_MSB_REG		0x03
#define OUT_Y_LSB_REG		0x04
#define OUT_Z_MSB_REG		0x05
#define OUT_Z_LSB_REG		0x06
#define WHO_AM_I_REG		0x07
#define CTRL_REG1			0x10
#define CTRL_REG2			0x11

#define MAG_SENSE			10.0f

#define AUTO_MRST_EN_BM		0x80
#define RAW_BM				0x40
#define MAG_RST_BM			0x10
#define FOURTY_HZ_OVSMPL32	0x08
#define TWENTY_HZ_OVSMPL64	0x10
#define ACTIVE_BM			0x01


void initBoard(void);
void initSensor(void);
void readSensor(sensorData_t * data);
void moduleLED(ledState_t state);
void MAG3110_Read(uint8_t reg, uint8_t *data);


#endif /* SENSOR_H_ */