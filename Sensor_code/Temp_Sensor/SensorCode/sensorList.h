/*
 * sensorList.h
 *
 * Created: 11/25/2016 1:53:24 PM
 *  Author: Josh
 */ 


#ifndef SENSORLIST_H_
#define SENSORLIST_H_

#define DATA_SIZE 19

#define TIMER_OFF_NUM 0
#define TIMER_ONE_HZ_NUM 7812
#define TIMER_FIVE_HZ_NUM 1562
#define TIMER_TEN_HZ_NUM 781

#define IMU_MPU6050 0
#define TEMP_MCP9808 1

typedef struct sensorData {
	uint8_t sensorData[DATA_SIZE];
}sensorData_t;

typedef enum {
	ON = 0,
	OFF
}ledState_t;

#endif /* SENSORLIST_H_ */