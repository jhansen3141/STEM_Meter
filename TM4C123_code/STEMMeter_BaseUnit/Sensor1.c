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
#include <driverlib/systick.h>

/* Example/Board Header files */
#include "Board.h"
#include "Sensor.h"
#include "BLEWrite.h"
#include "FatSD.h"

#define TASKSTACKSIZE       1500
#define TASK_PRIORITY 		1

static Task_Struct sensor1TaskStruct;
static Char sensor1TaskStack[TASKSTACKSIZE];

static uint8_t uartBufferRX[SENSOR_FRAME_LENGTH+1];
bool Sensor1SDWriteEnabled = false;
static UART_Handle      UART0Handle;

static void Sensor1TaskFxn(UArg arg0, UArg arg1);
static void Sensor1TaskInit();
static void UART0WriteCallback(UART_Handle handle, void *buffer, size_t size);


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
	UART0params.writeMode = UART_MODE_CALLBACK;
	UART0params.writeCallback = UART0WriteCallback;
	UART0params.readDataMode = UART_DATA_BINARY;
	UART0params.readReturnMode = UART_RETURN_FULL;
	UART0params.readMode = UART_MODE_BLOCKING;
	UART0params.readEcho = UART_ECHO_OFF;
	UART0Handle = UART_open(Board_UART0, &UART0params);
	if (!UART0Handle) {
		System_printf("UART0 did not open");
	}
}

void Sensor1WriteConfig(uint8_t freq) {
	char txBuffer[10];
	sprintf(txBuffer,"SF %d\r",freq);
	UART_writePolling(UART0Handle,txBuffer,5);
}

void Sensor1RequestStr() {
	char txBuffer[5];
	strcpy(txBuffer,"RS\n");
	UART_writePolling(UART0Handle,txBuffer,3);
}

static void UART0WriteCallback(UART_Handle handle, void *buffer, size_t size) {
	// TODO Ack write complete
}

static void Sensor1TaskFxn(UArg arg0, UArg arg1) {

	Sensor1TaskInit();

	while(1) {
		// block until 59 bytes have been recieved
		UART_read(UART0Handle,uartBufferRX,SENSOR_FRAME_LENGTH);

		// make sure frame sync bytes are correct
		if(uartBufferRX[0] == FRAME_BYTE_0 &&
			uartBufferRX[1] == FRAME_BYTE_1 &&
			uartBufferRX[2] == FRAME_BYTE_2)
		{
			// write the raw data to the CC2640
			enqueueBLEWritetTaskMsg(SENSOR_1_UPDATE_DATA_MSG,uartBufferRX+FRAME_BYTES_OFFSET,SENSOR_DATA_LENGTH);
			// if SD write is enabled for this sensor then enqueue the string data to the SD card task
			if(Sensor1SDWriteEnabled) {
				// enqueue only the string data portion of the incomming data, not the raw data
				enqueueSDTaskMsg(WRITE_S1_TO_SD_MSG,uartBufferRX+STR_BYTES_OFFSET);
			}
		}

	}
}
