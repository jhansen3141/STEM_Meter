/*
* Author: Josh Hansen
* Project: STEM-Meter Base Unit
* Last Updated: April. 4, 2017
* File: BLEWrite.h
* Desc: Header File
*/

#ifndef BLEWRITE_H_
#define BLEWRITE_H_

typedef enum {
  SENSOR_1_UPDATE_DATA_MSG = 0,
  SENSOR_2_UPDATE_DATA_MSG,
  SENSOR_3_UPDATE_DATA_MSG,
  SENSOR_4_UPDATE_DATA_MSG,
  UPDATE_SENSOR_CONFIG_MSG,
  CHARGE_COMPLETE_MSG,
  CHARGE_NOT_COMPLETE_MSG,
  CHARGE_STARTED_MSG,
  CHARGE_STOPPED_MSG,
  SD_STATUS_LED_OFF_MSG,
  SD_STATUS_LED_ON_MSG
} bleWrite_msg_types_t;

void enqueueBLEWritetTaskMsg(bleWrite_msg_types_t msgType, uint8_t *buffer, uint16_t len);
void enqueueBLEMsg(bleWrite_msg_types_t msgType);

#endif /* BLEWRITE_H_ */
