#include <stdint.h>
#include <avr/io.h>
#include <math.h>
#include <stdlib.h>
#include <string.h>
#include "sensor.h"
#include "sensorCommon.h"
#include "i2c.h"
#include "uart.h"


static uint8_t sensorRate = SENSOR_DEFAULT_RATE;
static void MPU6050_Write(uint8_t reg, uint8_t data);


void initBoard(void) {
	DDRB |= (1<<0); // LED as output
	
	I2CInit();
	UARTInit();
	OCR1A = TIMER_TEN_HZ_NUM;
	TCCR1B|=(1<<WGM12); // Timer1 in CTC mode
	TIMSK1|=(1<<OCIE1A); // Enable Timer1, CTC Compare A interrupt
	TCCR1B|=(1<<CS10) | (1<<CS12); // Enable Timer1 with prescaler of F_CPU/1024 (128uS / tick)
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
	uint8_t IMUData[14];
	memset(data->sensorData,0,DATA_SIZE);
	I2CReadMult(MPU6050_ADDRESS,ACCEL_XOUT_H,IMUData,14);
	memcpy(data->sensorData,IMUData,6);
	memcpy(data->sensorData+6,IMUData+8,6);
	data->sensorRate = sensorRate;
}

void moduleLED(ledState_t state) {
	if(state == OFF) {
		PORTB |= (1<<0);
	}
	else {
		PORTB &= ~(1<<0);
	}
}