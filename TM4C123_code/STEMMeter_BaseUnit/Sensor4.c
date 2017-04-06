/*
* Author: Josh Hansen
* Project: STEM-Meter Base Unit
* Last Updated: April. 4, 2017
* File: Sensor4.c
* Desc: Implements task responsible for sending / receiving data from
* sensor 4 over UART3
*/

#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <stdbool.h>

/* XDCtools Header files */
#include <xdc/std.h>
#include <xdc/runtime/System.h>

/* BIOS Header files */
#include <ti/sysbios/BIOS.h>
#include <ti/sysbios/knl/Clock.h>
#include <ti/sysbios/knl/Task.h>

/* TI-RTOS Header files */
#include <ti/drivers/GPIO.h>
#include <ti/drivers/UART.h>

#include "Board.h"
#include "Sensor.h"
#include "BLEWrite.h"
#include "FatSD.h"

#define TASKSTACKSIZE       1500
#define TASK_PRIORITY 		2

static Task_Struct sensor4TaskStruct;
static Char sensor4TaskStack[TASKSTACKSIZE];

static uint8_t uartBufferRX[SENSOR_FRAME_LENGTH+1];
bool Sensor4SDWriteEnabled = false;
static UART_Handle      UART3Handle;

static void Sensor4TaskFxn(UArg arg0, UArg arg1);
static void Sensor4TaskInit();
static void UART3WriteCallback(UART_Handle handle, void *buffer, size_t size);

void Sensor4_createTask(void) {
    Task_Params taskParams;
    /* Construct file copy Task thread */
    Task_Params_init(&taskParams);
    taskParams.stackSize = TASKSTACKSIZE;
    taskParams.stack = &sensor4TaskStack;
    taskParams.priority = TASK_PRIORITY;
    Task_construct(&sensor4TaskStruct, (Task_FuncPtr)Sensor4TaskFxn, &taskParams, NULL);
}

static void Sensor4TaskInit() {
	UART_Params      UART3params;

	UART_Params_init(&UART3params);
	UART3params.baudRate  = SENSOR_BAUD_RATE;
	UART3params.writeDataMode = UART_DATA_TEXT;
	UART3params.writeMode = UART_MODE_CALLBACK;
	UART3params.writeCallback = UART3WriteCallback;
	UART3params.readDataMode = UART_DATA_BINARY;
	UART3params.readReturnMode = UART_RETURN_FULL;
	UART3params.readMode = UART_MODE_BLOCKING;
	UART3params.readEcho = UART_ECHO_OFF;
	UART3Handle = UART_open(Board_UART3, &UART3params);
	if (!UART3Handle) {
		System_printf("UART3 did not open");
	}
}

void Sensor4WriteConfig(uint8_t freq) {
	char txBuffer[10];
	memset(txBuffer,0,10);
	sprintf(txBuffer,"SF %d\r",freq);
	UART_writePolling(UART3Handle,txBuffer,strlen(txBuffer));
}

static void UART3WriteCallback(UART_Handle handle, void *buffer, size_t size) {
	// TODO Ack write complete
}

static void Sensor4TaskFxn(UArg arg0, UArg arg1) {

	Sensor4TaskInit();

	while(1) {
		// block until 59 bytes have been recieved
		UART_read(UART3Handle,uartBufferRX,SENSOR_FRAME_LENGTH);

		// make sure frame sync bytes are correct
		if(uartBufferRX[0] == FRAME_BYTE_0 &&
			uartBufferRX[1] == FRAME_BYTE_1 &&
			uartBufferRX[2] == FRAME_BYTE_2)
		{
			// write the bytes to the CC2640
			enqueueBLEWritetTaskMsg(SENSOR_4_UPDATE_DATA_MSG,uartBufferRX+FRAME_BYTES_OFFSET,SENSOR_DATA_LENGTH);
			// if SD write is enabled for this sensor then enqueue the string data to the SD card task
			if(Sensor4SDWriteEnabled) {
				// enqueue only the string data portion of the incomming data, not the raw data
				enqueueSDTaskMsg(WRITE_S4_TO_SD_MSG,uartBufferRX+STR_BYTES_OFFSET);
			}
		}

	}

}




