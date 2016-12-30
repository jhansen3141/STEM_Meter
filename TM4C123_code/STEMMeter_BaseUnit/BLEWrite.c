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

#include "Sensor.h"

#define TASKSTACKSIZE       1024
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

//static uint8_t txBuffer[21];
static uint8_t rxBuffer[21];

// Struct for task messages
typedef struct {
  Queue_Elem _elem;
  bleWrite_msg_types_t type;
  uint8_t pdu[];
} bleWrite_msg_t;

static void BLEWriteFxn(UArg arg0, UArg arg1);
static void BLEWrite_Init();
static bool SPISendUpdate(uint8_t *txBuffer);
static void user_processBLEWriteMessage(bleWrite_msg_t *pMsg);
static void startUpLEDRoutine(uint8_t rotations);
static void SPISlaveInterrupt();
static void enqueueSelfMsg(bleWrite_msg_types_t msgType);
static void updateSensorConfig();


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
	 SPIParams.transferMode = SPI_MODE_BLOCKING;
	 SPIHandle = SPI_open(Board_SPI2, &SPIParams);
	 if (!SPIHandle) {
	       System_printf("SPI did not open");
	 }

	// install SPI Slave Interrupt callback
	GPIO_setCallback(Board_SPI_SLAVE_INT, SPISlaveInterrupt);

	// Enable interrupt
	GPIO_enableInt(Board_SPI_SLAVE_INT);

}

static void SPISlaveInterrupt() {
	enqueueSelfMsg(UPDATE_SENSOR_CONFIG_MSG);
}

static bool SPISendUpdate(uint8_t *txBuffer) {
	memset(rxBuffer,0,21); // clear the RX buffer first
	spiTransaction.count = 21;
	spiTransaction.txBuf = txBuffer;
	spiTransaction.rxBuf = rxBuffer;
	// do the SPI transfer
	return SPI_transfer(SPIHandle, &spiTransaction);
}

static void BLEWriteFxn(UArg arg0, UArg arg1) {

	BLEWrite_Init();

	startUpLEDRoutine(3);

	while(1) {
		// block until work to do
		Semaphore_pend(semBLEWriteHandle, BIOS_WAIT_FOREVER);

		while (!Queue_empty(hBleWritesMsgQ)) {
			bleWrite_msg_t *pMsg = Queue_dequeue(hBleWritesMsgQ); // dequeue the message
			user_processBLEWriteMessage(pMsg); // process the message
			free(pMsg); // free mem
		}
	}
}

// Input - Device Task Message Struct
// Output - None
// Description - Called from device task context when message dequeued
static void user_processBLEWriteMessage(bleWrite_msg_t *pMsg) {
	bool shouldSendUpdate = true;
	uint8_t txBufferUpdate[21];
	;
	switch (pMsg->type) {
		// Set sensor ID to be transmitted
		case SENSOR_1_UPDATE_DATA_MSG:
			txBufferUpdate[0] = SENSOR_1_ID;
			break;

		case SENSOR_2_UPDATE_DATA_MSG:
			txBufferUpdate[0] = SENSOR_2_ID;
			break;

		case SENSOR_3_UPDATE_DATA_MSG:
			txBufferUpdate[0] = SENSOR_3_ID;
			break;

		case SENSOR_4_UPDATE_DATA_MSG:
			txBufferUpdate[0] = SENSOR_4_ID;
			break;
		// message to update a sensor config
		case UPDATE_SENSOR_CONFIG_MSG:
			// Doing config update, not sensor data
			shouldSendUpdate = false;
			updateSensorConfig();
			break;
	}

	if(shouldSendUpdate) {
		// copy sensor data into txBuffer
		memcpy(txBufferUpdate+1,pMsg->pdu,20);
		// send the updated data
		SPISendUpdate(txBufferUpdate);
	}
}

static void updateSensorConfig() {
	bool ret;
	uint8_t sensorNumber;
	uint8_t sensorFreq;
	uint8_t sdCardWrite;
	uint8_t dummyTXBuffer[21];;
	memset(dummyTXBuffer,0,21);
	ret = SPISendUpdate(dummyTXBuffer);
	// Byte 0 = Sensor Position Number
	// Byte 1 = Sensor Freq
	// Byte 2 = Sensor SD Log

	if (ret) {
		// convert ASCII to int (Note: Only works for numbers 0-9)
		// Only 4 sensors and less than 10 freq and SD card states
		sensorNumber = rxBuffer[0] & 0x0F;
		sensorFreq = rxBuffer[1] & 0x0F;
		sdCardWrite = rxBuffer[2] & 0x0F;
		switch(sensorNumber) {
		case SENSOR_1_ID:
			Sensor1WriteConfig(sensorFreq);
			break;
		case SENSOR_2_ID:
			Sensor2WriteConfig(sensorFreq);
			break;
		case SENSOR_3_ID:
			Sensor3WriteConfig(sensorFreq);
			break;
		case SENSOR_4_ID:
			Sensor4WriteConfig(sensorFreq);
			break;
		}
	}
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

static void enqueueSelfMsg(bleWrite_msg_types_t msgType) {
	bleWrite_msg_t *pMsg = malloc(sizeof(bleWrite_msg_t));
	if (pMsg != NULL) {
		pMsg->type = msgType;
		Queue_enqueue(hBleWritesMsgQ, &pMsg->_elem); // enqueue the message
		Semaphore_post(semBLEWriteHandle);
	}
}

static void startUpLEDRoutine(uint8_t rotations) {
	uint8_t i;
	for(i=0;i<rotations;i++) {
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
		Task_sleep(100);
	}
}
