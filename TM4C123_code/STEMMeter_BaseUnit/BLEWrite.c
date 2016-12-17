// Josh Hansen
// CEEN 4360 - Fall 2016
// Phase 2

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
#include <ti/drivers/SPI.h>

#include <driverlib/gpio.h>
#include <inc/hw_memmap.h>

/* Example/Board Header files */
#include "Board.h"
#include "BLEWrite.h"
#include "SystemTime.h"


#define TASKSTACKSIZE       768
#define TASK_PRIORITY 		1

typedef enum {
	NO_SENSOR_ID = 0,
	SENSOR_1_ID,
	SENSOR_2_ID,
	SENSOR_3_ID,
	SENSOR_4_ID
} sensorID_t;

Task_Struct task0Struct;
Char task0Stack[TASKSTACKSIZE];

static SPI_Handle      SPIHandle;
static SPI_Params      SPIParams;
static SPI_Transaction spiTransaction;


// Queue for task messages
static Queue_Struct bleWriteMsgQ;
static Queue_Handle hBleWritesMsgQ;

static Semaphore_Struct semBLEWriteStruct;
static Semaphore_Handle semBLEWriteHandle;

static uint8_t txBuffer[21];
static uint8_t rxBuffer[21];

// Struct for task messages
typedef struct {
  Queue_Elem _elem;
  bleWrite_msg_types_t type;
  uint8_t pdu[];
} bleWrite_msg_t;

static void BLEWriteFxn(UArg arg0, UArg arg1);
static void BLEWrite_Init();
static void SPISendUpdate();
static void user_processBLEWriteMessage(bleWrite_msg_t *pMsg);
static void startUpLEDRoutine();


void BLEWrite_createTask(void) {
    Task_Params taskParams;
    /* Construct file copy Task thread */
    Task_Params_init(&taskParams);
    taskParams.stackSize = TASKSTACKSIZE;
    taskParams.stack = &task0Stack;
    taskParams.priority = TASK_PRIORITY;
    Task_construct(&task0Struct, (Task_FuncPtr)BLEWriteFxn, &taskParams, NULL);
}

static void BLEWrite_Init() {
	Semaphore_Params semParams;

    /* Construct a Semaphore object to be used as a resource lock, inital count 0 */
    Semaphore_Params_init(&semParams);
    Semaphore_construct(&semBLEWriteStruct, 0, &semParams);

    /* Obtain instance handle */
    semBLEWriteHandle = Semaphore_handle(&semBLEWriteStruct);

	Queue_construct(&bleWriteMsgQ, NULL);
	hBleWritesMsgQ = Queue_handle(&bleWriteMsgQ);

	 SPI_init();
	 SPI_Params_init(&SPIParams);
	 SPIParams.bitRate  = 1000000;
	 SPIParams.frameFormat = SPI_POL0_PHA0;
	 SPIParams.mode = SPI_MASTER;
	 SPIHandle = SPI_open(Board_SPI2, &SPIParams);
	 if (!SPIHandle) {
	       System_printf("SPI did not open");
	 }

}

static void SPISendUpdate() {
	bool ret;
	spiTransaction.count = 21;
	spiTransaction.txBuf = txBuffer;
	spiTransaction.rxBuf = rxBuffer;

	ret = SPI_transfer(SPIHandle, &spiTransaction);
	if (!ret) {

	}
//	if(rxBuffer[0] == '1') {
//		GPIOPinWrite(SENSOR_1_LED_PORT,SENSOR_1_LED_PIN,0);
//		GPIOPinWrite(SENSOR_2_LED_PORT,SENSOR_2_LED_PIN,0);
//
//	}
//	else if(rxBuffer[0] == '0') {
//		GPIOPinWrite(SENSOR_1_LED_PORT,SENSOR_1_LED_PIN,SENSOR_1_LED_PIN);
//		GPIOPinWrite(SENSOR_2_LED_PORT,SENSOR_2_LED_PIN,SENSOR_2_LED_PIN);
//
//	}

//	if(txBuffer[0] != 0) {
//		txBuffer[0] = NO_SENSOR_ID; // set sensor ID to none b/c tx complete
//	}
}

static void BLEWriteFxn(UArg arg0, UArg arg1) {

	BLEWrite_Init();

	startUpLEDRoutine();
	startUpLEDRoutine();


	while(1) {
		/* wait for swis to be posted from Clock function */
		Semaphore_pend(semBLEWriteHandle, BIOS_WAIT_FOREVER);
		//SPISendUpdate();
		while (!Queue_empty(hBleWritesMsgQ)) {
			bleWrite_msg_t *pMsg = Queue_dequeue(hBleWritesMsgQ); // dequeue the message
			user_processBLEWriteMessage(pMsg); // process the message
			free(pMsg); // free mem
		}
		//Task_sleep(50);
	}

}

// Input - Device Task Message Struct
// Output - None
// Description - Called from device task context when message dequeued
static void user_processBLEWriteMessage(bleWrite_msg_t *pMsg) {
	switch (pMsg->type) {
		// Set sensor ID
		case SENSOR_1_UPDATE_CONFIG_MSG:
			txBuffer[0] = SENSOR_1_ID;
			break;

		case SENSOR_2_UPDATE_CONFIG_MSG:
			txBuffer[0] = SENSOR_2_ID;
			break;

		case SENSOR_3_UPDATE_CONFIG_MSG:
			txBuffer[0] = SENSOR_3_ID;
			break;

		case SENSOR_4_UPDATE_CONFIG_MSG:
			txBuffer[0] = SENSOR_4_ID;
			break;
	}

	memcpy(txBuffer+1,pMsg->pdu,20); // copy sensor data into txBuffer
	SPISendUpdate(); // send the updated data
}

void enqueueBLEWritetTaskMsg(bleWrite_msg_types_t msgType, uint8_t *buffer, uint16_t len) {
	bleWrite_msg_t *pMsg = malloc(sizeof(bleWrite_msg_t) + len); // create a new message for the queue
	if (pMsg != NULL) {
		pMsg->type = msgType;
		memcpy(pMsg->pdu,buffer,len); // copy the data into the message
		Queue_enqueue(hBleWritesMsgQ, &pMsg->_elem); // enqueue the message
		Semaphore_post(semBLEWriteHandle);
	}
}

static void startUpLEDRoutine() {
	 GPIO_write(Board_SENSOR_1_LED, Board_LED_ON);
	 Task_sleep(100);
	 GPIO_write(Board_SENSOR_3_LED, Board_LED_ON);
	 Task_sleep(100);
	 GPIO_write(Board_SENSOR_4_LED, Board_LED_ON);
	 Task_sleep(100);
	 GPIO_write(Board_SENSOR_2_LED, Board_LED_ON);
	 Task_sleep(100);

	 GPIO_write(Board_SENSOR_1_LED, Board_LED_OFF);
	 Task_sleep(100);
	 GPIO_write(Board_SENSOR_3_LED, Board_LED_OFF);
	 Task_sleep(100);
	 GPIO_write(Board_SENSOR_4_LED, Board_LED_OFF);
	 Task_sleep(100);
	 GPIO_write(Board_SENSOR_2_LED, Board_LED_OFF);
}
