
#include <ti/sysbios/knl/Clock.h>
#include <ti/sysbios/knl/Semaphore.h>
#include <ti/sysbios/knl/Queue.h>
#include <stdint.h>
#include "Utils/util.h"
#include "UTC_clock.h"

/*********************************************************************
 * MACROS
 */

#define	YearLength(yr)	(IsLeapYear(yr) ? 366 : 365)

/*********************************************************************
 * CONSTANTS
 */

// Update every 1000ms
#define UTC_UPDATE_PERIOD  1000

#define IsLeapYear(yr)     (!((yr) % 400) || (((yr) % 100) && !((yr) % 4)))

#define	BEGYEAR	           2000     // UTC started at 00:00:00 January 1, 2000

#define	DAY                86400UL  // 24 hours * 60 minutes * 60 seconds

/*********************************************************************
 * TYPEDEFS
 */

/*********************************************************************
 * GLOBAL VARIABLES
 */

/*********************************************************************
 * EXTERNAL VARIABLES
 */

/*********************************************************************
 * EXTERNAL FUNCTIONS
 */

/*********************************************************************
 * LOCAL VARIABLES
 */

// Clock instance used to update UTC clock.
Clock_Struct UTC_clock;

static uint8_t currentDOW;

// Time is the number of seconds since 0 hrs, 0 minutes, 0 seconds, on the
// 1st of January 2000 UTC.
UTCTime UTC_timeSeconds = 0;

/*********************************************************************
 * LOCAL FUNCTION PROTOTYPES
 */
static uint8_t UTC_monthLength(uint8_t lpyr, uint8_t mon);

static void UTC_clockUpdate(uint32_t elapsedMSec);

static void UTC_timeUpdateHandler(UArg a0);

/*********************************************************************
 * FUNCTIONS
 *********************************************************************/


/*********************************************************************
 * @fn      UTC_init
 *
 * @brief   Initialize the UTC clock module.  Sets up and starts the
 *          clock instance.
 *
 * @param   None.
 *
 * @return  None.
 */
void UTC_init(void) {
  // Construct a periodic clock with a 1000ms duration and period to start
  // immediately.
  Util_constructClock(&UTC_clock, UTC_timeUpdateHandler, UTC_UPDATE_PERIOD,
                      UTC_UPDATE_PERIOD, true, 0);
}

/*********************************************************************
 * @fn      UTC_timeUpdateHandler
 *
 * @brief   Expiration callback for UTC clock instance.
 *          Each time this is called the internal counter is updated
 *
 *
 * @param   None.
 *
 * @return  None.
 */
void UTC_timeUpdateHandler(UArg a0)
{
  static uint32_t prevClockTicks = 0;
  static uint16_t remUsTicks = 0;
  uint32_t clockTicks, elapsedClockTicks;
  uint32_t elapsedMSec = 0;

  // Get the running count of clock ticks.
  clockTicks = Clock_getTicks();

  // Check that time has passed.
  if (clockTicks != prevClockTicks)
  {
    // To make sure time has passed and that a negative difference is not
    // calculated, check if the tick count is greater than the previous
    // measurement's.
    if (clockTicks > prevClockTicks)
    {
      // Get the elapsed clock ticks.
      elapsedClockTicks = clockTicks - prevClockTicks;
    }
    // Else tick count rolled over.
    else
    {
      // Get the elapsed clock ticks, accounting for the roll over.
      elapsedClockTicks = (0xFFFFFFFF - prevClockTicks) + clockTicks + 1;
    }

    // Convert to milliseconds.
    elapsedMSec = (elapsedClockTicks * Clock_tickPeriod) / 1000;

    // Find remainder.
    remUsTicks += (elapsedClockTicks * Clock_tickPeriod) % 1000;

    // If the running total of remaining microseconds is greater than or equal
    // to one millisecond.
    if (remUsTicks >= 1000)
    {
      // Add in the extra millisecond.
      // Note: the remainder has an open upper limit of 2 milliseconds.
      elapsedMSec += 1;

      // Adjust the remainder.
      remUsTicks %= 1000;
    }
  }

  // If time has passed
  if (elapsedMSec)
  {
    // Store the tick count for the next iteration through this function.
    prevClockTicks = clockTicks;

    // Update the UTC Clock.
    UTC_clockUpdate(elapsedMSec);
    //
  }
}

/*********************************************************************
 * @fn      UTC_clockUpdate
 *
 * @brief   Updates the UTC Clock time with elapsed milliseconds.
 *
 * @param   elapsedMSec - elapsed milliseconds
 *
 * @return  none
 */
static void UTC_clockUpdate(uint32_t elapsedMSec) {
  static uint32_t timeMSec = 0;


  // Add elapsed milliseconds to the saved millisecond portion of time.
  timeMSec += elapsedMSec;

  // Roll up milliseconds to the number of seconds.
  if (timeMSec >= 1000) {
    UTC_timeSeconds += timeMSec / 1000;
    timeMSec = timeMSec % 1000;
  }

}

/*********************************************************************
 * @fn      UTC_setClock
 *
 * @brief   Set a new time.  This will only set the seconds portion
 *          of time and doesn't change the factional second counter.
 *
 * @param   newTime - Number of seconds since 0 hrs, 0 minutes,
 *                    0 seconds, on the 1st of January 2000 UTC.
 *
 * @return  none
 */
void UTC_setClock(UTCTime newTime, const uint8_t dow) {
  UTC_timeSeconds = newTime;
  currentDOW = dow;
}

/*********************************************************************
 * @fn      UTC_getClock
 *
 * @brief   Gets the current time.  This will only return the seconds
 *          portion of time and doesn't include the factional second
 *          counter.
 *
 * @param   none
 *
 * @return  number of seconds since 0 hrs, 0 minutes, 0 seconds,
 *          on the 1st of January 2000 UTC
 */
UTCTime UTC_getClock(void)
{
  return (UTC_timeSeconds);
}

/*********************************************************************
 * @fn      UTC_convertUTCTime
 *
 * @brief   Converts UTCTime to UTCTimeStruct (from total seconds to exact
 *          date).
 *
 * @param   tm - pointer to breakdown struct.
 *
 * @param   secTime - number of seconds since 0 hrs, 0 minutes,
 *          0 seconds, on the 1st of January 2000 UTC.
 *
 * @return  none
 */
void UTC_convertUTCTime(UTCTimeStruct *tm, UTCTime secTime)
{
  // Calculate the time less than a day - hours, minutes, seconds.
  {
    // The number of seconds that have occured so far stoday.
    uint32_t day = secTime % DAY;

    // Seconds that have passed in the current minute.
    tm->seconds = day % 60UL;
    // Minutes that have passed in the current hour.
    // (seconds per day) / (seconds per minute) = (minutes on an hour boundary)
    tm->minutes = (day % 3600UL) / 60UL;
    // Hours that have passed in the current day.
    tm->hour = day / 3600UL;
  }

  // Fill in the calendar - day, month, year
  {
    uint16_t numDays = secTime / DAY;
    uint8_t monthLen;
    tm->year = BEGYEAR;

    while (numDays >= YearLength(tm->year))
    {
      numDays -= YearLength(tm->year);
      tm->year++;
    }

    // January.
    tm->month = 0;

    monthLen = UTC_monthLength(IsLeapYear(tm->year), tm->month);

    // Determine the number of months which have passed from remaining days.
    while (numDays >= monthLen)
    {
      // Subtract number of days in month from remaining count of days.
      numDays -= monthLen;
      tm->month++;

      // Recalculate month length.
      monthLen = UTC_monthLength(IsLeapYear(tm->year), tm->month);
    }

    // Store the remaining days.
    tm->day = numDays;
    tm->dow = currentDOW;
  }
}

/*********************************************************************
 * @fn      UTC_monthLength
 *
 * @param   lpyr - 1 for leap year, 0 if not
 *
 * @param   mon - 0 - 11 (jan - dec)
 *
 * @return  number of days in specified month
 */
static uint8_t UTC_monthLength(uint8_t lpyr, uint8_t mon)
{
  uint8_t days = 31;

  if (mon == 1) // feb
  {
    days = (28 + lpyr);
  }
  else
  {
    if (mon > 6) // aug-dec
    {
      mon--;
    }

    if (mon & 1)
    {
      days = 30;
    }
  }

  return (days);
}

/*********************************************************************
 * @fn      UTC_convertUTCSecs
 *
 * @brief   Converts a UTCTimeStruct to UTCTime (from exact date to total
 *          seconds).
 *
 * @param   tm - pointer to provided struct.
 *
 * @return  number of seconds since 00:00:00 on 01/01/2000 (UTC).
 */
UTCTime UTC_convertUTCSecs(UTCTimeStruct *tm)
{
  uint32_t seconds;

  // Seconds for the partial day.
  seconds = (((tm->hour * 60UL) + tm->minutes) * 60UL) + tm->seconds;

  // Account for previous complete days.
  {
    // Start with complete days in current month.
    uint16_t days = tm->day;

    // Next, complete months in current year.
    {
      int8_t month = tm->month;
      while (--month >= 0)
      {
        days += UTC_monthLength(IsLeapYear(tm->year), month);
      }
    }

    // Next, complete years before current year.
    {
      uint16_t year = tm->year;
      while (--year >= BEGYEAR)
      {
        days += YearLength(year);
      }
    }

    // Add total seconds before partial day.
    seconds += (days * DAY);
  }

  return (seconds);
}
