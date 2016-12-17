// Josh Hansen
// CEEN 4360 - Fall 2016
// Phase II

#define xdc_runtime_Log_DISABLE_ALL 1  // Add to disable logs from this file
#include <SMMain.h>
#include <xdc/runtime/Error.h>

#include <ti/sysbios/family/arm/cc26xx/Power.h>
#include <ti/sysbios/BIOS.h>

#include "ICall.h"
#include "bcomdef.h"
#include "peripheral.h"
#include <ti/drivers/UART.h>
#include <uart_logs.h>

#ifndef USE_DEFAULT_USER_CFG
#include "bleUserConfig.h"
// BLE user defined configuration
bleUserCfg_t user0Cfg = BLE_USER_CFG;
#endif // USE_DEFAULT_USER_CFG

/**
 * Exception handler
 */
void exceptionHandler() {
  volatile uint8_t i = 1;
  while(i){}
}

int main() {
  PIN_init(BoardGpioInitTable);

#ifndef POWER_SAVING
    /* Set constraints for Standby, powerdown and idle mode */
    Power_setConstraint(Power_SB_DISALLOW);
    Power_setConstraint(Power_IDLE_PD_DISALLOW);
#endif // POWER_SAVING


    /* Initialize ICall module */
    ICall_init();

    /* Start tasks of external images - Priority 5 */
    ICall_createRemoteTasks();

    /* Kick off profile - Priority 3 */
    GAPRole_createTask();

    /* User task - Priority 1 */
    STEMMeterBLE_createTask();

    BatMonitor_createTask();

    SPICommands_createTask();

    /* enable interrupts and start SYS/BIOS */
    BIOS_start();

    return 0;
}

/**
 * Error handled to be hooked into TI-RTOS
 */
Void smallErrorHook(Error_Block *eb) {
  for (;;);
}

/**
 * HAL assert handler required by OSAL memory module.
 */
void halAssertHandler(void) {
  for (;;);
}
