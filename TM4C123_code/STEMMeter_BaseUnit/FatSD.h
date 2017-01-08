
#ifndef FATSD_H_
#define FATSD_H_

#include <stdbool.h>

extern bool SDMasterWriteEnabled;;

typedef enum {
  WRITE_TO_SD_MSG = 0,
} SD_msg_types_t;

void enqueueSDTaskMsg(SD_msg_types_t msgType, uint8_t *buffer, uint16_t len);
uint16_t SDWriteTime();


#endif /* FATSD_H_ */
