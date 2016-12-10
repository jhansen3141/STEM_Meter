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


#define TASKSTACKSIZE       2048
#define TASK_PRIORITY 		1

static Task_Struct task2Struct;
static Char task2Stack[TASKSTACKSIZE];

static uint8_t uartBufferTX[25];
static uint8_t uartBufferRX[25];

static UART_Handle      UART1Handle;

// Queue for task messages
static Queue_Struct wifiWriteMsgQ;
static Queue_Handle hWifiWriteMsgQ;

// Struct for task messages
typedef struct {
  Queue_Elem _elem;
  uint8_t pdu[];
} wifiWrite_msg_t;

static void Sensor2TaskFxn(UArg arg0, UArg arg1);
static void Sensor2TaskInit();
static void UART1Read(UART_Handle handle, void *buffer, size_t size);
static void user_processWifiWriteMessage(wifiWrite_msg_t *pMsg);

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

	Queue_construct(&wifiWriteMsgQ, NULL);
	hWifiWriteMsgQ = Queue_handle(&wifiWriteMsgQ);

	UART_Params_init(&UART1params);
	UART1params.baudRate  = 115200;
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

	GPIOPinWrite(SENSOR_3_LED_PORT,SENSOR_3_LED_PIN,SENSOR_3_LED_PIN);
	GPIOPinWrite(SENSOR_4_LED_PORT,SENSOR_4_LED_PIN,SENSOR_4_LED_PIN);
	//UART_read(UART0Handle,);
}

static void UART1Read(UART_Handle handle, void *buffer, size_t size) {
	uint8_t rxBuffer[20] = {0};
	memcpy(rxBuffer,buffer,20);
	if(rxBuffer[0] == '1') {
		GPIOPinWrite(SENSOR_3_LED_PORT,SENSOR_3_LED_PIN,0);
		GPIOPinWrite(SENSOR_4_LED_PORT,SENSOR_4_LED_PIN,0);
	}
	else {
		GPIOPinWrite(SENSOR_3_LED_PORT,SENSOR_3_LED_PIN,SENSOR_3_LED_PIN);
		GPIOPinWrite(SENSOR_4_LED_PORT,SENSOR_4_LED_PIN,SENSOR_4_LED_PIN);
	}
	UART_read(UART1Handle,uartBufferRX,20);
}


static void Sensor2TaskFxn(UArg arg0, UArg arg1) {

	Sensor2TaskInit();
	Task_sleep(1000); // get past the UART garbage
	UART_read(UART1Handle,uartBufferRX,20);
	while(1) {
		while (!Queue_empty(hWifiWriteMsgQ)) {
			wifiWrite_msg_t *pMsg = Queue_dequeue(hWifiWriteMsgQ); // dequeue the message
			user_processWifiWriteMessage(pMsg); // process the message
			free(pMsg); // free mem
		}
		Task_sleep(50);

	}

}

// Input - Device Task Message Struct
// Output - None
// Description - Called from device task context when message dequeued
static void user_processWifiWriteMessage(wifiWrite_msg_t *pMsg) {
	uint8_t upperByte, lowerByte;
	int16_t combined;
	float temp;
	char charBuffer[20] = {0};
	memcpy(uartBufferTX,pMsg->pdu,20);
	upperByte = pMsg->pdu[1];
	lowerByte = pMsg->pdu[2];
	combined = ((int16_t)upperByte<<8) | lowerByte;
	temp = combined & 0x0FFF;
	temp = temp / 16.0f;
	if(combined & 0x1000) {
		temp -= 256;
	}

	temp = temp * 1.8 + 32;
	sprintf(charBuffer,"%.2f",temp);
	charBuffer[19] = '\n';
	memcpy(uartBufferTX,charBuffer,20);

	uartBufferTX[19] = '\n';
	// write the sensor data to the WiFi Module
	UART_write(UART1Handle,uartBufferTX,20);

}

void enqueueWifiWritetTaskMsg(uint8_t *buffer, uint16_t len) {
	wifiWrite_msg_t *pMsg = malloc(sizeof(wifiWrite_msg_t) + len);
	if (pMsg != NULL) {
		memcpy(pMsg->pdu,buffer,len);
		Queue_enqueue(hWifiWriteMsgQ, &pMsg->_elem);
	}
}




