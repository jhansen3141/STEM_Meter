#define F_CPU 8000000
#include <stdio.h>
#include <avr/io.h>
#include <util/delay.h>
#include "sensor.h"

// Used to keep readings in sync. Incremented after every reading
// Using 24-bits will overflow around 466 hours (19.4 days) at max interval of 10Hz
static uint32_t syncCount = 0;


void UARTInit(void) {
	UCSR0B |= (1<<TXEN0) | (1<<RXEN0);
	UCSR0C |= (1<<UCSZ01) | (1<<UCSZ00); // 8 bit frame, 1 stop bit, no parity
	UCSR0B |= (1 << RXCIE0); // enable RX interrupt
	UBRR0L = 1; // 250k baud
}

void UARTWrite(uint8_t data) {
	while (!(UCSR0A & (1<<UDRE0)));
	UDR0 = data;
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
	
	// write the sensor number so app knows what sensor this is
	UARTWrite(SENSOR_NUMBER);
	// write the rate readings are being taken
	UARTWrite(data->sensorRate);
	// write the sync number upper byte
	UARTWrite((syncCount>>16) & 0xFF);
	// write the sync number middle byte
	UARTWrite((syncCount>>8) & 0xFF);
	//write the sync number lower byte
	UARTWrite(syncCount & 0xFF);
	
	for(i=0;i<DATA_SIZE;i++) {
		// Write all of the data
		UARTWrite(data->sensorData[i]);
	}
	
	// increment sync number because we just sent a reading
	syncCount++;	
}