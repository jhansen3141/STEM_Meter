/*
* Author: Josh Hansen
* Project: STEM-Meter Base Unit
* Last Updated: April. 4, 2017
* File: Board.h
* Desc: Header File
*/

#ifndef __BOARD_H
#define __BOARD_H

#ifdef __cplusplus
extern "C" {
#endif

#include "EK_TM4C123GXL.h"

#define Board_initDMA               EK_TM4C123GXL_initDMA
#define Board_initGeneral           EK_TM4C123GXL_initGeneral
#define Board_initGPIO              EK_TM4C123GXL_initGPIO
#define Board_initI2C               EK_TM4C123GXL_initI2C
#define Board_initPWM               EK_TM4C123GXL_initPWM
#define Board_initSDSPI             EK_TM4C123GXL_initSDSPI
#define Board_initSPI               EK_TM4C123GXL_initSPI
#define Board_initUART              EK_TM4C123GXL_initUART
#define Board_initUSB               EK_TM4C123GXL_initUSB
#define Board_initWatchdog          EK_TM4C123GXL_initWatchdog
#define Board_initWiFi              EK_TM4C123GXL_initWiFi

#define Board_I2C0                  EK_TM4C123GXL_I2C0
#define Board_I2C1                  EK_TM4C123GXL_I2C3
#define Board_I2C_TMP               EK_TM4C123GXL_I2C3
#define Board_I2C_NFC               EK_TM4C123GXL_I2C3
#define Board_I2C_TPL0401           EK_TM4C123GXL_I2C3

#define Board_SDSPI0                EK_TM4C123GXL_SDSPI0

#define Board_SPI0                  EK_TM4C123GXL_SPI0
#define Board_SPI2					EK_TM4C123GXL_SPI2
#define Board_SPI1                  EK_TM4C123GXL_SPI3

#define Board_UART0                 EK_TM4C123GXL_UART0
#define Board_UART1                 EK_TM4C123GXL_UART1
#define Board_UART2                 EK_TM4C123GXL_UART2
#define Board_UART3                 EK_TM4C123GXL_UART3

#define Board_WATCHDOG0             EK_TM4C123GXL_WATCHDOG0

#define Board_SENSOR_1_INPUT		SENSOR_1_INPUT
#define Board_SENSOR_2_INPUT		SENSOR_2_INPUT
#define Board_SENSOR_3_INPUT		SENSOR_3_INPUT
#define Board_SENSOR_4_INPUT		SENSOR_4_INPUT

#define Board_SENSOR_1_OUTPUT		SENSOR_1_OUTPUT
#define Board_SENSOR_2_OUTPUT		SENSOR_2_OUTPUT
#define Board_SENSOR_3_OUTPUT		SENSOR_3_OUTPUT
#define Board_SENSOR_4_OUTPUT		SENSOR_4_OUTPUT

#define Board_SPI_SLAVE_INT			BLE_INT_INPUT
#define Board_SD_CARD_INT			SD_CARD_INT_INPUT
#define Board_SPI_CS_INT			SPI_CS_OUTPUT

#define Board_PG_INT				CHARGE_PG_INPUT
#define Board_CHG_INT				CHARGE_CHG_INPUT

#define Board_SD_CARD_LED			SD_CARD_LED

#define Board_LED_ON				(0)
#define Board_LED_OFF				(1)

#define Board_GPIO_HIGH				(1)
#define Board_GPIO_LOW				(0)

#define Board_CS_ACTIVE				(0)
#define Board_CS_DEACTIVE			(1)

#define SENSOR_1_LED_PIN			GPIO_PIN_4
#define SENSOR_2_LED_PIN			GPIO_PIN_5
#define SENSOR_3_LED_PIN			GPIO_PIN_3
#define SENSOR_4_LED_PIN			GPIO_PIN_4

#define SENSOR_1_LED_PORT			GPIO_PORTD_BASE
#define SENSOR_2_LED_PORT			GPIO_PORTD_BASE
#define SENSOR_3_LED_PORT			GPIO_PORTF_BASE
#define SENSOR_4_LED_PORT			GPIO_PORTF_BASE

#define SENSOR_1_CNTL_PORT			GPIO_PORTE_BASE
#define SENSOR_2_CNTL_PORT			GPIO_PORTF_BASE
#define SENSOR_3_CNTL_PORT			GPIO_PORTE_BASE
#define SENSOR_4_CNTL_PORT			GPIO_PORTB_BASE

#define SENSOR_1_IN_PIN				GPIO_PIN_0
#define SENSOR_1_OUT_PIN			GPIO_PIN_1

#define SENSOR_2_IN_PIN				GPIO_PIN_1
#define SENSOR_2_OUT_PIN			GPIO_PIN_2

#define SENSOR_3_IN_PIN				GPIO_PIN_4
#define SENSOR_3_OUT_PIN			GPIO_PIN_5

#define SENSOR_4_IN_PIN				GPIO_PIN_2
#define SENSOR_4_OUT_PIN			GPIO_PIN_3

#ifdef __cplusplus
}
#endif

#endif /* __BOARD_H */
