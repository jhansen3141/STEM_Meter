// Josh Hansen
// STEM-Meter
// Team 3
// Spring 2017

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
#include <driverlib/gpio.h>
#include <inc/hw_memmap.h>

/* Example/Board Header files */
#include "Board.h"
#include "Sensor.h"
#include "BLEWrite.h"
#include "FatSD.h"


#define TASKSTACKSIZE       1500
#define TASK_PRIORITY 		1

static Task_Struct task2Struct;
static Char task2Stack[TASKSTACKSIZE];

static uint8_t uartBufferRX[SENSOR_FRAME_LENGTH+1];
bool Sensor2SDWriteEnabled = false;
static UART_Handle      UART1Handle;

static void Sensor2TaskFxn(UArg arg0, UArg arg1);
static void Sensor2TaskInit();
static void UART1WriteCallback(UART_Handle handle, void *buffer, size_t size);

void Sensor2_createTask(void) {
    Task_Params taskParams;
    /* Construct file copy Task thread */
    Task_Params_init(&taskParams);
    taskParams.stackSize = TASKSTACKSIZE;
    taskParams.stack = &task2Stack;
    taskParams.priority = TASK_PRIORITY;
    Task_construct(&task2Struct, (Task_FuncPtr)Sensor2TaskFxn, &taskParams, NULL);
}

static void Sensor2TaskInit() {
	UART_Params      UART1params;

	UART_Params_init(&UART1params);
	UART1params.baudRate  = SENSOR_BAUD_RATE;
	UART1params.writeDataMode = UART_DATA_TEXT;
	UART1params.writeMode = UART_MODE_CALLBACK;
	UART1params.writeCallback = UART1WriteCallback;
	UART1params.readDataMode = UART_DATA_BINARY;
	UART1params.readReturnMode = UART_RETURN_FULL;
	UART1params.readMode = UART_MODE_BLOCKING;
	UART1params.readEcho = UART_ECHO_OFF;
	UART1Handle = UART_open(Board_UART1, &UART1params);
	if (!UART1Handle) {
		System_printf("UART1 did not open");
	}
}

void Sensor2WriteConfig(uint8_t freq) {
	char txBuffer[10];
	sprintf(txBuffer,"SF %d\r",freq);
	UART_writePolling(UART1Handle,txBuffer,5);
}

void Sensor2RequestStr() {
	char txBuffer[5];
	strcpy(txBuffer,"RS\n");
	UART_writePolling(UART1Handle,txBuffer,3);
}

static void UART1WriteCallback(UART_Handle handle, void *buffer, size_t size) {
	// TODO Ack write complete
}

static void Sensor2TaskFxn(UArg arg0, UArg arg1) {

	Sensor2TaskInit();

	while(1) {
		// block until 20 bytes have been recieved
		UART_read(UART1Handle,uartBufferRX,SENSOR_FRAME_LENGTH);

		// make sure frame sync bytes are correct
		if(uartBufferRX[0] == FRAME_BYTE_0 &&
			uartBufferRX[1] == FRAME_BYTE_1 &&
			uartBufferRX[2] == FRAME_BYTE_2)
		{
			// write the bytes to the CC2640
			enqueueBLEWritetTaskMsg(SENSOR_2_UPDATE_DATA_MSG,uartBufferRX+FRAME_BYTES_OFFSET,SENSOR_DATA_LENGTH);
			// if SD write is enabled for this sensor then enqueue the string data to the SD card task
			if(Sensor2SDWriteEnabled) {
				// enqueue only the string data portion of the incomming data, not the raw data
				enqueueSDTaskMsg(WRITE_S2_TO_SD_MSG,uartBufferRX+STR_BYTES_OFFSET);
			}
		}

	}
}
