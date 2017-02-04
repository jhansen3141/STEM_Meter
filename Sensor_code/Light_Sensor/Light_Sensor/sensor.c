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

static void OPT3002_Write(uint8_t reg, uint16_t data);
static uint16_t OPT3002_Read(uint8_t reg);

void initBoard(void) {
	DDRC |= (1<<0); // LED as output
	DDRD &= ~(1<<2); // UART Re-send line as input
	
	I2CInit();
	UARTInit();
	OCR1A = TIMER_ONE_HZ_NUM;
	TCCR1B|=(1<<WGM12); // Timer1 in CTC mode
	TIMSK1|=(1<<OCIE1A); // Enable Timer1, CTC Compare A interrupt
	TCCR1B|=(1<<CS10) | (1<<CS12); // Enable Timer1 with prescaler of F_CPU/1024 (128uS / tick)
	
}

void initSensor(void) {
	// Start sensor with auto ranging and constant conversions enabled
	// at 100mS conversion rate
	OPT3002_Write(CONFIG_REG,(AUTO_RANGE_BM | CONT_CONV_BM));
	moduleLED(OFF);
}

static void OPT3002_Write(uint8_t reg, uint16_t data) {
	I2CStart();
	I2CWrite(SENSOR_I2C_ADDRESS<<1);
	I2CWrite(reg);
	I2CWrite((data>>8) & 0xFF);
	I2CWrite(data & 0xFF);
	I2CStop();
}

static uint16_t OPT3002_Read(uint8_t reg) {
	uint8_t lowByte, highByte;
	// Write the register to read from
	I2CStart();
	I2CWrite(SENSOR_I2C_ADDRESS<<1);
	I2CWrite(reg);
	I2CStop();
	
	// Read from the register
	I2CStart();
	I2CWrite((SENSOR_I2C_ADDRESS<<1) | 0x01);
	highByte = I2CReadACK();
	lowByte = I2CReadNACK();
	I2CStop();
	
	return ((uint16_t)highByte<<8) | lowByte;
}

void readSensor(sensorData_t *data) {
	uint16_t rawData;
	uint16_t exponent;
	uint16_t mantissa;
	float multiplier; 
	float fOpticalPower;
	char tempStr[6];

	memset(data->sensorDataRaw,0,RAW_DATA_SIZE);
	memset(data->sensorDataStr,0,STR_DATA_SIZE);
	
	rawData = OPT3002_Read(RESULT_REG);

	// copy the raw data into the struct
	memcpy(data->sensorDataRaw,&rawData,2);

	// Exponent is held in B15:B12
	exponent = (rawData >> 12) & 0x000F;
	
	// Mantissa is held in B11:B0
	mantissa = rawData & 0x0FFF;
	

	multiplier = (float)((uint16_t)(1<<exponent) * 1.2f);
	
	// optical power = 
	// 2^(B15:B12) * (B11:B0) * 1.2 nW/cm^2
	fOpticalPower = (float)mantissa * multiplier;
	
	sprintf(tempStr,"%.1f;",fOpticalPower);
	strcat(data->sensorDataStr,tempStr);
}

void moduleLED(ledState_t state) {
	if(state == OFF) {
		PORTC |= (1<<0);
	}
	else {
		PORTC &= ~(1<<0);
	}
}