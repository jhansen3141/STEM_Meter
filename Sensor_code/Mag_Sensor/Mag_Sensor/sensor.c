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

static void MAG3110_Write(uint8_t reg, uint8_t data);

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
	MAG3110_Write(CTRL_REG2,AUTO_MRST_EN_BM);	
	MAG3110_Write(CTRL_REG1,(TWENTY_HZ_OVSMPL64 | ACTIVE_BM));
	moduleLED(OFF);
}

static void MAG3110_Write(uint8_t reg, uint8_t data) {
	I2CStart();
	I2CWrite(SENSOR_I2C_ADDRESS<<1);
	I2CWrite(reg);
	I2CWrite(data);
	I2CStop();
}


void MAG3110_Read(uint8_t reg, uint8_t *data) {
	I2CStart();
	// I2C Write
	I2CWrite(SENSOR_I2C_ADDRESS << 1);
	I2CWrite(reg);
	I2CStart();
	// I2C Read
	I2CWrite((SENSOR_I2C_ADDRESS << 1) | 0x01);
	*data = I2CReadNACK();
	I2CStop();
}

void readSensor(sensorData_t *data) {
	uint8_t MagData[6];
	char tempStr[6];
	int16_t magX, magY, magZ;
	float fMagX, fMagY, fMagZ;
	
	moduleLED(ON);
	
	memset(data->sensorDataRaw,0,RAW_DATA_SIZE);
	memset(data->sensorDataStr,0,STR_DATA_SIZE);
	
	I2CReadMult(SENSOR_I2C_ADDRESS,OUT_X_MSB_REG,MagData,6);
	
	
	// copy the raw data into the struct
	memcpy(data->sensorDataRaw,MagData,6);
	

	// combine the bytes together
	magX = ( ( (int16_t)MagData[0]<<8 ) | MagData[1] );
	magY = ( ( (int16_t)MagData[2]<<8 ) | MagData[3] );
	magZ = ( ( (int16_t)MagData[4]<<8 ) | MagData[5] );
	
	
	fMagX = (float)magX / MAG_SENSE;
	fMagY = (float)magY / MAG_SENSE;
	fMagZ = (float)magZ / MAG_SENSE;
	
	
	sprintf(tempStr,"%1.2f,",fMagX);
	strcat(data->sensorDataStr,tempStr);
	
	sprintf(tempStr,"%1.2f,",fMagY);
	strcat(data->sensorDataStr,tempStr);
	
	sprintf(tempStr,"%1.2f\n",fMagZ);
	strcat(data->sensorDataStr,tempStr);
	
	moduleLED(OFF);
}

void moduleLED(ledState_t state) {
	if(state == OFF) {
		PORTC |= (1<<0);
	}
	else {
		PORTC &= ~(1<<0);
	}
}