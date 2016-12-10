#include <stdint.h>
#include <driverlib/systick.h>
#include "SystemTime.h"

static long currentMilliCount = 0;

static void SysTickInt();


void initSysTick() {
	SysTickPeriodSet(80000); // 1ms Systick
	SysTickEnable();
	SysTickIntRegister(SysTickInt);
	SysTickIntEnable();
}


long currentMilli() {
	return currentMilliCount;
}

static void SysTickInt() {
	currentMilliCount++;
}
