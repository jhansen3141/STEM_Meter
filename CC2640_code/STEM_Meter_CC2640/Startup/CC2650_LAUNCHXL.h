/*
 * Copyright (c) 2015, Texas Instruments Incorporated
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * *  Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * *  Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * *  Neither the name of Texas Instruments Incorporated nor the names of
 *    its contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
/** ============================================================================
 *  @file       CC2650_LAUNCHXL.h
 *
 *  @brief      CC2650 LaunchPad Board Specific header file.
 *
 *  NB! This is the board file for CC2650 LaunchPad PCB version 0.2
 *
 *  ============================================================================
 */
#ifndef __CC2650_LAUNCHXL_BOARD_H__
#define __CC2650_LAUNCHXL_BOARD_H__

#ifdef __cplusplus
extern "C" {
#endif

/** ============================================================================
 *  Includes
 *  ==========================================================================*/
#include <ti/drivers/PIN.h>
#include <driverlib/ioc.h>

/** ============================================================================
 *  Externs
 *  ==========================================================================*/
extern const PIN_Config BoardGpioInitTable[];

/** ============================================================================
 *  Defines
 *  ==========================================================================*/

/* Identify as CC2650 LaunchPad */
#define CC2650LP

/* Same RF Configuration as 7x7 EM */
#define CC2650EM_7ID

 /* External flash manufacturer and device ID */
#define EXT_FLASH_MAN_ID            0xEF
#define EXT_FLASH_DEV_ID            0x12

/* Mapping of pins to board signals using general board aliases
 *      <board signal alias>        <pin mapping>
 */

#define Board_LED_ON                1
#define Board_LED_OFF               0


/* UART Board */
#define Board_UART_RX               IOID_2          /* RXD  */
#define Board_UART_TX               IOID_3          /* TXD  */
#define Board_UART_CTS              IOID_19         /* CTS  */
#define Board_UART_RTS              IOID_18         /* RTS */

/* SPI Board */
#define Board_SPI0_MISO             IOID_9          /* RF1.20 */
#define Board_SPI0_MOSI             IOID_8          /* RF1.18 */
#define Board_SPI0_CLK              IOID_10         /* RF1.16 */
#define Board_SPI0_CSN              IOID_11			// IOID_11
#define Board_SPI1_MISO             PIN_UNASSIGNED
#define Board_SPI1_MOSI             PIN_UNASSIGNED
#define Board_SPI1_CLK              PIN_UNASSIGNED
#define Board_SPI1_CSN              PIN_UNASSIGNED

/* I2C */
#define Board_I2C0_SCL0             IOID_12
#define Board_I2C0_SDA0             IOID_13

#define Board_RED_LED				IOID_3
#define Board_BLU_LED				IOID_4
#define Board_GRN_LED				IOID_2


#define Board_SPI_INT				IOID_0
#define Board_SPI_CS_Pin			IOID_1
#define Board_BTN					IOID_14


/** ============================================================================
 *  Instance identifiers
 *  ==========================================================================*/
/* Generic I2C instance identifiers */
#define Board_I2C                   CC2650_LAUNCHXL_I2C0
/* Generic SPI instance identifiers */
#define Board_SPI0                  CC2650_LAUNCHXL_SPI0
#define Board_SPI1                  CC2650_LAUNCHXL_SPI1
/* Generic UART instance identifiers */
#define Board_UART                  CC2650_LAUNCHXL_UART0


/** ============================================================================
 *  Number of peripherals and their names
 *  ==========================================================================*/

/*!
 *  @def    CC2650_LAUNCHXL_I2CName
 *  @brief  Enum of I2C names on the CC2650 dev board
 */
typedef enum CC2650_LAUNCHXL_I2CName {
    CC2650_LAUNCHXL_I2C0 = 0,

    CC2650_LAUNCHXL_I2CCOUNT
} CC2650_LAUNCHXL_I2CName;

/*!
 *  @def    CC2650_LAUNCHXL_CryptoName
 *  @brief  Enum of Crypto names on the CC2650 dev board
 */
typedef enum CC2650_LAUNCHXL_CryptoName {
    CC2650_LAUNCHXL_CRYPTO0 = 0,

    CC2650_LAUNCHXL_CRYPTOCOUNT
} CC2650_LAUNCHXL_CryptoName;


/*!
 *  @def    CC2650_LAUNCHXL_SPIName
 *  @brief  Enum of SPI names on the CC2650 dev board
 */
typedef enum CC2650_LAUNCHXL_SPIName {
    CC2650_LAUNCHXL_SPI0 = 0,
    CC2650_LAUNCHXL_SPI1,

    CC2650_LAUNCHXL_SPICOUNT
} CC2650_LAUNCHXL_SPIName;

/*!
 *  @def    CC2650_LAUNCHXL_UARTName
 *  @brief  Enum of UARTs on the CC2650 dev board
 */
typedef enum CC2650_LAUNCHXL_UARTName {
    CC2650_LAUNCHXL_UART0 = 0,

    CC2650_LAUNCHXL_UARTCOUNT
} CC2650_LAUNCHXL_UARTName;

/*!
 *  @def    CC2650_LAUNCHXL_UdmaName
 *  @brief  Enum of DMA buffers
 */
typedef enum CC2650_LAUNCHXL_UdmaName {
    CC2650_LAUNCHXL_UDMA0 = 0,

    CC2650_LAUNCHXL_UDMACOUNT
} CC2650_LAUNCHXL_UdmaName;

#ifdef __cplusplus
}
#endif

#endif /* __CC2650_LAUNCHXL_BOARD_H__ */
