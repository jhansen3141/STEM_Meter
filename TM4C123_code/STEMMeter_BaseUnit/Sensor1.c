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
#include <driverlib/systick.h>

/* Example/Board Header files */
#include "Board.h"
#include "Sensor.h"
#include "BLEWrite.h"


#define TASKSTACKSIZE       768
#define TASK_PRIORITY 		1

static Task_Struct sensor1TaskStruct;
static Char sensor1TaskStack[TASKSTACKSIZE];

static uint8_t uartBufferTX[25];
static uint8_t uartBufferRX[25];

static UART_Handle      UART0Handle;

static void Sensor1TaskFxn(UArg arg0, UArg arg1);
static void Sensor1TaskInit();
static void UART0Read(UART_Handle handle, void *buffer, size_t size);

void Sensor1_createTask(void) {
    Task_Params taskParams;
    /* Construct file copy Task thread */
    Task_Params_init(&taskParams);
    taskParams.stackSize = TASKSTACKSIZE;
    taskParams.stack = &sensor1TaskStack;
    taskParams.priority = TASK_PRIORITY;
    Task_construct(&sensor1TaskStruct, (Task_FuncPtr)Sensor1TaskFxn, &taskParams, NULL);
}

static void Sensor1TaskInit() {
	UART_Params      UART0params;

	UART_Params_init(&UART0params);
	UART0params.baudRate  = SENSOR_BAUD_RATE;
	UART0params.writeDataMode = UART_DATA_TEXT;
	UART0params.readDataMode = UART_DATA_BINARY;
	UART0params.readReturnMode = UART_RETURN_FULL;
	UART0params.readMode = UART_MODE_CALLBACK;
	UART0params.readEcho = UART_ECHO_OFF;
	UART0params.readCallback = UART0Read;
	UART0Handle = UART_open(Board_UART0, &UART0params);
	if (!UART0Handle) {
		System_printf("UART0 did not open");
	}
}

static void UART0Read(UART_Handle handle, void *buffer, size_t size) {
	enqueueBLEWritetTaskMsg(SENSOR_1_UPDATE_CONFIG_MSG,buffer,20);
	enqueueWifiWritetTaskMsg(buffer,20);
	UART_read(UART0Handle,uartBufferRX,20);
}


static void Sensor1TaskFxn(UArg arg0, UArg arg1) {

	Sensor1TaskInit();
	UART_read(UART0Handle,uartBufferRX,20);
	while(1) {
		Task_sleep(50);

	}
}




