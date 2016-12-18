/*
 * sensorCommon.h
 *
 * Created: 11/25/2016 1:53:24 PM
 *  Author: Josh
 */ 


#ifndef SENSORLIST_H_
#define SENSORLIST_H_

#define DATA_SIZE 15

#define TIMER_OFF_NUM 0
#define TIMER_ONE_HZ_NUM 7812
#define TIMER_FIVE_HZ_NUM 1562
#define TIMER_TEN_HZ_NUM 781

#define IMU_MPU6050 2
#define TEMP_MCP9808 1

#define TRUE 1
#define FALSE 0

typedef enum {
	RATE_OFF = 0,
	RATE_TEN_HZ,
	RATE_FIVE_HZ,
	RATE_ONE_HZ,
	RATE_ONE_MIN,
	RATE_TEN_MIN,
	RATE_THRITY_MIN,
	RATE_ONE_HOUR
}sensorRate_t;

extern sensorRate_t sensorRate;

typedef struct sensorData {
	uint8_t sensorData[DATA_SIZE];
}sensorData_t;

typedef enum {
	ON = 0,
	OFF
}ledState_t;

#endif /* SENSORLIST_H_ */