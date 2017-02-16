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

#define READ_ERROR	   -1
#define READ_COMPLETE	1

static void MPL3115A2_Write(uint8_t reg, uint8_t data);
static uint8_t MPL3115A2_Read(uint8_t reg);
static void setModeBarometer();
static void setModeAltimeter();
static int8_t startPressureReading();
static void setOversampleRate(uint8_t sampleRate);
static void setModeStandby();
static void setModeActive();
static void toggleOneShot();
static void enableEventFlags();

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
	// Measure pressure in Pascals from 20 to 110 kPa
	setModeBarometer(); 
	// Set Oversample to 16x for faster reads and lower power use
	setOversampleRate(4); 
	// Enable all three pressure and temp event flags
	enableEventFlags(); 
	//setModeActive();
	moduleLED(OFF);
}

static void MPL3115A2_Write(uint8_t reg, uint8_t data) {
	I2CStart();
	I2CWrite(SENSOR_I2C_ADDRESS<<1);
	I2CWrite(reg);
	I2CWrite(data);
	I2CStop();
}

static uint8_t MPL3115A2_Read(uint8_t reg) {
	uint8_t tempData;
	I2CStart();
	// I2C Write
	I2CWrite(SENSOR_I2C_ADDRESS << 1);
	I2CWrite(reg);
	I2CStart();
	// I2C Read
	I2CWrite((SENSOR_I2C_ADDRESS << 1) | 0x01);
	tempData = I2CReadNACK();
	I2CStop();	
	return tempData;
}

static void setModeBarometer() {
	//Read current settings
	uint8_t tempSetting = MPL3115A2_Read(CTRL_REG1); 	
	//Clear ALT bit
	tempSetting &= ~(1<<7); 
	MPL3115A2_Write(CTRL_REG1, tempSetting);
}

static void setModeAltimeter() {
	//Read current settings
	uint8_t tempSetting = MPL3115A2_Read(CTRL_REG1);
	//Set ALT bit
	tempSetting |= (1<<7); //Set ALT bit
	MPL3115A2_Write(CTRL_REG1, tempSetting);
}

// Puts the sensor in standby mode
static void setModeStandby() {
	//Read current settings
	uint8_t tempSetting = MPL3115A2_Read(CTRL_REG1);
	//Clear SBYB bit for Standby mode
	tempSetting &= ~(1<<0); 
	MPL3115A2_Write(CTRL_REG1, tempSetting);
}

// Puts the sensor in active mode
static void setModeActive() {	
	//Read current settings
	uint8_t tempSetting = MPL3115A2_Read(CTRL_REG1);
	//Set SBYB bit for Active mode
	tempSetting |= (1<<0); 
	MPL3115A2_Write(CTRL_REG1, tempSetting);
}

// Call with a rate from 0 to 7. The higher the oversample 
// rate the greater the time between data samples.
static void setOversampleRate(uint8_t sampleRate) {
	uint8_t tempSetting;
	
	if(sampleRate > 7) {
		 sampleRate = 7; //OS cannot be larger than 0b.0111
	}
	sampleRate <<= 3; //Align it for the CTRL_REG1 register
	
	//Read current settings
	tempSetting = MPL3115A2_Read(CTRL_REG1);
	//Clear out old OS bits				
	tempSetting &= 0xC7; 
	//Mask in new OS bits
	tempSetting |= sampleRate; 
	MPL3115A2_Write(CTRL_REG1, tempSetting);
}

// Enables the pressure and temp measurement event flags so that we can
// test against them.
static void enableEventFlags() {
	MPL3115A2_Write(PT_DATA_CFG_REG, 0x07); // Enable all three pressure and temp event flags
}

//Clears then sets the OST bit which causes the sensor to immediately take another reading
//Needed to sample faster than 1Hz
static void toggleOneShot() {
	//Read current settings
	uint8_t tempSetting = MPL3115A2_Read(CTRL_REG1);
	//Clear OST bit
	tempSetting &= ~(1<<1); 
	MPL3115A2_Write(CTRL_REG1, tempSetting);
	
	//Read current settings to be safe
	tempSetting = MPL3115A2_Read(CTRL_REG1);
	//Set OST bit
	tempSetting |= (1<<1); 
	MPL3115A2_Write(CTRL_REG1, tempSetting);
}


static int8_t startPressureReading() {
	uint16_t counter = 0;

	//Check PDR bit, if it's not set then toggle OST
	if((MPL3115A2_Read(STATUS_REG) & (1<<2)) == 0){
		//Toggle the OST bit causing the sensor to immediately take another reading
		 toggleOneShot(); 
	}

	//Wait for PDR bit, indicates we have new pressure data
	while((MPL3115A2_Read(STATUS_REG) & (1<<2)) == 0) {
		// time out after 70ms (66ms for 16x OVSampling)
		if(++counter > 70) {
			return READ_ERROR;
		}
		_delay_ms(1);
	}
	
	return READ_COMPLETE;
}


void readSensor(sensorData_t *data) {
	moduleLED(ON);
	uint8_t PressureData[5];
	char tempStr[6];
	int32_t pressure;
	float fPressure;
	
	memset(data->sensorDataRaw,0,RAW_DATA_SIZE);
	memset(data->sensorDataStr,0,STR_DATA_SIZE);
	
	// start and wait for a pressure reading
	if(startPressureReading() == READ_ERROR) {
		// if there was an error then just return
		return;
	}
	
	I2CReadMult(SENSOR_I2C_ADDRESS,OUT_P_MSB_REG,PressureData,5);
	
	// copy the raw data into the struct
	memcpy(data->sensorDataRaw,PressureData,5);
	
	// Toggle the OST bit causing the sensor to immediately take another reading
	// for use next time
	toggleOneShot(); 

	// Combine the bytes together
	pressure = ( ( (int32_t)PressureData[0]<<16 ) | ((int32_t)PressureData[1]<<8) | ((int32_t)PressureData[2]) );
	
	// Pressure is an 18 bit number with 2 bits of decimal. Get rid of decimal portion
	pressure >>= 6;
	
	// Bits 5,4 fractional component
	PressureData[2] &= 0x30; 
	
	// Align it
	PressureData[2] >>= 4;
	
	fPressure = (float)PressureData[2] / 4.0f;
	
	fPressure += (float)pressure;
	
	sprintf(tempStr,"%.2f\n",fPressure);
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