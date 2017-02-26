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

static void Si7021_Write(uint8_t reg, uint8_t data);
static void Si7021_Read(uint8_t address, uint8_t reg, uint8_t *data);

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
	// reset the sensor
	Si7021_Write(RESET_CMD,0);
	_delay_ms(20);
	moduleLED(OFF);
}

static void Si7021_Write(uint8_t reg, uint8_t data) {
	I2CStart();
	I2CWrite(SENSOR_I2C_ADDRESS);
	I2CWrite(reg);
	I2CWrite(data);
	I2CStop();
}

static void Si7021_Read(uint8_t address, uint8_t cmd, uint8_t *data) {

	I2CStart();
	I2CWrite((address << 1) & 0xFE);
	I2CWrite(cmd);
	I2CStart();
	I2CWrite((address << 1) | 0x01);
	_delay_us(10);
	
	data[0] = I2CReadACK();

	data[1] = I2CReadNACK();
	I2CStop();
}

void readSensor(sensorData_t *data) {
	PORTC &= ~(1<<0);
	uint8_t RDSensorData[2];
	uint8_t rawData[4];
	char tempStr[10];
	int16_t temp, humidity;
	float fTemp, fHumidity;
	
	memset(data->sensorDataRaw,0,RAW_DATA_SIZE);
	memset(data->sensorDataStr,0,STR_DATA_SIZE);
	
	Si7021_Read(SENSOR_I2C_ADDRESS,MSR_HUMD_HOLD,RDSensorData);
	humidity = ( ( (int16_t)RDSensorData[0]<<8 ) | RDSensorData[1] );
	rawData[0] = RDSensorData[0];
	rawData[1] = RDSensorData[1];

	Si7021_Read(SENSOR_I2C_ADDRESS,RD_TEMP_PAST_MSR,RDSensorData);
	temp = ( ( (int16_t)RDSensorData[0]<<8 ) | RDSensorData[1] );
	rawData[2] = RDSensorData[0];
	rawData[3] = RDSensorData[1];
	
	fHumidity = ((125.0f*(float)humidity) / 65536.0f) - 6.0f;
	fTemp = ((175.72f*(float)temp) / 65536.0f) - 46.85f;
	
	// copy the raw data into the struct
	memcpy(data->sensorDataRaw,rawData,RAW_DATA_LOCAL_SIZE);

	sprintf(tempStr,"%.2f,",fTemp);
	strcat(data->sensorDataStr,tempStr);
	
	sprintf(tempStr,"%.2f\n",fHumidity);
	strcat(data->sensorDataStr,tempStr);
	
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