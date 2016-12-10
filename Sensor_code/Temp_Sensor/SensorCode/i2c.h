/*
 * i2c.h
 *
 * Created: 11/3/2016 12:14:07 PM
 *  Author: Josh
 */ 


#ifndef I2C_H_
#define I2C_H_
#include <stdio.h>

void I2CInit(void);
void I2CStart(void);
void I2CStop(void);
void I2CWrite(uint8_t u8data);
uint8_t I2CReadACK(void);
uint8_t I2CReadNACK(void);
uint8_t I2CGetStatus(void);
void I2CReadMult(uint8_t address, uint8_t reg, uint8_t *data, uint8_t len);

#endif /* I2C_H_ */