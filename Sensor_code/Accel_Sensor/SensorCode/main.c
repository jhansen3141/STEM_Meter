#define F_CPU 8000000

#include <avr/io.h>
#include <util/delay.h>
#include <string.h>
#include <avr/interrupt.h>
#include <stdlib.h>
#include "sensor.h"
#include "sensorCommon.h"
#include "uart.h"
#include "cmd.h"


ISR(USART_RX_vect) {
	cmd_handler(UDR0);	
}

ISR(TIMER1_COMPA_vect) {
	sensorData_t data;
	readSensor(&data);
	writeBaseUnitData(&data);	
}


int main(void) {
	
	initBoard();
	initSensor();
	cmdInit();

	
	sei();
	
	
    while(1);
}