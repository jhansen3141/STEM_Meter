/*
* Author: Josh Hansen
* Project: STEM-Meter Base Unit
* Last Updated: April. 4, 2017
* File: FatSD.h
* Desc: Header File
*/
#ifndef FATSD_H_
#define FATSD_H_

#include <stdbool.h>

extern bool SDMasterWriteEnabled;

typedef enum {
  WRITE_S1_TO_SD_MSG = 0,
  WRITE_S2_TO_SD_MSG,
  WRITE_S3_TO_SD_MSG,
  WRITE_S4_TO_SD_MSG,
  WRITE_SENSOR_STR_MSG,
  TOGGLE_SD_EN_MSG,
  SET_TIME_MSG
} SD_msg_types_t;

void enqueueSDTaskMsg(SD_msg_types_t msgType, uint8_t *buffer);
void enqueueSDToggleTaskMsg();
void enqueSDTaskTimeSetMsg(uint8_t *timeBuffer);
uint16_t SDWriteTime();


#endif /* FATSD_H_ */
