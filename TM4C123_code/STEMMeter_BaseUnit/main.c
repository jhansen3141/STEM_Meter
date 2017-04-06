/*
* Author: Josh Hansen
* Project: STEM-Meter Base Unit
* Last Updated: April. 4, 2017
* File: main.c
* Desc: Main function. Creates all tasks then starts BIOS
*/

/* BIOS Header files */
#include <ti/sysbios/BIOS.h>
#include <ti/sysbios/knl/Clock.h>
#include <ti/sysbios/knl/Task.h>

#include <ti/drivers/GPIO.h>
#include <ti/drivers/SDSPI.h>

#include "Board.h"
#include "main.h"


int main(void) {

   // Call board init functions
    Board_initGeneral();
    Board_initGPIO();
    Board_initSDSPI();
    Board_initSPI();
    Board_initUART();

    SDCard_createTask();

    BLEWrite_createTask();

    Sensor1_createTask();

    Sensor2_createTask();

    Sensor3_createTask();

    Sensor4_createTask();

    BIOS_start();

    return (0);
}
