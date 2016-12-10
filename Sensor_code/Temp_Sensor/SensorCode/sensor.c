#include <stdint.h>
#include <avr/io.h>
#include <math.h>
#include <stdlib.h>
#include <string.h>
#include "sensor.h"
#include "sensorList.h"
#include "i2c.h"
#include "uart.h"


void initBoard(void) {
	DDRB |= (1<<0); // LED as output
	
	I2CInit();
	UARTInit();
	OCR1A = TIMER_FIVE_HZ_NUM;
	TCCR1B|=(1<<WGM12); // Timer1 in CTC mode
	TIMSK1|=(1<<OCIE1A); // Enable Timer1, CTC Compare A interrupt
	TCCR1B|=(1<<CS10) | (1<<CS12); // Enable Timer1 with prescaler of F_CPU/1024 (128uS / tick)
}

void initSensor(void) {
	moduleLED(OFF);
}


void readSensor(sensorData_t *data) {
	memset(data->sensorData,0,DATA_SIZE);

	I2CReadMult(TEMP_SENESOR_ADDRESS,TEMP_SENSEOR_TEMPATURE_REG_ADDR,data->sensorData,2);
	/*
	combinedData = ((uint16_t)tempData[0] << 8) | tempData[1];

	temperature = combinedData & 0x0FFF;
	temperature /=  16.0f;
	if (combinedData & 0x1000) {
		temperature -= 256;
	}
	temperature = temperature * 100.0f;
	*/
	
}

void moduleLED(ledState_t state) {
	if(state == OFF) {
		PORTB |= (1<<0);
	}
	else {
		PORTB &= ~(1<<0);
	}
}