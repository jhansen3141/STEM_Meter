/* BIOS Header files */
#include <ti/sysbios/BIOS.h>
#include <ti/sysbios/knl/Clock.h>
#include <ti/sysbios/knl/Task.h>

#include <ti/drivers/GPIO.h>
#include <ti/drivers/SDSPI.h>

/* Example/Board Header files */
#include "Board.h"
#include "SystemTime.h"
#include "main.h"

/*
 *  ======== main ========
 */
int main(void) {


    /* Call board init functions */
    Board_initGeneral();
    Board_initGPIO();
    Board_initSDSPI();
    Board_initSPI();
    Board_initUART();



    //SDCard_createTask();

    BLEWrite_createTask();

    Sensor1_createTask();

    Sensor2_createTask();

    Sensor3_createTask();

    Sensor4_createTask();

    /* Start BIOS */
    BIOS_start();

    return (0);
}
