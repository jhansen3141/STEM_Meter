#define F_CPU 8000000
#include <stdint.h>
#include <avr/io.h>
#include <math.h>
#include <stdlib.h>
#include <util/delay.h>
#include <string.h>
#include <stdio.h>
#include "sensor.h"
#include "sensorCommon.h"
#include "uart.h"
#include "spi.h"

#define THERM_ENABLE() PORTC &= ~(1<<4)
#define THERM_DISABLE() PORTC |= (1<<4)
#define DUMMY_BYTE 0

void initBoard(void) {
	DDRC |= (1<<0); // LED as output
	DDRC |= (1<<4); // Therm CS as output
	DDRD &= ~(1<<2); // UART Re-send line as input	
	UARTInit();
	SPIInit();
	OCR1A = TIMER_ONE_HZ_NUM;
	TCCR1B = 0; // turn timer off
	TCNT1 = 0; // reset count	
}

void initSensor(void) {
	_delay_ms(20);
	moduleLED(OFF);
}

void readTherm(MAX31855_Data *therm) {

	THERM_ENABLE(); // CS low
	_delay_us(1);
	therm->byte3 = spiRead(DUMMY_BYTE);
	therm->byte2 = spiRead(DUMMY_BYTE);
	therm->byte1 = spiRead(DUMMY_BYTE);
	therm->byte0 = spiRead(DUMMY_BYTE);
	_delay_us(1);
	THERM_DISABLE(); //CS high

	therm->rawData = ((therm->byte3<<24) | (therm->byte2<<16) | (therm->byte1<<8) | (therm->byte0));
	therm->fault = (therm->byte2 & 0x01);

	int32_t temp = therm->rawData;
	temp >>= 18;
	temp &= 0x3FFF;
	therm->tempC = ((float)temp * 0.25f);
}

void readSensor(sensorData_t *data) {
	MAX31855_Data thermData;
	char tempStr[5];
	uint8_t rawData[4];
	
	PORTC &= ~(1<<0);
	
	memset(tempStr,0,5);
	memset(data->sensorDataRaw,0,RAW_DATA_SIZE);
	memset(data->sensorDataStr,0,STR_DATA_SIZE);
	
	readTherm(&thermData);
	
	rawData[0] = thermData.byte0;
	rawData[1] = thermData.byte1;
	rawData[2] = thermData.byte2;
	rawData[3] = thermData.byte3;
	
	// copy the raw data into the struct
	memcpy(data->sensorDataRaw,rawData,RAW_DATA_LOCAL_SIZE);

	sprintf(tempStr,"%.2f\n",thermData.tempC);
	strcpy(data->sensorDataStr,tempStr);
	
	PORTC |= (1<<0);
	
}

void moduleLED(ledState_t state) {
	if(state == OFF) {
		PORTC |= (1<<0);
	}
	else {
		PORTC &= ~(1<<0);
	}
}