/*
 * sensorCommon.h
 *
 * Created: 11/25/2016 1:53:24 PM
 *  Author: Josh
 */ 


#ifndef SENSORLIST_H_
#define SENSORLIST_H_

#define RAW_DATA_SIZE	(15)
#define STR_DATA_SIZE	(30)
#define UART_FRAME_SIZE (54)
#define RAW_DATA_LOCAL_SIZE 4

#define TIMER_OFF_NUM 0
#define TIMER_ONE_HZ_NUM 7812
#define TIMER_FIVE_HZ_NUM 1562
#define TIMER_TEN_HZ_NUM 781


typedef enum sensorType {
	INVALID = 0,
	TEMP_MCP9808,
	IMU_ACCEL_MPU6050,
	IMU_GYRO_MPU6050,
	AIR_PRES_MPL3115A2,
	LIGHT_OPT3002,
	MAG_MAG3110,
	TEMP_SI7021,
}sensorType_t;

#define TRUE 1
#define FALSE 0

typedef volatile enum {
	RATE_OFF = 0,
	RATE_TEN_HZ,
	RATE_FIVE_HZ,
	RATE_ONE_SEC,
	RATE_FIVE_SEC,
	RATE_TEN_SEC,
	RATE_THIRTY_SEC,
	RATE_ONE_MIN,
	RATE_TEN_MIN,
	RATE_THIRTY_MIN,
	RATE_ONE_HOUR
}sensorRate_t;

extern sensorRate_t sensorRate;

typedef struct sensorData {
	uint8_t sensorDataRaw[RAW_DATA_SIZE];
	char sensorDataStr[STR_DATA_SIZE];
}sensorData_t;

typedef enum {
	ON = 0,
	OFF
}ledState_t;

#endif /* SENSORLIST_H_ */