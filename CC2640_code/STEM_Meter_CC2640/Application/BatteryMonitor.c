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
#include "SPICommands.h"

#define BATMONITOR_TASK_STACK_SIZE	    950 // BatMonitor task size in bytes
#define BATMONITOR_TASK_PRIORITY 		2 // BatMonitor Priority
#define BATT_GAUGE_ADDR				    0x70 // I2C slave address for battery gauge
#define BUTTON_PRESS					0x01
#define BUTTON_LONG_PRESS				0x02

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

static Clock_Struct connectionStatusClock;
static Clock_Handle hconnectionStatusClock;

static Clock_Struct buttonDebounceClock;
static Clock_Handle hButtonDebounceClock;

static Clock_Struct buttonLongPressClock;
static Clock_Handle hButtonLongPressClock;

static PIN_Handle ledPinHandle;
static PIN_State ledPinState;

PIN_Config ledPinTable[] = {
	Board_RED_SLED| PIN_GPIO_OUTPUT_EN | PIN_GPIO_HIGH | PIN_PUSHPULL | PIN_DRVSTR_MAX, // Red Status LED
	Board_BLU_SLED| PIN_GPIO_OUTPUT_EN | PIN_GPIO_HIGH | PIN_PUSHPULL | PIN_DRVSTR_MAX, // Blue Status LED
	Board_GRN_SLED| PIN_GPIO_OUTPUT_EN | PIN_GPIO_HIGH | PIN_PUSHPULL | PIN_DRVSTR_MAX, // Green Status LED
    PIN_TERMINATE
};

static PIN_Handle btnPinHandle;
static PIN_State btnPinState;

PIN_Config btnPinTable[] = {
	Board_BUTTON | PIN_INPUT_EN | PIN_NOPULL | PIN_IRQ_POSEDGE, // Button interrupt pin
    PIN_TERMINATE
};


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
static bool I2CWrite(uint8_t address, uint8_t *data, uint8_t len);
static bool I2CWriteRead(uint8_t address, uint8_t *wdata, uint8_t wlen, uint8_t *rdata, uint8_t rlen);
static void BattGauge_On();
static float Get_Batt_Voltage();
static float Get_Tempature();
static float Get_Batt_Current();
static void updateBatteryValues();
static void FiveSecUpdate();
static void toggleBlueLED();
static void user_buttonClockCallBack(UArg buttonId);
static void btnPinCallbackFxn(PIN_Handle handle, PIN_Id pinId);

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

	// Configure pins used by this task
	ledPinHandle = PIN_open(&ledPinState, ledPinTable);

	btnPinHandle = PIN_open(&btnPinState, btnPinTable);

	// Init I2C
	I2C_init();
	/* Create I2C for usage */
	I2C_Params_init(&i2cParams);
	i2cParams.bitRate = I2C_400kHz;
	i2cParams.transferMode = I2C_MODE_BLOCKING;
	I2Chandle = I2C_open(Board_I2C, &i2cParams);

	Clock_Params clockParams;
	Clock_Params buttonClockParams;
	Clock_Params_init(&clockParams);
	Clock_Params_init(&buttonClockParams);

	clockParams.period = (5000 * (1000/Clock_tickPeriod)); // 5s battery update rate
	clockParams.startFlag = FALSE;
	Clock_construct(&batteryUpdateClock, FiveSecUpdate, 5, &clockParams);
	hBatteryUpdateClock = Clock_handle(&batteryUpdateClock);

	clockParams.period = (500 * (1000/Clock_tickPeriod)); // Blue LED Flash rate
	clockParams.startFlag = FALSE;
	Clock_construct(&connectionStatusClock, toggleBlueLED, 5, &clockParams);
	hconnectionStatusClock = Clock_handle(&connectionStatusClock);

	buttonClockParams.arg = BUTTON_PRESS;
	Clock_construct(&buttonDebounceClock, user_buttonClockCallBack,
				    1000 * (1000/Clock_tickPeriod), &buttonClockParams);
	hButtonDebounceClock = Clock_handle(&buttonDebounceClock);

	buttonClockParams.arg = BUTTON_LONG_PRESS;
	Clock_construct(&buttonLongPressClock, user_buttonClockCallBack,
					3000 * (1000/Clock_tickPeriod), &buttonClockParams);
	hButtonLongPressClock = Clock_handle(&buttonLongPressClock);

	// Setup callback for button interrupt
	PIN_registerIntCb(btnPinHandle, &btnPinCallbackFxn);

	BattGauge_On();
	Task_sleep(1000);
	BattGauge_On();

	Clock_start(hBatteryUpdateClock);

	// start blinking blue LED on start (advertising)
	Clock_start(hconnectionStatusClock);
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
		case BATMONITOR_MSG_RED_LED_ON:
			// Make RED and Green LED mutually exclusive
			// turn Green led off
			PIN_setOutputValue(ledPinHandle, Board_GRN_SLED, 1);
			// turn Red led on
			PIN_setOutputValue(ledPinHandle, Board_RED_SLED, 0);
			break;
		case BATMONITOR_MSG_GRN_LED_ON:
			// turn Red led off
			PIN_setOutputValue(ledPinHandle, Board_RED_SLED, 1);
			// turn green led on
			PIN_setOutputValue(ledPinHandle, Board_GRN_SLED, 0);
			break;
		case BATMONITOR_MSG_BLU_LED_ON:
			// if the blue toggle clock is going, stop it
			Clock_stop(hconnectionStatusClock);
			PIN_setOutputValue(ledPinHandle, Board_BLU_SLED, 0);
			break;
		case BATMONITOR_MSG_RED_LED_OFF:
			PIN_setOutputValue(ledPinHandle, Board_RED_SLED, 1);
			break;
		case BATMONITOR_MSG_GRN_LED_OFF:
			PIN_setOutputValue(ledPinHandle, Board_GRN_SLED, 1);
			break;
		case BATMONITOR_MSG_BLU_LED_OFF:
			// if the blue toggle clock is going, stop it
			Clock_stop(hconnectionStatusClock);
			PIN_setOutputValue(ledPinHandle, Board_BLU_SLED, 1);
			break;
		case BATMONITOR_MSG_BLE_LEG_TOGGLE:
			// start the clock to toggle blue LED
			Clock_start(hconnectionStatusClock);
			break;
	}
}

// Button interrupt callback. Called in hardware context
static void btnPinCallbackFxn(PIN_Handle handle, PIN_Id pinId) {
	// Disable interrupt on that pin for now.
	PIN_setConfig(handle, PIN_BM_IRQ, pinId | PIN_IRQ_DIS);
	// Start the button debounce clock
	Clock_start(hButtonDebounceClock);
	// Start the button long clock
	Clock_start(hButtonLongPressClock);
}

// called when the debounce clock expires
static void user_buttonClockCallBack(UArg buttonId) {

	// Get current value of the button pin after the clock timeout
	uint8_t buttonPinVal = PIN_getInputValue(Board_BUTTON);

	if(buttonId == BUTTON_PRESS) {
		if(!buttonPinVal) {
			// button was released after 1s
			enqueueBLEMainMsg(APP_MSG_TOGGLE_ADVERTISING);
		}

	}
	else if(buttonId == BUTTON_LONG_PRESS) {
		if(buttonPinVal) {
			// if button is still pressed then do long press action
			enqueueSPICommandstTaskMsg(TOGGLE_SD_MOUNT_MSG);
		}
	}

	// Enable positive edge interrupts to wait for press
	PIN_setConfig(btnPinHandle, PIN_BM_IRQ, Board_BUTTON | PIN_IRQ_POSEDGE);
}



void enqueueBatMonitortTaskMsg(batMonitor_msg_types_t msgType) {
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

// Input -None
// Output - None
// Description - Takes battery gauge IC out of standby
static void BattGauge_On() {
	uint8_t dataCombined[2];
	dataCombined[0] = BATT_GAUGE_REG_MODE;
	dataCombined[1] = 0x10;
	I2CWrite(BATT_GAUGE_ADDR, dataCombined, 2);
}

// Input - None
// Output - Battery voltage as a float
// Description - Reads current battery voltage from battery gauge IC
static float Get_Batt_Voltage() {
	uint8_t returnData[2];
	uint8_t voltageRegAddress = BATT_GAUGE_REG_VOLTAGE_LOW;
	uint16_t combinedData;
	I2CWriteRead(BATT_GAUGE_ADDR,&voltageRegAddress,1,returnData,2);
	combinedData = ( ( (uint16_t)returnData[1] ) << 8 ) | returnData[0];
	return ( (float)combinedData * 2.44f ) / 1000.0f;
}

// Input - None
// Output - Battery tempature as float
// Description - Reads current battery tempature from battery gauge
static float Get_Tempature() {
	uint8_t returnData[2];
	uint8_t tempRegAddress = BATT_GAUGE_REG_TEMP_LOW;
	uint16_t combinedData;
	I2CWriteRead(BATT_GAUGE_ADDR,&tempRegAddress,1,returnData,2);
	// Read the two bytes of data from IC then combine into 16-bit value
	combinedData = ( ( (int16_t)returnData[1] ) << 8 ) | returnData[0];
	// multiply by temp per LSB then convert to F
	return ((float)combinedData * 0.125f) * 1.8f + 32.0f;
}

// Input - None
// Output - Battery current draw
// Description - Reads current battery current draw
static float Get_Batt_Current() {
	float current;
	uint8_t returnData[2];
	uint8_t currentRegAddress = BATT_GAUGE_REG_CURRENT_LOW;
	int16_t combinedData;
	I2CWriteRead(BATT_GAUGE_ADDR,&currentRegAddress,1,returnData,2);
	combinedData = ( ( (int16_t)returnData[1] ) << 8 ) | returnData[0];
	current = ( ( (float)combinedData * 11.77f ) / 0.03f );
	return (current / 1000.0f);
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
	BattGauge_On();
	uint8_t batteryString[20] = {0};
	float temp = Get_Tempature();

	float voltage = Get_Batt_Voltage();

	float current = Get_Batt_Current();

	snprintf((char *)batteryString,20,"%.2f;%.2f;%.2f",voltage,current,temp);

	// send updated values to BLE task
	enqueueSensorCharUpdate(STEMMETER_SERVICE_BATTERYDATA_UUID, batteryString);
	//enqueueBatteryCharUpdate((uint8_t *)batteryString);
}

static void toggleBlueLED() {
	static uint8_t ledStatus = 0;
	PIN_setOutputValue(ledPinHandle, Board_BLU_SLED, ledStatus);
	ledStatus ^= 1;
}



