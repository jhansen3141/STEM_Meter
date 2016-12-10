
#include "i2c.h"
#include <stdint.h>
#include <avr/io.h>


void I2CInit(void) {
	//400kHz
	TWSR = 0x00;
	TWBR = 0x0C;
	TWCR = (1<<TWEN);
}

void I2CStart(void) {
	TWCR = (1<<TWINT)|(1<<TWSTA)|(1<<TWEN);
	while ((TWCR & (1<<TWINT)) == 0);
}

void I2CStop(void) {
	TWCR = (1<<TWINT)|(1<<TWSTO)|(1<<TWEN);
}


void I2CWrite(uint8_t u8data) {
	TWDR = u8data;
	TWCR = (1<<TWINT)|(1<<TWEN);
	while ((TWCR & (1<<TWINT)) == 0);
}

uint8_t I2CReadACK(void) {
	TWCR = (1<<TWINT)|(1<<TWEN)|(1<<TWEA);
	while ((TWCR & (1<<TWINT)) == 0);
	return TWDR;
}

uint8_t I2CReadNACK(void) {
	TWCR = (1<<TWINT)|(1<<TWEN);
	while ((TWCR & (1<<TWINT)) == 0);
	return TWDR;
}

uint8_t I2CGetStatus(void) {
	uint8_t status;
	status = TWSR & 0xF8;
	return status;
}

void I2CReadMult(uint8_t address, uint8_t reg, uint8_t *data, uint8_t len) {
	uint8_t i;
	I2CStart();
	I2CWrite(address << 1);
	I2CWrite(reg);
	I2CStart();
	I2CWrite((address << 1) | 0x01);
	
	for(i=0;i<len-1;i++) {
		data[i] = I2CReadACK();
	}
	data[len-1] = I2CReadNACK();
	I2CStop();
}