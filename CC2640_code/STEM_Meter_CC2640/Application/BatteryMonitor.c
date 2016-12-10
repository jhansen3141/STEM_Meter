/******************************************************************************************
 * Filename:       BatteryMonitor.c
 * Author:		   Josh Hansen
 * Description:    BatMonitor task configures battery gas gauge IC (STC3100).
 ******************************************************************************************/
#include <ti/drivers/PIN.h>
#include <ti/drivers/i2c/I2CCC26XX.h>
#include <ti/drivers/I2C.h>
#include <xdc/runtime/Log.h>
#include <xdc/runtime/Diags.h>
#include <ti/sysbios/knl/Task.h>
#include <ti/sysbios/knl/Semaphore.h>
#include <ti/sysbios/knl/Queue.h>
#include <ti/sysbios/knl/Clock.h>
#include <stdio.h>
#include <ICall.h>
#include "Board.h"
#include "BatteryMonitor.h"
#include "SMMain.h"


#define BATMONITOR_TASK_STACK_SIZE	    1024 // BatMonitor task size in bytes
#define BATMONITOR_TASK_PRIORITY 		1 // BatMonitor Priority
#define BATT_GAUGE_ADDR				0x70 // I2C slave address for battery gauge

Task_Struct batMonitorTask;
Char batMonitorTaskStack[BATMONITOR_TASK_STACK_SIZE]; // mem allocation for batMonitor task stack

static ICall_EntityID batMonitorSelfEntity;
static ICall_Semaphore batMonitorSem;

// Queue for task messages
static Queue_Struct batMonitorMsgQ;
static Queue_Handle hBatMonitorMsgQ;

// I2C types
static I2C_Handle      I2Chandle;
static I2C_Params      i2cParams;
static I2C_Transaction masterTransaction;


// Clock objects
static Clock_Struct batteryUpdateClock;
static Clock_Handle hBatteryUpdateClock;


 // Enum of message types
typedef enum {
  BATMONITOR_MSG_UPDATE_BAT_VALUES
} batMonitor_msg_types_t;


// Struct for task messages
typedef struct {
  Queue_Elem _elem;
  batMonitor_msg_types_t type;
  uint8_t pdu[];
} batMonitor_msg_t;


/*--------- Function Prototypes for this task--------------*/
static void BatMonitor_taskFxn(UArg a0, UArg a1);
static void BatMonitor_init();
static void user_processBatMonitorMessage(batMonitor_msg_t *pMsg);
static void user_enqueueRawBatMonitorMsg(batMonitor_msg_types_t deviceMsgType, uint8_t *pData, uint16_t len);
static bool I2CWrite(uint8_t address, uint8_t *data, uint8_t len);
static bool I2CWriteSingle(uint8_t address, uint8_t data);
static bool I2CRead(uint8_t address, uint8_t *data, uint8_t len);
static bool I2CWriteRead(uint8_t address, uint8_t *wdata, uint8_t wlen, uint8_t *rdata, uint8_t rlen);
static void BattGauge_Write_Reg(uint8_t reg, uint8_t data);
static void BattGauge_On();
static void BattGauge_Off();
static float Get_Batt_Voltage();
static float Get_Tempature();
static float Get_Batt_Current();
static void updateBatteryValues();
static void FiveSecUpdate();
static void enqueueBatMonitortTaskMsg(batMonitor_msg_types_t msgType);


// Input - None
// Output - None
// Description - Initializes task. Called from main
void BatMonitor_createTask(void) {
  Task_Params taskParams;
  Task_Params_init(&taskParams);
  taskParams.stack = batMonitorTaskStack;
  taskParams.stackSize = BATMONITOR_TASK_STACK_SIZE;
  taskParams.priority = BATMONITOR_TASK_PRIORITY;
  Task_construct(&batMonitorTask, BatMonitor_taskFxn, &taskParams, NULL);
}

// Input - None
// Output - None
// Description - Initializes required resoruces for Devices Task
static void BatMonitor_init() {
	// Register the semaphore for this task
	ICall_registerApp(&batMonitorSelfEntity, &batMonitorSem);

	// Create the task queue
	Queue_construct(&batMonitorMsgQ, NULL);
	hBatMonitorMsgQ = Queue_handle(&batMonitorMsgQ);


	// Init I2C
	I2C_init();
	/* Create I2C for usage */
	I2C_Params_init(&i2cParams);
	i2cParams.bitRate = I2C_400kHz;
	i2cParams.transferMode = I2C_MODE_BLOCKING;
	I2Chandle = I2C_open(Board_I2C, &i2cParams);

	// Turn the battery gauge IC on to start monitoring battery
	BattGauge_On();

	Clock_Params clockParams;
	Clock_Params_init(&clockParams);

	clockParams.period = (5000 * (1000/Clock_tickPeriod)); // 5s battery update rate
	clockParams.startFlag = FALSE;

	Clock_construct(&batteryUpdateClock, FiveSecUpdate, 5, &clockParams);
	hBatteryUpdateClock = Clock_handle(&batteryUpdateClock);
	Clock_start(hBatteryUpdateClock);
}

// Input - Task arguments
// Output - None
// Description - Main function for BatMonitor Task
static void BatMonitor_taskFxn(UArg a0, UArg a1) {
	// Allocate and init resources used for this task
	BatMonitor_init();
	while(1) {
		ICall_Errno errno = ICall_wait(ICALL_TIMEOUT_FOREVER); // waits until semaphore is signaled

		while (!Queue_empty(hBatMonitorMsgQ)) {
			batMonitor_msg_t *pMsg = Queue_dequeue(hBatMonitorMsgQ); // dequeue the message
			user_processBatMonitorMessage(pMsg); // process the message
			ICall_free(pMsg); // free mem
		}
	}
}

// Input - Device Task Message Struct
// Output - None
// Description - Called from device task context when message dequeued
static void user_processBatMonitorMessage(batMonitor_msg_t *pMsg) {
	switch (pMsg->type) {
		case BATMONITOR_MSG_UPDATE_BAT_VALUES:
			updateBatteryValues();
			break;
	}
}


// Input - Task message type, messge data, message length
// Output - None
// Description - Enqueues message for device task
static void user_enqueueRawBatMonitorMsg(batMonitor_msg_types_t deviceMsgType, uint8_t *pData, uint16_t len) {
	// Allocate memory for the message.
	batMonitor_msg_t *pMsg = ICall_malloc( sizeof(batMonitor_msg_t) + len );

	if (pMsg != NULL) {
		// set the message type
		pMsg->type = deviceMsgType;

		// Copy data into message
		if(len > 0) {
			memcpy(pMsg->pdu, pData, len);
		}

		// Enqueue the message
		Queue_enqueue(hBatMonitorMsgQ, &pMsg->_elem);
		// Let application know there's a message.
		Semaphore_post(batMonitorSem);
	}
}

static void enqueueBatMonitortTaskMsg(batMonitor_msg_types_t msgType) {
	batMonitor_msg_t *pMsg = ICall_malloc(sizeof(batMonitor_msg_t));
	if (pMsg != NULL) {
		pMsg->type = msgType;
		Queue_enqueue(hBatMonitorMsgQ, &pMsg->_elem);
		Semaphore_post(batMonitorSem);
	}
}

// Input - I2C slave address, I2C data to write, Data length
// Output - Successful boolean
// Description - Writes given number of bytes to I2C device at slave address
static bool I2CWrite(uint8_t address, uint8_t *data, uint8_t len) {
	masterTransaction.writeCount   = len;
	masterTransaction.writeBuf     = data;
	masterTransaction.readCount    = 0;
	masterTransaction.readBuf      = NULL;
	masterTransaction.slaveAddress = address;
	return I2C_transfer(I2Chandle, &masterTransaction) == TRUE;
}

// Input - I2C slave address, I2C data to write, Write data length
//		   Pointer where to store read data, Read data length
// Output - Successful boolean
// Description - I2C write and read in one function
static bool I2CWriteRead(uint8_t address, uint8_t *wdata, uint8_t wlen, uint8_t *rdata, uint8_t rlen) {
  masterTransaction.writeCount   = wlen;
  masterTransaction.writeBuf     = wdata;
  masterTransaction.readCount    = rlen;
  masterTransaction.readBuf      = rdata;
  masterTransaction.slaveAddress = address;
  return I2C_transfer(I2Chandle, &masterTransaction) == TRUE;
}


// Input - Register to write to battery gauge, data to write
// Output - None
// Description - Writes a value to batt gauge register
static void BattGauge_Write_Reg(uint8_t reg, uint8_t data) {
	uint8_t dataCombined[2];
	dataCombined[0] = reg;
	dataCombined[1] = data;
	I2CWrite(BATT_GAUGE_ADDR, dataCombined, 2);
}

// Input -None
// Output - None
// Description - Takes battery gauge IC out of standby
static void BattGauge_On() {
	// take bat gauge out of standby
	BattGauge_Write_Reg(BATT_GAUGE_REG_MODE,0x10);
}

// Input - None
// Output - Battery voltage as a float
// Description - Reads current battery voltage from battery gauge IC
static float Get_Batt_Voltage() {
	uint8_t returnData[2];
	uint8_t regAddress = BATT_GAUGE_REG_VOLTAGE_LOW;
	uint16_t combinedData;
	I2CWriteRead(BATT_GAUGE_ADDR,&regAddress,1,returnData,2);
	combinedData = (uint16_t)(returnData[1] << 8) | returnData[0];
	return ((float)combinedData * 2.44f)/1000;
}

// Input - None
// Output - Battery tempature as float
// Description - Reads current battery tempature from battery gauge
static float Get_Tempature() {
	uint8_t returnData[2];
	uint8_t regAddress = BATT_GAUGE_REG_TEMP_LOW;
	uint16_t combinedData;
	I2CWriteRead(BATT_GAUGE_ADDR,&regAddress,1,returnData,2);
	// Read the two bytes of data from IC then combine into 16-bit value
	combinedData = (uint16_t)(returnData[1] << 8) | returnData[0];
	// multiply by temp per LSB then convert to F
	return ((float)combinedData * 0.125f) * 1.8f + 32.0f;
}

// Input - None
// Output - Battery current draw
// Description - Reads current battery current draw
static float Get_Batt_Current() {
	float current;
	uint8_t returnData[2];
	uint8_t regAddress = BATT_GAUGE_REG_CURRENT_LOW;
	int16_t combinedData;
	I2CWriteRead(BATT_GAUGE_ADDR,&regAddress,1,returnData,2);
	combinedData = (int8_t)((uint16_t)(returnData[1] << 8) | returnData[0]);
	current = (((float)combinedData * 11.77f)/0.03);
	return current / 1000.0f;
}

// Called by clock every 5seconds
static void FiveSecUpdate() {
	// send upadted bat values to BLE task every 5 seconds
	enqueueBatMonitortTaskMsg(BATMONITOR_MSG_UPDATE_BAT_VALUES);
}

// Input - None
// Output - None
// Description - Retrives current battery data from bat monitor IC
// then sends that updated data to the BLE task to be read by the Android app
static void updateBatteryValues() {
	char batteryString[20] = {0};
	float temp = Get_Tempature();
	float voltage = Get_Batt_Voltage();
	float current = Get_Batt_Current();
	sprintf(batteryString,"%.2f;%.2f;%.2f",voltage,current,temp);
	// send updated values to BLE task
	enqueueBatteryCharUpdate((uint8_t *)batteryString);
}

