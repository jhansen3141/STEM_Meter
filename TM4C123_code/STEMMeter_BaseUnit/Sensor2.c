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
#include <ti/sysbios/knl/Semaphore.h>
#include <ti/sysbios/knl/Queue.h>

/* TI-RTOS Header files */
#include <ti/drivers/GPIO.h>
#include <ti/drivers/UART.h>
#include <driverlib/gpio.h>
#include <inc/hw_memmap.h>

/* Example/Board Header files */
#include "Board.h"
#include "Sensor.h"
#include "BLEWrite.h"


#define TASKSTACKSIZE       768
#define TASK_PRIORITY 		1

static Task_Struct task2Struct;
static Char task2Stack[TASKSTACKSIZE];

static uint8_t uartBufferTX[25];
static uint8_t uartBufferRX[25];

static UART_Handle      UART1Handle;


// Struct for task messages
typedef struct {
  Queue_Elem _elem;
  uint8_t pdu[];
} wifiWrite_msg_t;

static void Sensor2TaskFxn(UArg arg0, UArg arg1);
static void Sensor2TaskInit();
static void UART1Read(UART_Handle handle, void *buffer, size_t size);


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
	UART1params.readDataMode = UART_DATA_TEXT;
	UART1params.readReturnMode = UART_RETURN_NEWLINE;
	UART1params.readMode = UART_MODE_CALLBACK;
	UART1params.readEcho = UART_ECHO_OFF;
	UART1params.readCallback = UART1Read;
	UART1Handle = UART_open(Board_UART1, &UART1params);
	if (!UART1Handle) {
		System_printf("UART1 did not open");
	}

}

static void UART1Read(UART_Handle handle, void *buffer, size_t size) {
	enqueueBLEWritetTaskMsg(SENSOR_2_UPDATE_CONFIG_MSG,buffer,20);
	UART_read(UART1Handle,uartBufferRX,20);
}

static void Sensor2TaskFxn(UArg arg0, UArg arg1) {

	Sensor2TaskInit();
	UART_read(UART1Handle,uartBufferRX,20);
	while(1) {
		Task_sleep(50);
	}

}
