/*
* Author: Josh Hansen
* Project: STEM-Meter Base Unit
* Last Updated: April. 4, 2017
* File: Sensor.h
* Desc: Header File
*/

#ifndef SENSOR_H_
#define SENSOR_H_

#include <stdbool.h>

#define SENSOR_BAUD_RATE 	500000
#define SENSOR_FRAME_LENGTH 69
#define SENSOR_DATA_LENGTH 	20
#define FRAME_BYTES_OFFSET 	3
#define STR_BYTES_OFFSET 	23
#define SD_CARD_DATA_LEN	46
#define STR_NAME_OFFSET		1
#define STR_META_DATA_LEN	16

#define FRAME_BYTE_0 0x55
#define FRAME_BYTE_1 0xAA
#define FRAME_BYTE_2 0xA5

#define STR_FRAME_BYTE_0 0x81
#define STR_FRAME_BYTE_1 0x66
#define STR_FRAME_BYTE_2 0x77

extern bool Sensor1SDWriteEnabled;
extern bool Sensor2SDWriteEnabled;
extern bool Sensor3SDWriteEnabled;
extern bool Sensor4SDWriteEnabled;

extern void Sensor1WriteConfig(uint8_t freq);
extern void Sensor2WriteConfig(uint8_t freq);
extern void Sensor3WriteConfig(uint8_t freq);
extern void Sensor4WriteConfig(uint8_t freq);
extern void Sensor1RequestStr();
extern void Sensor2RequestStr();
extern void Sensor3RequestStr();
extern void Sensor4RequestStr();

#endif /* SENSOR_H_ */
