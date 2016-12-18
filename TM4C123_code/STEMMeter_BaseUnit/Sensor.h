/*
 * Sensor1.h
 *
 *  Created on: Nov 15, 2016
 *      Author: Josh
 */

#ifndef SENSOR_H_
#define SENSOR_H_

#define SENSOR_BAUD_RATE 250000
#define SENSOR_FRAME_LENGTH 23
#define SENSOR_DATA_LENGTH 20
#define FRAME_BYTES_OFFSET 3

#define FRAME_BYTE_0 0x55
#define FRAME_BYTE_1 0xAA
#define FRAME_BYTE_2 0xA5

extern void Sensor1WriteConfig(uint8_t freq);
extern void Sensor2WriteConfig(uint8_t freq);
extern void Sensor3WriteConfig(uint8_t freq);
extern void Sensor4WriteConfig(uint8_t freq);

#endif /* SENSOR_H_ */
