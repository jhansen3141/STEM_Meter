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
#include "Sensor.h"

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

#define TIME_STR_LEN		17

Task_Struct SDCardTaskStruct;
Char SDCardTaskStack[TASKSTACKSIZE];

// Queue for task messages
static Queue_Struct SDMsgQ;
static Queue_Handle hSDMsgQ;

// Semaphore for task
static Semaphore_Struct semSDStruct;
static Semaphore_Handle semSDHandle;

const char outputfile[] = "fat:"STR(DRIVE_NUM)":SM_Data.csv";
bool SDCardInserted = false;
bool SDMasterWriteEnabled = true;
static FILE *dataFile;
static SDSPI_Handle sdspiHandle;
static SDSPI_Params sdspiParams;

static bool sensorAttached[4] = {false,false,false,false};
static uint8_t sensorNumDataPoints[4] = {0,0,0,0};
static uint8_t totalCommas = 1;

typedef struct {
  Queue_Elem _elem;
  SD_msg_types_t type;
  uint8_t pdu[];
} SD_msg_t;


/* Function Prototypes */
static void SDCardFxn(UArg arg0, UArg arg1);
static void SDCard_Init();
static void user_processSDMessage(SD_msg_t *pMsg);
static void writeSensorDataToSD(uint8_t sNum, SD_msg_t *sData);
static void getTimeStr(char *timeStr);
static void writeCommas(uint8_t sNum);

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
}


static void SDCardFxn(UArg arg0, UArg arg1) {
	SDCard_Init();

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

static void getTimeStr(char *timeStr) {
	UTCTimeStruct time;
	UTC_convertUTCTime(&time, UTC_getClock());
	sprintf(timeStr,"%02d-%02d-%02d %02d:%02d:%02d",
			time.month,time.day,(uint8_t)(time.year - 2000),
			time.hour,time.minutes,time.seconds);
}

static void writeCommas(uint8_t sNum) {
	uint8_t numCommas = sensorNumDataPoints[sNum-1];
	char commaStr[20];
	uint8_t i;

	// set string to null
	memset(commaStr,0,20);


	if(numCommas != 0) {
		// create string with commas needed
		for(i=0; i<numCommas; i++) {
			strcat(commaStr,",");
		}

		// write the string with commas
		fwrite(commaStr, 1, numCommas, dataFile);
	}
}


static void writeSensorDataToSD(uint8_t sNum, SD_msg_t *sData) {
	 uint8_t numDataPoints = sData->pdu[0];
	 uint8_t dataStrLen = strlen((const char*)(sData->pdu+STR_META_DATA_LEN));
	 uint8_t sNameLen = strlen((const char*)(sData->pdu+STR_NAME_OFFSET));
	 char timeString[TIME_STR_LEN];

	// check to see if master write is enabled
	if(SDMasterWriteEnabled) {
		// open the output file for writing (appending data)
		dataFile = fopen(outputfile, "a");

		// check to see if file opened
		if (dataFile) {
			// if new sensor was attached
			if(sensorAttached[sNum-1] == false) {
				sensorAttached[sNum-1] = true;

				sensorNumDataPoints[sNum-1] = totalCommas;
				// write the number of columns
				writeCommas(sNum);

				// write the title of the column
				// column str starts at sensor string + 1
				fwrite(sData->pdu+STR_NAME_OFFSET, 1, sNameLen, dataFile);
				fwrite("\n",1,1,dataFile);

				totalCommas += numDataPoints;
			}
			// get the current time as a string
			getTimeStr(timeString);

			// write the time string
			fwrite(timeString, 1, TIME_STR_LEN, dataFile);

			// write the number of commas needed
			writeCommas(sNum);

			// write the actual sensor data string
			fwrite(sData->pdu+STR_META_DATA_LEN, 1, dataStrLen, dataFile);

			// flush the stream buffer
			fflush(dataFile);

			// close the data file
			fclose(dataFile);
		}
	}
}

static void user_processSDMessage(SD_msg_t *pMsg) {

	switch (pMsg->type) {
		case WRITE_S1_TO_SD_MSG:
			writeSensorDataToSD(1,pMsg);
			break;
		case WRITE_S2_TO_SD_MSG:
			writeSensorDataToSD(2,pMsg);
			break;
		case WRITE_S3_TO_SD_MSG:
			writeSensorDataToSD(3,pMsg);
			break;
		case WRITE_S4_TO_SD_MSG:
			writeSensorDataToSD(4,pMsg);
			break;
		case WRITE_SENSOR_STR_MSG:
			break;
	}
}

void enqueueSDTaskMsg(SD_msg_types_t msgType, uint8_t *buffer) {
	// create a new message for the queue
	SD_msg_t *pMsg = malloc(sizeof(SD_msg_t) + SD_CARD_DATA_LEN);
	if (pMsg != NULL) {
		pMsg->type = msgType;
		memcpy(pMsg->pdu,buffer,SD_CARD_DATA_LEN); // copy the data into the message
		Queue_enqueue(hSDMsgQ, &pMsg->_elem); // enqueue the message
		Semaphore_post(semSDHandle);
	}
}


