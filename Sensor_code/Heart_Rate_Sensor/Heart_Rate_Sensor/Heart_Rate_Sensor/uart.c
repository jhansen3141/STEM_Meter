#define F_CPU 8000000
#include <stdio.h>
#include <avr/io.h>
#include <util/delay.h>
#include <string.h>
#include "sensor.h"
#include "sensorCommon.h"


#define FRAME_BYTE_0 (0x55)
#define FRAME_BYTE_1 (0xAA)
#define FRAME_BYTE_2 (0xA5)


#define UARTWrite(data) {			\
	while (!(UCSR0A & (1<<UDRE0)));	\
	UDR0 = data;					\
}

// Used to keep readings in sync. Incremented after every reading
// Using 24-bits will overflow around 466 hours (19.4 days) at max interval of 10Hz
static uint32_t syncCount = 0;

void UARTInit(void) {
	UCSR0B |= (1<<TXEN0) | (1<<RXEN0);
	UCSR0C |= (1<<UCSZ01) | (1<<UCSZ00); // 8 bit frame, 1 stop bit, no parity
	UCSR0B |= (1 << RXCIE0); // enable RX interrupt
	UBRR0L = 0; // 500k baud
}


void UARTWriteString(char *string) {
	uint16_t counter = 0;
	char currentChar = string[counter++];
	
	while(currentChar != '\0') {
		UARTWrite(currentChar);
		currentChar = string[counter++];
	}
}

void writeBaseUnitData(sensorData_t *data) {
	uint8_t i;
	char sensorString[SENSOR_STR_LEN];
	
	memset(sensorString,0,SENSOR_STR_LEN);
	strcpy(sensorString,SENSOR_STRING);
	
	// Write the three frame sync bytes
	UARTWrite(FRAME_BYTE_0);
	UARTWrite(FRAME_BYTE_1);
	UARTWrite(FRAME_BYTE_2);
	// write the sensor number so app knows what sensor this is
	UARTWrite(SENSOR_NUMBER);
	// write the rate readings are being taken
	UARTWrite(sensorRate);
	// write the sync number upper byte
	UARTWrite(((syncCount>>16) & 0xFF));
	// write the sync number middle byte
	UARTWrite(((syncCount>>8) & 0xFF));
	//write the sync number lower byte
	UARTWrite((syncCount & 0xFF));
	
	for(i=0;i<RAW_DATA_SIZE;i++) {
		// Write all of the raw data
		UARTWrite((data->sensorDataRaw[i]));
	}
	// Write how many data points this sensor has
	UARTWrite(NUMBER_DATA_POINTS);
	
	// Write the sensor name string
	for(i=0;i<SENSOR_STR_LEN;i++) {
		// Write all of the string data
		UARTWrite((sensorString[i]));
	}
	
	for(i=0;i<STR_DATA_SIZE;i++) {
		// Write all of the string data
		UARTWrite((data->sensorDataStr[i]));
	}
	
	// increment sync number because we just sent a reading
	syncCount++;
}