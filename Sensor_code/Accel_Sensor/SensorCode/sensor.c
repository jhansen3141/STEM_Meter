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

static void MPU6050_Write(uint8_t reg, uint8_t data);

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
	MPU6050_Write(SMPRT_DIV, 0x07); //Sample rate -8000/1+7 = 1000Hz
	MPU6050_Write(MPU_CONFIG, 0x03); //Disable FSync, 40Hz DLPF
	MPU6050_Write(GYRO_CONFIG, 0x18);//Disable gyro self tests, scale of 2000 degrees/s
	MPU6050_Write(ACCEL_CONFIG, 0x18); //Disable accel self tests, scale of +-16g, no DHPF
	MPU6050_Write(MOT_THR, 0x00); // Disable motion threshold
	MPU6050_Write(FIFO_EN, 0x00); // Disable FIFO buffer
	MPU6050_Write(I2C_MST_CTRL, 0x00); //Sets AUX I2C to single master control
	MPU6050_Write(PWR_MGMT_1, 0x00); // Sleep disable - internal 8Mhz clock source
	MPU6050_Write(PWR_MGMT_2, 0x00); // All 6-axis out of standby mode
	
	moduleLED(OFF);
}

static void MPU6050_Write(uint8_t reg, uint8_t data) {
	I2CStart();
	I2CWrite(MPU6050_WRITE_ADDRESS);
	I2CWrite(reg);
	I2CWrite(data);
	I2CStop();
}

void readSensor(sensorData_t *data) {
	moduleLED(ON);
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
	
	
	sprintf(tempStr,"%1.2f,",fAccelX);
	strcat(data->sensorDataStr,tempStr);
	
	sprintf(tempStr,"%1.2f,",fAccelY);
	strcat(data->sensorDataStr,tempStr);
	
	sprintf(tempStr,"%1.2f\n",fAccelZ);
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