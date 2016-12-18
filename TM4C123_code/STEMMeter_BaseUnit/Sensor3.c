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


#define TASKSTACKSIZE       768
#define TASK_PRIORITY 		1

static Task_Struct sensor3TaskStruct;
static Char sensor3TaskStack[TASKSTACKSIZE];

//static uint8_t uartBufferTX[25];
static uint8_t uart2BufferRX[25];

static UART_Handle      UART2Handle;

static void Sensor3TaskFxn(UArg arg0, UArg arg1);
static void Sensor3TaskInit();


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
	UART_write(UART2Handle,txBuffer,5);
}

static void Sensor3TaskFxn(UArg arg0, UArg arg1) {

	Sensor3TaskInit();
	while(1) {
		// block until 20 bytes have been recieved
		UART_read(UART2Handle,uart2BufferRX,SENSOR_FRAME_LENGTH);

		// make sure frame sync bytes are correct
		if(uart2BufferRX[0] == FRAME_BYTE_0 &&
			uart2BufferRX[1] == FRAME_BYTE_1 &&
			uart2BufferRX[2] == FRAME_BYTE_2)
		{
			// write the bytes to the CC2640
			enqueueBLEWritetTaskMsg(SENSOR_3_UPDATE_DATA_MSG,uart2BufferRX+FRAME_BYTES_OFFSET,SENSOR_DATA_LENGTH);
		}
		else {
			// TODO Reset sensor module
		}
	}

}




