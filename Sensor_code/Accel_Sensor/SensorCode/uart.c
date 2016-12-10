#define F_CPU 8000000
#include <stdio.h>
#include <avr/io.h>
#include <util/delay.h>
#include "sensor.h"


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
	UARTWrite(SENSOR_NUMBER);
	for(i=0;i<DATA_SIZE;i++) {
		UARTWrite(data->sensorData[i]);
	}	
}