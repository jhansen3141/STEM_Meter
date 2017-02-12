/**********************************************************************************************
 * Filename:       STEMMeter_Service.h
 *
 * Description:    This file contains the STEMMeter_Service service definitions and
 *                 prototypes.
 *
 * Copyright (c) 2015-2016, Texas Instruments Incorporated
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * *  Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * *  Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * *  Neither the name of Texas Instruments Incorporated nor the names of
 *    its contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *************************************************************************************************/


#ifndef _STEMMETER_SERVICE_H_
#define _STEMMETER_SERVICE_H_

#ifdef __cplusplus
extern "C"
{
#endif

/*********************************************************************
 * INCLUDES
 */

/*********************************************************************
 * CONSTANTS
 */

/*********************************************************************
* CONSTANTS
*/
// Service UUID
#define STEMMETER_SERVICE_SERV_UUID 0xABAE

//  Characteristic defines
#define STEMMETER_SERVICE_SENSOR1DATA      0
#define STEMMETER_SERVICE_SENSOR1DATA_UUID 0xBEAA
#define STEMMETER_SERVICE_SENSOR1DATA_LEN  20

//  Characteristic defines
#define STEMMETER_SERVICE_SENSOR2DATA      1
#define STEMMETER_SERVICE_SENSOR2DATA_UUID 0xBEAB
#define STEMMETER_SERVICE_SENSOR2DATA_LEN  20

//  Characteristic defines
#define STEMMETER_SERVICE_SENSOR3DATA      2
#define STEMMETER_SERVICE_SENSOR3DATA_UUID 0xBEAC
#define STEMMETER_SERVICE_SENSOR3DATA_LEN  20

//  Characteristic defines
#define STEMMETER_SERVICE_SENSOR4DATA      3
#define STEMMETER_SERVICE_SENSOR4DATA_UUID 0xBEAD
#define STEMMETER_SERVICE_SENSOR4DATA_LEN  20

//  Characteristic defines
#define STEMMETER_SERVICE_CONFIG      4
#define STEMMETER_SERVICE_CONFIG_UUID 0xBEAF
#define STEMMETER_SERVICE_CONFIG_LEN  20

//  Characteristic defines
#define STEMMETER_SERVICE_BATTERYDATA      5
#define STEMMETER_SERVICE_BATTERYDATA_UUID 0xBEBC
#define STEMMETER_SERVICE_BATTERYDATA_LEN  20

//  Characteristic defines
#define STEMMETER_SERVICE_TIME      6
#define STEMMETER_SERVICE_TIME_UUID 0xBEBD
#define STEMMETER_SERVICE_TIME_LEN  20

/*********************************************************************
 * TYPEDEFS
 */

/*********************************************************************
 * MACROS
 */

/*********************************************************************
 * Profile Callbacks
 */

// Callback when a characteristic value has changed
typedef void (*STEMMeter_ServiceChange_t)( uint8 paramID );

typedef struct
{
  STEMMeter_ServiceChange_t        pfnChangeCb;  // Called when characteristic value changes
} STEMMeter_ServiceCBs_t;



/*********************************************************************
 * API FUNCTIONS
 */


/*
 * STEMMeter_Service_AddService- Initializes the STEMMeter_Service service by registering
 *          GATT attributes with the GATT server.
 *
 */
extern bStatus_t STEMMeter_Service_AddService( void );

/*
 * STEMMeter_Service_RegisterAppCBs - Registers the application callback function.
 *                    Only call this function once.
 *
 *    appCallbacks - pointer to application callbacks.
 */
extern bStatus_t STEMMeter_Service_RegisterAppCBs( STEMMeter_ServiceCBs_t *appCallbacks );

/*
 * STEMMeter_Service_SetParameter - Set a STEMMeter_Service parameter.
 *
 *    param - Profile parameter ID
 *    len - length of data to right
 *    value - pointer to data to write.  This is dependent on
 *          the parameter ID and WILL be cast to the appropriate
 *          data type (example: data type of uint16 will be cast to
 *          uint16 pointer).
 */
extern bStatus_t STEMMeter_Service_SetParameter( uint8 param, uint8 len, void *value );

/*
 * STEMMeter_Service_GetParameter - Get a STEMMeter_Service parameter.
 *
 *    param - Profile parameter ID
 *    value - pointer to data to write.  This is dependent on
 *          the parameter ID and WILL be cast to the appropriate
 *          data type (example: data type of uint16 will be cast to
 *          uint16 pointer).
 */
extern bStatus_t STEMMeter_Service_GetParameter( uint8 param, void *value );

/*********************************************************************
*********************************************************************/

#ifdef __cplusplus
}
#endif

#endif /* _STEMMETER_SERVICE_H_ */
