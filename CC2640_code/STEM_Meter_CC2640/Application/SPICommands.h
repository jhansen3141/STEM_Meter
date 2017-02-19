/*
 * SPICommands.h
 *
 *  Created on: Nov 11, 2016
 *      Author: Josh
 */

#ifndef SPICOMMANDS_H_
#define SPICOMMANDS_H_

typedef enum {
  UPDATE_BAT_VALUES_MSG = 0,
  SENSOR_UPDATE_CONFIG_MSG,
  TOGGLE_SD_MOUNT_MSG

} spiCommands_msg_types_t;


#define STEMMETER_SERVICE_SENSOR1DATA_UUID 0xBEAA
#define STEMMETER_SERVICE_SENSOR2DATA_UUID 0xBEAB
#define STEMMETER_SERVICE_SENSOR3DATA_UUID 0xBEAC
#define STEMMETER_SERVICE_SENSOR4DATA_UUID 0xBEAD
#define STEMMETER_SERVICE_BATTERYDATA_UUID 0xBEBC

extern void user_enqueueRawSPICommandsMsg(spiCommands_msg_types_t deviceMsgType, uint8_t *pData, uint16_t len);
extern void enqueueSPICommandstTaskMsg(spiCommands_msg_types_t msgType);

#endif /* SPICOMMANDS_H_ */
