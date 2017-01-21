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

#include "Sensor.h"

#define TASKSTACKSIZE       1024
#define TASK_PRIORITY 		1
#define SPI_BIT_RATE 		5000000
#define SPI_CONFIG_DATA_MARKER			0xA5
#define CONFIG_RECIEVED_MARKER 			0x55

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

// Queue for task messages
static Queue_Struct bleWriteMsgQ;
static Queue_Handle hBleWritesMsgQ;

static Semaphore_Struct semBLEWriteStruct;
static Semaphore_Handle semBLEWriteHandle;

// Struct for task messages
typedef struct {
  Queue_Elem _elem;
  bleWrite_msg_types_t type;
  uint8_t pdu[];
} bleWrite_msg_t;

static void BLEWriteFxn(UArg arg0, UArg arg1);
static void BLEWrite_Init();
static bool SPISendUpdate(uint8_t *txBuffer, uint8_t *rxBuffer);
static void user_processBLEWriteMessage(bleWrite_msg_t *pMsg);
static void sdCardLEDBlink(uint8_t numBlink);
static void SPISlaveInterrupt(unsigned int index);
static void enqueueSelfMsg(bleWrite_msg_types_t msgType);
static void updateSensorConfig();


void BLEWrite_createTask(void) {
    Task_Params taskParams;
    // Construct task thread
    Task_Params_init(&taskParams);
    taskParams.stackSize = TASKSTACKSIZE;
    taskParams.stack = &task0Stack;
    taskParams.priority = TASK_PRIORITY;
    Task_construct(&task0Struct, (Task_FuncPtr)BLEWriteFxn, &taskParams, NULL);
}

static void BLEWrite_Init() {
	Semaphore_Params semParams;

    // Construct a Semaphore object to be used as a resource lock, inital count 0
    Semaphore_Params_init(&semParams);
    Semaphore_construct(&semBLEWriteStruct, 0, &semParams);

    /* Obtain instance handle */
    semBLEWriteHandle = Semaphore_handle(&semBLEWriteStruct);

	Queue_construct(&bleWriteMsgQ, NULL);
	hBleWritesMsgQ = Queue_handle(&bleWriteMsgQ);

	 SPI_init();
	 SPI_Params_init(&SPIParams);
	 SPIParams.bitRate  = SPI_BIT_RATE;
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

static void SPISlaveInterrupt(unsigned int index) {
	// enqueue a message to tell task that sensor update is requested
	enqueueSelfMsg(UPDATE_SENSOR_CONFIG_MSG);
	// disable the interrupt until finished
	GPIO_disableInt(Board_SPI_SLAVE_INT);
}

static bool SPISendUpdate(uint8_t *txBuffer, uint8_t *rxBuffer) {
	bool returnStatus;
	// bring the CS line active to tell slave SPI transfer about to start
	GPIO_write(Board_SPI_CS_INT, Board_CS_ACTIVE);

	SPI_Transaction spiTransaction;
	spiTransaction.count = 21;
	spiTransaction.txBuf = txBuffer;
	spiTransaction.rxBuf = rxBuffer;
	// do the SPI transfer
	returnStatus = SPI_transfer(SPIHandle, &spiTransaction);

	// deactivate the SPI CS line
	GPIO_write(Board_SPI_CS_INT, Board_CS_DEACTIVE);
	return returnStatus;
}

static void BLEWriteFxn(UArg arg0, UArg arg1) {

	BLEWrite_Init();

	// Blink the LED 3 times on power up
	sdCardLEDBlink(3);

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
	uint8_t rxBuffer[21];
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
		SPISendUpdate(txBufferUpdate,rxBuffer);
	}
}

// Sends sensor config data to appropriate sensor over UART
static void updateSensorConfig() {
	bool ret;
	uint8_t dummyTXBuffer[21];;
	uint8_t localRXBuffer[21];

	memset(dummyTXBuffer,0,21);
	Task_sleep(25);
	// perform a transfer to get the config data from the CC2640
	ret = SPISendUpdate(dummyTXBuffer,localRXBuffer);

	if (ret) {
		// Byte 0 = S1 Freq | Byte 1 = S1 SD Log
		// Byte 2 = S2 Freq | Byte 3 = S2 SD Log
		// Byte 4 = S3 Freq | Byte 5 = S3 SD Log
		// Byte 6 = S4 Freq | Byte 7 = S4 SD Log

		if(localRXBuffer[20] == SPI_CONFIG_DATA_MARKER) {
			Sensor1WriteConfig(localRXBuffer[0]);
			if(localRXBuffer[1]) {
				Sensor1SDWriteEnabled = true;
			}
			else {
				Sensor1SDWriteEnabled = false;
			}

			Sensor2WriteConfig(localRXBuffer[2]);
			if(localRXBuffer[3]) {
				Sensor2SDWriteEnabled = true;
			}
			else {
				Sensor2SDWriteEnabled = false;
			}

			Sensor3WriteConfig(localRXBuffer[4]);
			if(localRXBuffer[5]) {
				Sensor3SDWriteEnabled = true;
			}
			else {
				Sensor3SDWriteEnabled = false;
			}

			Sensor4WriteConfig(localRXBuffer[6]);
			if(localRXBuffer[7]) {
				Sensor4SDWriteEnabled = true;
			}
			else {
				Sensor4SDWriteEnabled = false;
			}
		}
	}

	// Re-Enable interrupt
	GPIO_enableInt(Board_SPI_SLAVE_INT);
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

static void sdCardLEDBlink(uint8_t numBlink) {
	uint8_t i;
	for(i=0;i<numBlink;i++) {
		GPIO_write(Board_SD_CARD_LED, Board_LED_ON);
		Task_sleep(300);
		GPIO_write(Board_SD_CARD_LED, Board_LED_OFF);
		Task_sleep(300);
	}
}
