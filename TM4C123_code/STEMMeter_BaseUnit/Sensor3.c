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

/* Example/Board Header files */
#include "Board.h"
#include "Sensor.h"
#include "BLEWrite.h"
#include "FatSD.h"


#define TASKSTACKSIZE       768
#define TASK_PRIORITY 		1

static Task_Struct sensor3TaskStruct;
static Char sensor3TaskStack[TASKSTACKSIZE];

static uint8_t uartBufferRX[25];
bool Sensor3SDWriteEnabled = true;
static UART_Handle      UART2Handle;

static void Sensor3TaskFxn(UArg arg0, UArg arg1);
static void Sensor3TaskInit();
static void UART2WriteCallback(UART_Handle handle, void *buffer, size_t size);


void Sensor3_createTask(void) {
    Task_Params taskParams;
    /* Construct file copy Task thread */
    Task_Params_init(&taskParams);
    taskParams.stackSize = TASKSTACKSIZE;
    taskParams.stack = &sensor3TaskStack;
    taskParams.priority = TASK_PRIORITY;
    Task_construct(&sensor3TaskStruct, (Task_FuncPtr)Sensor3TaskFxn, &taskParams, NULL);
}

static void Sensor3TaskInit() {
	UART_Params      UART2params;

	UART_Params_init(&UART2params);
	UART2params.baudRate  = SENSOR_BAUD_RATE;
	UART2params.writeDataMode = UART_DATA_TEXT;
	UART2params.writeMode = UART_MODE_CALLBACK;
	UART2params.writeCallback = UART2WriteCallback;
	UART2params.readDataMode = UART_DATA_BINARY;
	UART2params.readReturnMode = UART_RETURN_FULL;
	UART2params.readMode = UART_MODE_BLOCKING;
	UART2params.readEcho = UART_ECHO_OFF;
	UART2Handle = UART_open(Board_UART2, &UART2params);
	if (!UART2Handle) {
		System_printf("UART2 did not open");
	}
}

void Sensor3WriteConfig(uint8_t freq) {
	char txBuffer[10];
	sprintf(txBuffer,"SF %d\n",freq);
	UART_writePolling(UART2Handle,txBuffer,5);
}

static void UART2WriteCallback(UART_Handle handle, void *buffer, size_t size) {
	// TODO Ack write complete
}

static void Sensor3TaskFxn(UArg arg0, UArg arg1) {

	Sensor3TaskInit();
	while(1) {
		// block until 20 bytes have been recieved
		UART_read(UART2Handle,uartBufferRX,SENSOR_FRAME_LENGTH);

		// make sure frame sync bytes are correct
		if(uartBufferRX[0] == FRAME_BYTE_0 &&
			uartBufferRX[1] == FRAME_BYTE_1 &&
			uartBufferRX[2] == FRAME_BYTE_2)
		{
			// write the bytes to the CC2640
			enqueueBLEWritetTaskMsg(SENSOR_3_UPDATE_DATA_MSG,uartBufferRX+FRAME_BYTES_OFFSET,SENSOR_DATA_LENGTH);
			// if SD write is enabled for this sensor then write the reading to the SD card
			if(Sensor3SDWriteEnabled) {
				enqueueSDTaskMsg(WRITE_TO_SD_MSG,uartBufferRX,SENSOR_DATA_LENGTH+FRAME_BYTES_OFFSET);
			}
		}
		else {
			// TODO Reset sensor module
		}
	}

}




