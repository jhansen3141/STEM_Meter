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
#include "BatteryMonitor.h"

#define SPICOMMANDS_TASK_STACK_SIZE	    1300 // spiCommands task size in bytes
#define SPICOMMANDS_TASK_PRIORITY 		1 // spiCommands Priority
#define SPI_BUFFER_SIZE 				21
#define MASTER_SEND_ID 					0
#define SPI_BIT_RATE					5000000 // 10MHz
#define SPI_CONFIG_TRANS				0
#define SPI_DATA_TRANS					1
#define SPI_CONFIG_DATA_MARKER			0xA5
#define SPI_SD_TOGGLE_MARKER			0x8A
#define SPI_SET_TIME_MARKER				0x6C

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

static PIN_Handle intPinHandle;
static PIN_State intPinState;

PIN_Config intPinTable[] = {
	Board_SPI_CS | PIN_INPUT_EN 	  | PIN_PULLUP   | PIN_IRQ_NEGEDGE, // SPI CS interrupt
	Board_SPIINT | PIN_GPIO_OUTPUT_EN | PIN_GPIO_LOW | PIN_PUSHPULL | PIN_DRVSTR_MAX, // SPI data ready interrupt
    PIN_TERMINATE
};

// Enum of message types
typedef enum {
	INVALID_ID = 0,
	SENSOR_1_ID,
	SENSOR_2_ID,
	SENSOR_3_ID,
	SENSOR_4_ID,
	CHARGE_START_ID,
	CHARGE_STOP_ID,
	CHARGE_FULL_ID,
	CHARGE_NOT_FULL_ID
} spiMsgID_t;

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
	// sensor number is held in first byte
	uint8_t messageID = SPIBufRX[0];
	//uint8_t sensorData[20] = {0};

	if(transaction->status == SPI_TRANSFER_COMPLETED) {

		switch(messageID) {
		// enqueue an update for the sensor char with the new data
		case SENSOR_1_ID:
			// copy the sensor data into array minus the first byte which is sen num
			//memcpy(sensorData,SPIBufRX+1,20);
			enqueueSensorCharUpdate(STEMMETER_SERVICE_SENSOR1DATA_UUID,SPIBufRX+1);
			break;
		case SENSOR_2_ID:
			//memcpy(sensorData,SPIBufRX+1,20);
			enqueueSensorCharUpdate(STEMMETER_SERVICE_SENSOR2DATA_UUID,SPIBufRX+1);
			break;
		case SENSOR_3_ID:
			//memcpy(sensorData,SPIBufRX+1,20);
			enqueueSensorCharUpdate(STEMMETER_SERVICE_SENSOR3DATA_UUID,SPIBufRX+1);
			break;
		case SENSOR_4_ID:
			//memcpy(sensorData,SPIBufRX+1,20);
			enqueueSensorCharUpdate(STEMMETER_SERVICE_SENSOR4DATA_UUID,SPIBufRX+1);
			break;
		case CHARGE_START_ID:
			enqueueBatMonitortTaskMsg(BATMONITOR_MSG_RED_LED_ON);
			break;
		case CHARGE_STOP_ID:
			enqueueBatMonitortTaskMsg(BATMONITOR_MSG_RED_LED_OFF);
			break;
		case CHARGE_FULL_ID:
			enqueueBatMonitortTaskMsg(BATMONITOR_MSG_GRN_LED_ON);
			break;
		case CHARGE_NOT_FULL_ID:
			enqueueBatMonitortTaskMsg(BATMONITOR_MSG_GRN_LED_OFF);
			break;
		}
	}

	// re-enable the SPI CS interrupt
	PIN_setConfig(intPinHandle, PIN_BM_IRQ, Board_SPI_CS | PIN_IRQ_NEGEDGE);
}

// Input - Task arguments
// Output - None
// Description - Main function for spiCommands Task
static void SPICommands_taskFxn(UArg a0, UArg a1) {
	// Allocate and init resources used for this task
	SPICommands_init();
	while(1) {
		// waits until semaphore is signaled
		ICall_Errno errno = ICall_wait(ICALL_TIMEOUT_FOREVER);
		while (!Queue_empty(hSpiCommandsMsgQ)) {
			// dequeue the message
			spiCommands_msg_t *pMsg = Queue_dequeue(hSpiCommandsMsgQ);
			// process the message
			user_processSPICommandsMessage(pMsg);
			// free mem
			ICall_free(pMsg);
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
		{
			// copy the incoming config data to TX buffer
			memcpy(SPIBufTX,pMsg->pdu,20);

			// set last byte to show config data is correct
			SPIBufTX[20] = SPI_CONFIG_DATA_MARKER;

			// Toggle interrupt line to let master know data is ready to be recieved
			// Master will then perform SPI transfer to get config data from TX buffer
			PIN_setOutputValue(intPinHandle, Board_SPIINT, 1);
			PIN_setOutputValue(intPinHandle, Board_SPIINT, 0);
		}
			break;

		case TOGGLE_SD_MOUNT_MSG:
		{
			memset(SPIBufTX,0,20);

			SPIBufTX[20]  = SPI_SD_TOGGLE_MARKER;

			// Toggle interrupt line to let master know data is ready to be recieved
			// Master will then perform SPI transfer to get config data from TX buffer
			PIN_setOutputValue(intPinHandle, Board_SPIINT, 1);
			PIN_setOutputValue(intPinHandle, Board_SPIINT, 0);
		}
			break;

		case UPDATE_TIME_MSG:
		{
			// copy the incoming config data to TX buffer
			memcpy(SPIBufTX,pMsg->pdu,20);

			// set last byte to show config data is correct
			SPIBufTX[20] = SPI_SET_TIME_MARKER;

			// Toggle interrupt line to let master know data is ready to be recieved
			// Master will then perform SPI transfer to get time data
			PIN_setOutputValue(intPinHandle, Board_SPIINT, 1);
			Task_sleep(1);
			PIN_setOutputValue(intPinHandle, Board_SPIINT, 0);
		}
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

void enqueueSPICommandstTaskMsg(spiCommands_msg_types_t msgType) {
	spiCommands_msg_t *pMsg = ICall_malloc(sizeof(spiCommands_msg_t));
	if (pMsg != NULL) {
		pMsg->type = msgType;
		Queue_enqueue(hSpiCommandsMsgQ, &pMsg->_elem);
		Semaphore_post(spiCommandsSem);
	}
}
