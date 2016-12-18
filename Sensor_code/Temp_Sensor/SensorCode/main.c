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

static uint32_t secondCounter = 0;
static uint32_t minuteCounter = 0;
sensorRate_t sensorRate = RATE_ONE_HZ;

void setSensorFreq(int arg_cnt, char **args);

ISR(USART_RX_vect) {
	cmd_handler(UDR0);	
}

ISR(TIMER1_COMPA_vect) {
	uint8_t shouldRead = FALSE;
	// there are 5 rates that are based on one second intervals
	if(sensorRate == RATE_ONE_MIN ||
	sensorRate == RATE_TEN_MIN ||
	sensorRate == RATE_THRITY_MIN ||
	sensorRate == RATE_ONE_HOUR ||
	sensorRate == RATE_ONE_HZ)
	{
		secondCounter++;
		if(secondCounter == 60) {
			secondCounter = 0;
			minuteCounter++;
		}
	}
	
	switch (sensorRate) {
		case RATE_ONE_MIN:
		if(secondCounter == 0) {
			shouldRead = TRUE;
		}
		break;
		
		case RATE_TEN_MIN:
		if(((minuteCounter % 10) == 0) && (minuteCounter != 0)) {
			UARTWriteString("MOD10\n");
			shouldRead = TRUE;
		}
		break;
		
		case RATE_THRITY_MIN:
		if(((minuteCounter % 30) == 0) && (minuteCounter != 0)) {
			shouldRead = TRUE;
		}
		break;
		
		case RATE_ONE_HOUR:
		if(((minuteCounter % 60) == 0) && (minuteCounter != 0)) {
			shouldRead = TRUE;
		}
		break;
		default:
		shouldRead = TRUE;
		break;
	}
	
	if(shouldRead) {
		sensorData_t data;
		readSensor(&data);
		writeBaseUnitData(&data);
	}
}

void setSensorFreq(int arg_cnt, char **args) {
	uint8_t freqCommand = atoi(args[1]);
	TCCR1B = 0; // turn timer off
	TCNT1 = 0; // reset count
	switch (freqCommand) {
		case RATE_OFF:
		return;
		break;
		case RATE_TEN_HZ:
		OCR1A = TIMER_TEN_HZ_NUM;
		sensorRate = RATE_TEN_HZ;
		break;
		
		case RATE_FIVE_HZ:
		OCR1A = TIMER_FIVE_HZ_NUM;
		sensorRate = RATE_FIVE_HZ;
		break;
		
		case RATE_ONE_HZ:
		OCR1A = TIMER_ONE_HZ_NUM;
		sensorRate = RATE_ONE_HZ;
		break;
		
		case RATE_ONE_MIN:
		OCR1A = TIMER_ONE_HZ_NUM;
		sensorRate = RATE_ONE_MIN;
		break;
		
		case RATE_TEN_MIN:
		OCR1A = TIMER_ONE_HZ_NUM;
		sensorRate = RATE_TEN_MIN;
		break;
		
		case RATE_THRITY_MIN:
		OCR1A = TIMER_ONE_HZ_NUM;
		sensorRate = RATE_THRITY_MIN;
		break;
		
		case RATE_ONE_HOUR:
		OCR1A = TIMER_ONE_HZ_NUM;
		sensorRate = RATE_ONE_HOUR;
		break;
		
	}
	secondCounter = 0;
	minuteCounter = 0;
	
	// turn timer back on
	TCCR1B|=(1<<WGM12); // Timer1 in CTC mode
	TIMSK1|=(1<<OCIE1A); // Enable Timer1, CTC Compare A interrupt
	TCCR1B|=(1<<CS10) | (1<<CS12); // Enable Timer1 with prescaler of F_CPU/1024 (128uS / tick)
}

int main(void) {
	_delay_ms(100);
	initBoard();
	_delay_ms(100);
	initSensor();
	cmdInit();
	cmdAdd("SF",setSensorFreq);
	_delay_ms(500);
	// enable global interrupts
	sei();
	
    while(1);
}