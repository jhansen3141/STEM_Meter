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

const char outputfile[] = "fat:"STR(DRIVE_NUM)":data.bin";
static SDSPI_Handle sdspiHandle;
static SDSPI_Params sdspiParams;


/* Function Prototypes */
static void SDCardFxn(UArg arg0, UArg arg1);
static void SDCard_Init();


void SDCard_createTask(void) {
    Task_Params taskParams;
    /* Construct file copy Task thread */
    Task_Params_init(&taskParams);
    taskParams.stackSize = TASKSTACKSIZE;
    taskParams.stack = &SDCardTaskStack;
    Task_construct(&SDCardTaskStruct, (Task_FuncPtr)SDCardFxn, &taskParams, NULL);
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
	initTime.day = 23;
	initTime.dow = 5;
	initTime.month = 8;
	initTime.year = 2016;
	initTime.hour = 8;
	initTime.minutes = 30;
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
	// Construct a Semaphore object to be used as a resource lock, inital count 0
	Semaphore_Params_init(&semSDParams);
	Semaphore_construct(&semSDStruct, 0, &semSDParams);

	// assign semaphore handle
	semSDHandle = Semaphore_handle(&semSDStruct);

	// create the task queue
	Queue_construct(&SDMsgQ, NULL);
	hSDMsgQ = Queue_handle(&SDMsgQ);

}
static void SDCardFxn(UArg arg0, UArg arg1) {
	FILE *dataFile;
	char timeStr[6] = {0};
	uint32_t bytesWritten;
	SDCard_Init();

	UTCTimeStruct time;
	UTC_convertUTCTime(&time, UTC_getClock());
	sprintf(timeStr,"%02d:%02d",time.hour,time.minutes);

	// open the output file for writing
	dataFile = fopen(outputfile, "w");
	// check to see if file opened
	if (dataFile) {
		// fwrite(dataToWrite, size in bytes of each element, number of elements, file object)
		bytesWritten = fwrite(timeStr, 1, 6, dataFile);

		// flush the stream buffer
		fflush(dataFile);

		// close the data file
		fclose(dataFile);
	}


	while(1) {
		// block until work to do
		Semaphore_pend(semSDHandle, BIOS_WAIT_FOREVER);

		while (!Queue_empty(hSDMsgQ)) {

		}
	}
}


