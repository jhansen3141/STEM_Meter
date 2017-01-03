// Josh Hansen
// STEM-Meter
// Team 3
// Spring 2017

#include <ti/drivers/PIN.h>
#include <xdc/runtime/Diags.h>
#include <ti/sysbios/knl/Task.h>
#include <ti/sysbios/knl/Semaphore.h>
#include <ti/sysbios/knl/Queue.h>
#include <ti/sysbios/knl/Clock.h>
#include <ti/drivers/SPI.h>
#include <ti/drivers/spi/SPICC26XXDMA.h>
#include <ti/drivers/dma/UDMACC26XX.h>
#include <string.h>
#include <stdio.h>
#include <ICall.h>
#include "Board.h"
#include "SPICommands.h"
#include "SMMain.h"

#define SPICOMMANDS_TASK_STACK_SIZE	    1024 // spiCommands task size in bytes
#define SPICOMMANDS_TASK_PRIORITY 		2 // spiCommands Priority
#define SPI_BUFFER_SIZE 				21
#define MASTER_SEND_ID 					0
#define SPI_BIT_RATE					5000000
#define SPI_CONFIG_TRANS				0
#define SPI_DATA_TRANS					1
#define SPI_CONFIG_DATA_MARKER			0xA5
#define CONFIG_RECIEVED_MARKER 			0x55

Task_Struct spiCommandsTask;
Char spiCommandsTaskStack[SPICOMMANDS_TASK_STACK_SIZE]; // mem allocation for spiCommands task stack

static ICall_EntityID spiCommandsSelfEntity;
static ICall_Semaphore spiCommandsSem;

// Queue for task messages
static Queue_Struct spiCommandsMsgQ;
static Queue_Handle hSpiCommandsMsgQ;

static SPI_Handle SPIHandle;
static SPI_Params SPIParams;
static SPI_Transaction SPITransaction;


//static PIN_Handle spiPinHandle;
//static PIN_State spiPinState;
//
//PIN_Config SPIIntPinTable[] = {
//	// set SPI interrupt pin as output and init to low
//	Board_SPIINT | PIN_GPIO_OUTPUT_EN | PIN_GPIO_LOW | PIN_PUSHPULL | PIN_DRVSTR_MAX,
//	PIN_TERMINATE
//};


static PIN_Handle intPinHandle;
static PIN_State intPinState;


PIN_Config intPinTable[] = {
	Board_SPI_CS | PIN_INPUT_EN 	  | PIN_PULLUP   | PIN_IRQ_NEGEDGE, // SPI CS interrupt
	Board_SPIINT | PIN_GPIO_OUTPUT_EN | PIN_GPIO_LOW | PIN_PUSHPULL | PIN_DRVSTR_MAX, // SPI data ready interrupt
    PIN_TERMINATE
};


 // Enum of message types
typedef enum {
	SENSOR_1_CHAR = 1,
	SENSOR_2_CHAR,
	SENSOR_3_CHAR,
	SENSOR_4_CHAR
} sensorChar_t;


// Struct for task messages
typedef struct {
  Queue_Elem _elem;
  spiCommands_msg_types_t type;
  uint8_t pdu[];
} spiCommands_msg_t;

typedef enum {
	SENSOR_1_CONFIG = 0,
	SESNOR_2_CONFIG,
	SENSOR_3_CONFIG,
	SENSOR_4_CONFIG
}sensorConfig_t;


static uint8_t SPIBufRX[SPI_BUFFER_SIZE];                  // SPI Receive and transmit buffer
static uint8_t SPIBufTX[SPI_BUFFER_SIZE];                  // SPI Receive and transmit buffer

/*--------- Function Prototypes for this task--------------*/
static void SPICommands_taskFxn(UArg a0, UArg a1);
static void SPICommands_init();
static void user_processSPICommandsMessage(spiCommands_msg_t *pMsg);
static void enqueueSPICommandsTaskMsg(spiCommands_msg_types_t msgType);
static void transferCallback(SPI_Handle handle, SPI_Transaction *transaction);
static void intPinCallbackFxn(PIN_Handle handle, PIN_Id pinId);

// Input - None
// Output - None
// Description - Initializes task. Called from main
void SPICommands_createTask(void) {
  Task_Params taskParams;
  Task_Params_init(&taskParams);
  taskParams.stack = spiCommandsTaskStack;
  taskParams.stackSize = SPICOMMANDS_TASK_STACK_SIZE;
  taskParams.priority = SPICOMMANDS_TASK_PRIORITY;
  Task_construct(&spiCommandsTask, SPICommands_taskFxn, &taskParams, NULL);
}

// Input - None
// Output - None
// Description - Initializes required resoruces for Devices Task
static void SPICommands_init() {
	// Register the semaphore for this task
	ICall_registerApp(&spiCommandsSelfEntity, &spiCommandsSem);

	// Create the task queue
	Queue_construct(&spiCommandsMsgQ, NULL);
	hSpiCommandsMsgQ = Queue_handle(&spiCommandsMsgQ);

	// Configure pins used by this task
	intPinHandle = PIN_open(&intPinState, intPinTable);
	if(!intPinHandle) {
		// TODO failed to open pin so report error
		Task_exit();
	}

	SPI_init();

	// Init SPI and specify non-default parameters
	SPI_Params_init(&SPIParams);
	SPIParams.bitRate             = SPI_BIT_RATE;
	SPIParams.frameFormat         = SPI_POL0_PHA0;
	SPIParams.mode                = SPI_SLAVE;
	SPIParams.transferMode        = SPI_MODE_CALLBACK;
	SPIParams.transferCallbackFxn = transferCallback;

	// Configure the transaction
	SPITransaction.count = SPI_BUFFER_SIZE;
	SPITransaction.txBuf = SPIBufTX;
	SPITransaction.rxBuf = SPIBufRX;

	// Open the SPI and initiate the first transfer
	SPIHandle = SPI_open(Board_SPI0, &SPIParams);

	// Setup callback for button pins
	if (PIN_registerIntCb(intPinHandle, &intPinCallbackFxn) != 0) {
		// TODO failed to register interrupt callback
		Task_exit();
	}

	//SPI_transfer(SPIHandle, &SPITransaction);
}


// Input - Pin handle, pin ID
// Output - None
// Description - Called from hardware context on interrupt
static void intPinCallbackFxn(PIN_Handle handle, PIN_Id pinId) {

	// if the interrupt was due to SPI CS going low
	if(pinId == Board_SPI_CS) {
		// Disable interrupt on that pin for now. Re-enabled after serviced
		PIN_setConfig(handle, PIN_BM_IRQ, pinId | PIN_IRQ_DIS);
		// start the SPI transfer. When finished callback will be called
		SPI_transfer(SPIHandle, &SPITransaction);
	}

}

static void transferCallback(SPI_Handle handle, SPI_Transaction *transaction) {
	uint8_t sensorCharNum = SPIBufRX[0]; // sensor number is held in first byte
	uint8_t sensorData[20] = {0};

	// check to see if this callback is due to needing to send data to master
	// if it is then the data we received was dummy data so don't do anything with it
	if(transaction->status == SPI_TRANSFER_COMPLETED) {
		memcpy(sensorData,SPIBufRX+1,20); // copy the sensor data into array

		switch(sensorCharNum) {
		// enqueue an update for the sensor char with the new data
		case SENSOR_1_CHAR:
			enqueueSensorCharUpdate(STEMMETER_SERVICE_SENSOR1DATA_UUID,sensorData);
			break;
		case SENSOR_2_CHAR:
			enqueueSensorCharUpdate(STEMMETER_SERVICE_SENSOR2DATA_UUID,sensorData);
			break;
		case SENSOR_3_CHAR:
			enqueueSensorCharUpdate(STEMMETER_SERVICE_SENSOR3DATA_UUID,sensorData);
			break;
		case SENSOR_4_CHAR:
			enqueueSensorCharUpdate(STEMMETER_SERVICE_SENSOR4DATA_UUID,sensorData);
			break;
		}
	}

	// re-enable the SPI CS interrupt
	PIN_setConfig(intPinHandle, PIN_BM_IRQ, Board_SPI_CS | PIN_IRQ_NEGEDGE);

	// start a new SPI transfer to wait for the next data
	//SPI_transfer(SPIHandle, &SPITransaction);
}

// Input - Task arguments
// Output - None
// Description - Main function for spiCommands Task
static void SPICommands_taskFxn(UArg a0, UArg a1) {
	// Allocate and init resources used for this task
	SPICommands_init();
	while(1) {
		ICall_Errno errno = ICall_wait(ICALL_TIMEOUT_FOREVER); // waits until semaphore is signaled
		while (!Queue_empty(hSpiCommandsMsgQ)) {
			spiCommands_msg_t *pMsg = Queue_dequeue(hSpiCommandsMsgQ); // dequeue the message
			user_processSPICommandsMessage(pMsg); // process the message
			ICall_free(pMsg); // free mem
		}
	}
}

// Input - Device Task Message Struct
// Output - None
// Description - Called from device task context when message dequeued
static void user_processSPICommandsMessage(spiCommands_msg_t *pMsg) {
	switch (pMsg->type) {
		case UPDATE_BAT_VALUES_MSG:

			break;
		// message from BLE task to update sensor config
		case SENSOR_UPDATE_CONFIG_MSG:
			// set last byte to show config data is correct
			SPIBufTX[20] = SPI_CONFIG_DATA_MARKER;

			// copy the incoming config data to TX buffer
			memcpy(SPIBufTX,pMsg->pdu,20);

			// Toggle interrupt line to let master know data is ready to be recieved
			// Master will then perform SPI transfer to get config data from TX buffer
			PIN_setOutputValue(intPinHandle, Board_SPIINT, 1);
			PIN_setOutputValue(intPinHandle, Board_SPIINT, 0);
			break;
	}
}

// Input - Task message type, messge data, message length
// Output - None
// Description - Enqueues message for device task
void user_enqueueRawSPICommandsMsg(spiCommands_msg_types_t deviceMsgType, uint8_t *pData, uint16_t len) {
	// Allocate memory for the message.
	spiCommands_msg_t *pMsg = ICall_malloc( sizeof(spiCommands_msg_t) + len );

	if (pMsg != NULL) {
		// set the message type
		pMsg->type = deviceMsgType;

		// Copy data into message
		if(len > 0) {
			memcpy(pMsg->pdu, pData, len);
		}

		// Enqueue the message
		Queue_enqueue(hSpiCommandsMsgQ, &pMsg->_elem);
		// Let application know there's a message.
		Semaphore_post(spiCommandsSem);
	}
}

static void enqueueSPICommandstTaskMsg(spiCommands_msg_types_t msgType) {
	spiCommands_msg_t *pMsg = ICall_malloc(sizeof(spiCommands_msg_t));
	if (pMsg != NULL) {
		pMsg->type = msgType;
		Queue_enqueue(hSpiCommandsMsgQ, &pMsg->_elem);
		Semaphore_post(spiCommandsSem);
	}
}
