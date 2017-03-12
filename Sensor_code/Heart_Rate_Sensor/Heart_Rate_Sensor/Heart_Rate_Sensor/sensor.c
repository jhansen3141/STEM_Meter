#define F_CPU 8000000
#include <stdint.h>
#include <avr/io.h>
#include <math.h>
#include <stdlib.h>
#include <util/delay.h>
#include <string.h>
#include "sensor.h"
#include "sensorCommon.h"
#include "i2c.h"
#include "uart.h"


void initBoard(void) {
	DDRC |= (1<<0); // LED as output
	DDRD &= ~(1<<2); // UART Re-send line as input
	
	I2CInit();
	UARTInit();
	OCR1A = TIMER_ONE_HZ_NUM;
	TCCR1B = 0; // turn timer off
	TCNT1 = 0; // reset count
}

void initSensor(void) {
	
	moduleLED(OFF);
}



void readSensor(sensorData_t *data) {
	/*
	uint8_t IMUData[6];
	char tempStr[6];
	int16_t accelX, accelY, accelZ;
	float fAccelX, fAccelY, fAccelZ;
	
	memset(data->sensorDataRaw,0,RAW_DATA_SIZE);
	memset(data->sensorDataStr,0,STR_DATA_SIZE);
	
	I2CReadMult(MPU6050_ADDRESS,ACCEL_XOUT_H,IMUData,6);
	
	// copy the raw data into the struct
	memcpy(data->sensorDataRaw,IMUData,6);

	// combine the bytes together
	accelX = ( ( (int16_t)IMUData[0]<<8 ) | IMUData[1] );
	accelY = ( ( (int16_t)IMUData[2]<<8 ) | IMUData[3] );
	accelZ = ( ( (int16_t)IMUData[4]<<8 ) | IMUData[5] );
	
	
	fAccelX = (float)accelX / ACCEL_SENSE;
	fAccelY = (float)accelY / ACCEL_SENSE;
	fAccelZ = (float)accelZ / ACCEL_SENSE;
	
	
	sprintf(tempStr,"%1.2f;",fAccelX);
	strcat(data->sensorDataStr,tempStr);
	
	sprintf(tempStr,"%1.2f;",fAccelY);
	strcat(data->sensorDataStr,tempStr);
	
	sprintf(tempStr,"%1.2f;",fAccelZ);
	strcat(data->sensorDataStr,tempStr);
	*/

}

void moduleLED(ledState_t state) {
	if(state == OFF) {
		PORTC |= (1<<0);
	}
	else {
		PORTC &= ~(1<<0);
	}
}