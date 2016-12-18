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

static Task_Struct sensor4TaskStruct;
static Char sensor4TaskStack[TASKSTACKSIZE];

//static uint8_t uartBufferTX[25];
static uint8_t uartBufferRX[25];

static UART_Handle      UART3Handle;

static void Sensor4TaskFxn(UArg arg0, UArg arg1);
static void Sensor4TaskInit();

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
	sprintf(txBuffer,"SF %d\n",freq);
	UART_write(UART3Handle,txBuffer,5);
}

static void Sensor4TaskFxn(UArg arg0, UArg arg1) {

	Sensor4TaskInit();

	while(1) {
		// block until 20 bytes have been recieved
		UART_read(UART3Handle,uartBufferRX,SENSOR_FRAME_LENGTH);
		enqueueBLEWritetTaskMsg(SENSOR_4_UPDATE_DATA_MSG,uartBufferRX+FRAME_BYTES_OFFSET,SENSOR_DATA_LENGTH);

		// make sure frame sync bytes are correct
		if(uartBufferRX[0] == FRAME_BYTE_0 &&
			uartBufferRX[1] == FRAME_BYTE_1 &&
			uartBufferRX[2] == FRAME_BYTE_2)
		{
			// write the bytes to the CC2640
			enqueueBLEWritetTaskMsg(SENSOR_4_UPDATE_DATA_MSG,uartBufferRX+FRAME_BYTES_OFFSET,SENSOR_DATA_LENGTH);
		}
		else {
			// TODO Reset sensor module
		}
	}

}




