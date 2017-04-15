#include <avr/io.h>
#include "spi.h"

#define SCK_HIGH() PORTB |= (1<<5)
#define SCK_LOW() PORTB &= ~(1<<5)

void SPIInit() {
	
	DDRB |= (1<<5); // SCK as output
	DDRB &= ~(1<<4); // SDI	
	
	/*		
	DDRB = ((1<<DDB2)|(1<<DDB1)|(1<<DDB0)); //spi pins on port b MOSI SCK,SS outputs
	DDRB &= ~(1<<3); // SDI	
	SPCR = (1<<SPE)|(1<<MSTR)|(1<<SPR0);
	*/
}

uint8_t spiRead(uint8_t data) {
	SCK_LOW();
	uint8_t counter;
	uint8_t dataIn = 0;
	for(counter=0;counter<8;counter++) {
		SCK_HIGH();
		dataIn = (dataIn << 1);
		dataIn |= ((PINB & 0x10) >> 4);
		SCK_LOW();
	}
	return dataIn;
}