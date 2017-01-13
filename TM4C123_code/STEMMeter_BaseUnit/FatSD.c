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
#include <ti/drivers/SDSPI.h>

/* Example/Board Header files */
#include "Board.h"
#include "time_clock.h"
#include "FatSD.h"

/* Buffer size used for the file copy process */
#ifndef CPY_BUFF_SIZE
#define CPY_BUFF_SIZE       2048
#endif

/* String conversion macro */
#define STR_(n)             #n
#define STR(n)              STR_(n)

/* Drive number used for FatFs */
#define DRIVE_NUM           0

#define TASKSTACKSIZE       2048

Task_Struct SDCardTaskStruct;
Char SDCardTaskStack[TASKSTACKSIZE];

// Queue for task messages
static Queue_Struct SDMsgQ;
static Queue_Handle hSDMsgQ;

// Semaphore for task
static Semaphore_Struct semSDStruct;
static Semaphore_Handle semSDHandle;

const char outputfile[] = "fat:"STR(DRIVE_NUM)":SM_Data.bin";
bool SDCardInserted = false;
bool SDMasterWriteEnabled = true;
static FILE *dataFile;
static SDSPI_Handle sdspiHandle;
static SDSPI_Params sdspiParams;

typedef struct {
  Queue_Elem _elem;
  SD_msg_types_t type;
  uint16_t length;
  uint8_t pdu[];
} SD_msg_t;


/* Function Prototypes */
static void SDCardFxn(UArg arg0, UArg arg1);
static void SDCard_Init();
static void user_processSDMessage(SD_msg_t *pMsg);
static void SDCardDetectInterrupt(unsigned int index);


void SDCard_createTask(void) {
    Task_Params taskParams;
    /* Construct file copy Task thread */
    Task_Params_init(&taskParams);
    taskParams.stackSize = TASKSTACKSIZE;
    taskParams.stack = &SDCardTaskStack;
    Task_construct(&SDCardTaskStruct,
		(Task_FuncPtr)SDCardFxn, &taskParams, NULL);
}


// function to return the current date for file writes
uint32_t fatTimeHook() {
	UTCTimeStruct time;
	// get the current time
	UTC_convertUTCTime(&time, UTC_getClock());

	return  (((time.year)-1980) << 25)
			| ((time.month+1) << 21)
			| (time.day << 16)
			| ((time.hour-1) << 11)
			| (time.minutes << 5)
			| (time.seconds >> 1)
			;
}

static void SDCardDetectInterrupt(unsigned int index) {
	// disable the interrupt until finished
	GPIO_disableInt(Board_SD_CARD_INT);

	// if pin is high then card was removed
	if(GPIO_read(Board_SD_CARD_INT)) {
		SDCardInserted = false;
	}
	// otherwise it was inserted
	else {
		SDCardInserted = true;
	}

	GPIO_enableInt(Board_SD_CARD_INT);
}

static void SDCard_Init() {
	Time_clockInit(); // init the RTC

	UTCTimeStruct initTime;
	initTime.day = 1;
	initTime.dow = 1;
	initTime.month = 1;
	initTime.year = 2017;
	initTime.hour = 8;
	initTime.minutes = 0;
	initTime.seconds = 0;
	Time_clockSetTimeStruct(initTime); // set an intial time

	/* Mount and register the SD Card */
	SDSPI_Params_init(&sdspiParams);
	sdspiHandle = SDSPI_open(Board_SDSPI0, DRIVE_NUM, &sdspiParams);
	if (sdspiHandle == NULL) {
		// Can't open the SDSPI driver so exit
		Task_exit();
	}

	Semaphore_Params semSDParams;
	// Construct a Semaphore object to be used as a resource lock
	Semaphore_Params_init(&semSDParams);
	Semaphore_construct(&semSDStruct, 0, &semSDParams);

	// assign semaphore handle
	semSDHandle = Semaphore_handle(&semSDStruct);

	// create the task queue
	Queue_construct(&SDMsgQ, NULL);
	hSDMsgQ = Queue_handle(&SDMsgQ);

	// setup SD Card Detect Interrupt callback
	GPIO_setCallback(Board_SD_CARD_INT, SDCardDetectInterrupt);

	// Enable interrupt
	// Not working now due to issue with PCB layout
	// Using software to detect card insert
	//GPIO_enableInt(Board_SD_CARD_INT);

}

uint16_t SDWriteTime() {
	uint16_t bytesWritten;
	uint8_t timeData[10] = {0};

	UTCTimeStruct time;
	UTC_convertUTCTime(&time, UTC_getClock());

	// set up time markers
	timeData[0] = 0xAA;
	timeData[1] = 0xBB;
	timeData[2] = 0xCC;
	timeData[3] = time.month;
	timeData[4] = time.day;
	timeData[5] = (uint8_t)(time.year - 2000);
	timeData[6] = time.hour;
	timeData[7] = time.minutes;
	timeData[8] = time.seconds;
	timeData[9] = 0xDD;

	// open the output file for writing (appending data)
	dataFile = fopen(outputfile, "a");
	// check to see if file opened
	if (dataFile) {
		// fwrite(dataToWrite, size in bytes of each element,
		// number of elements, file object)
		bytesWritten = fwrite(timeData, 1, 10, dataFile);

		// flush the stream buffer
		fflush(dataFile);

		// close the data file
		fclose(dataFile);
		SDMasterWriteEnabled = true;
	}
	else {
		SDMasterWriteEnabled = false;
	}

	return bytesWritten;
}
static void SDCardFxn(UArg arg0, UArg arg1) {
	SDCard_Init();
	SDWriteTime();

	while(1) {
		// block until work to do
		Semaphore_pend(semSDHandle, BIOS_WAIT_FOREVER);

		while (!Queue_empty(hSDMsgQ)) {
			SD_msg_t *pMsg = Queue_dequeue(hSDMsgQ); // dequeue the message
			user_processSDMessage(pMsg); // process the message
			free(pMsg); // free mem
		}
	}
}

static void user_processSDMessage(SD_msg_t *pMsg) {

	switch (pMsg->type) {
		case WRITE_TO_SD_MSG:
			// check to see if master write is enabled
			if(SDMasterWriteEnabled) {
				dataFile = fopen(outputfile, "a");
				// check to see if file opened
				if (dataFile) {
					GPIO_write(Board_SENSOR_1_LED, Board_LED_ON);

					fwrite(pMsg->pdu, 1, pMsg->length, dataFile);
					// flush the stream buffer
					fflush(dataFile);
					// close the data file
					fclose(dataFile);
					GPIO_write(Board_SENSOR_1_LED, Board_LED_OFF);
				}
			}
			break;
	}
}

void enqueueSDTaskMsg(SD_msg_types_t msgType, uint8_t *buffer, uint16_t len) {
	// create a new message for the queue
	SD_msg_t *pMsg = malloc(sizeof(SD_msg_t) + len);
	if (pMsg != NULL) {
		pMsg->type = msgType;
		pMsg->length = len;
		memcpy(pMsg->pdu,buffer,len); // copy the data into the message
		Queue_enqueue(hSDMsgQ, &pMsg->_elem); // enqueue the message
		Semaphore_post(semSDHandle);
	}
}


