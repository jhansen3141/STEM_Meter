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

static uint8_t uartBufferTX[25];
static uint8_t uartBufferRX[25];

static UART_Handle      UART2Handle;

static void Sensor3TaskFxn(UArg arg0, UArg arg1);
static void Sensor3TaskInit();
static void UART2Read(UART_Handle handle, void *buffer, size_t size);

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
	UART2params.readDataMode = UART_DATA_BINARY;
	UART2params.readReturnMode = UART_RETURN_FULL;
	UART2params.readMode = UART_MODE_CALLBACK;
	UART2params.readEcho = UART_ECHO_OFF;
	UART2params.readCallback = UART2Read;
	UART2Handle = UART_open(Board_UART2, &UART2params);
	if (!UART2Handle) {
		System_printf("UART2 did not open");
	}
	//UART_read(UART0Handle,);
}

static void UART2Read(UART_Handle handle, void *buffer, size_t size) {
	enqueueBLEWritetTaskMsg(SENSOR_3_UPDATE_CONFIG_MSG,buffer,20);
	UART_read(UART2Handle,uartBufferRX,20);
}


static void Sensor3TaskFxn(UArg arg0, UArg arg1) {

	Sensor3TaskInit();
	UART_read(UART2Handle,uartBufferRX,20);
	while(1) {
		Task_sleep(50);
	}

}




