/**********************************************************************************************
 * Filename:       STEMMeter_Service.c
 *
 * Description:    This file contains the implementation of the service.
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


/*********************************************************************
 * INCLUDES
 */
#include <string.h>

#include "bcomdef.h"
#include "OSAL.h"
#include "linkdb.h"
#include "att.h"
#include "gatt.h"
#include "gatt_uuid.h"
#include "gattservapp.h"
#include "gapbondmgr.h"

#include "STEMMeter_Service.h"

/*********************************************************************
 * MACROS
 */

/*********************************************************************
 * CONSTANTS
 */

/*********************************************************************
 * TYPEDEFS
 */

/*********************************************************************
* GLOBAL VARIABLES
*/

// STEMMeter_Service Service UUID
CONST uint8_t STEMMeter_ServiceUUID[ATT_BT_UUID_SIZE] =
{
  LO_UINT16(STEMMETER_SERVICE_SERV_UUID), HI_UINT16(STEMMETER_SERVICE_SERV_UUID)
};

// sensor1Data UUID
CONST uint8_t STEMMeter_Service_Sensor1DataUUID[ATT_UUID_SIZE] =
{
  TI_BASE_UUID_128(STEMMETER_SERVICE_SENSOR1DATA_UUID)
};
// sensor2Data UUID
CONST uint8_t STEMMeter_Service_Sensor2DataUUID[ATT_UUID_SIZE] =
{
  TI_BASE_UUID_128(STEMMETER_SERVICE_SENSOR2DATA_UUID)
};
// sensor3Data UUID
CONST uint8_t STEMMeter_Service_Sensor3DataUUID[ATT_UUID_SIZE] =
{
  TI_BASE_UUID_128(STEMMETER_SERVICE_SENSOR3DATA_UUID)
};
// sensor4Data UUID
CONST uint8_t STEMMeter_Service_Sensor4DataUUID[ATT_UUID_SIZE] =
{
  TI_BASE_UUID_128(STEMMETER_SERVICE_SENSOR4DATA_UUID)
};
// sensor1Config UUID
CONST uint8_t STEMMeter_Service_Sensor1ConfigUUID[ATT_UUID_SIZE] =
{
  TI_BASE_UUID_128(STEMMETER_SERVICE_SENSOR1CONFIG_UUID)
};
// sensor2Config UUID
CONST uint8_t STEMMeter_Service_Sensor2ConfigUUID[ATT_UUID_SIZE] =
{
  TI_BASE_UUID_128(STEMMETER_SERVICE_SENSOR2CONFIG_UUID)
};
// sensor3Config UUID
CONST uint8_t STEMMeter_Service_Sensor3ConfigUUID[ATT_UUID_SIZE] =
{
  TI_BASE_UUID_128(STEMMETER_SERVICE_SENSOR3CONFIG_UUID)
};
// sensor4Config UUID
CONST uint8_t STEMMeter_Service_Sensor4ConfigUUID[ATT_UUID_SIZE] =
{
  TI_BASE_UUID_128(STEMMETER_SERVICE_SENSOR4CONFIG_UUID)
};
// batteryData UUID
CONST uint8_t STEMMeter_Service_BatteryDataUUID[ATT_UUID_SIZE] =
{
  TI_BASE_UUID_128(STEMMETER_SERVICE_BATTERYDATA_UUID)
};
// generalConfig UUID
CONST uint8_t STEMMeter_Service_GeneralConfigUUID[ATT_UUID_SIZE] =
{
  TI_BASE_UUID_128(STEMMETER_SERVICE_GENERALCONFIG_UUID)
};

/*********************************************************************
 * LOCAL VARIABLES
 */

static STEMMeter_ServiceCBs_t *pAppCBs = NULL;

/*********************************************************************
* Profile Attributes - variables
*/

// Service declaration
static CONST gattAttrType_t STEMMeter_ServiceDecl = { ATT_BT_UUID_SIZE, STEMMeter_ServiceUUID };

// Characteristic "Sensor1Data" Properties (for declaration)
static uint8_t STEMMeter_Service_Sensor1DataProps = GATT_PROP_READ | GATT_PROP_NOTIFY;

// Characteristic "Sensor1Data" Value variable
static uint8_t STEMMeter_Service_Sensor1DataVal[STEMMETER_SERVICE_SENSOR1DATA_LEN] = {0};

// Characteristic "Sensor1Data" CCCD
static gattCharCfg_t *STEMMeter_Service_Sensor1DataConfig;
// Characteristic "Sensor2Data" Properties (for declaration)
static uint8_t STEMMeter_Service_Sensor2DataProps = GATT_PROP_READ | GATT_PROP_NOTIFY;

// Characteristic "Sensor2Data" Value variable
static uint8_t STEMMeter_Service_Sensor2DataVal[STEMMETER_SERVICE_SENSOR2DATA_LEN] = {0};

// Characteristic "Sensor2Data" CCCD
static gattCharCfg_t *STEMMeter_Service_Sensor2DataConfig;
// Characteristic "Sensor3Data" Properties (for declaration)
static uint8_t STEMMeter_Service_Sensor3DataProps = GATT_PROP_READ | GATT_PROP_NOTIFY;

// Characteristic "Sensor3Data" Value variable
static uint8_t STEMMeter_Service_Sensor3DataVal[STEMMETER_SERVICE_SENSOR3DATA_LEN] = {0};

// Characteristic "Sensor3Data" CCCD
static gattCharCfg_t *STEMMeter_Service_Sensor3DataConfig;
// Characteristic "Sensor4Data" Properties (for declaration)
static uint8_t STEMMeter_Service_Sensor4DataProps = GATT_PROP_READ | GATT_PROP_NOTIFY;

// Characteristic "Sensor4Data" Value variable
static uint8_t STEMMeter_Service_Sensor4DataVal[STEMMETER_SERVICE_SENSOR4DATA_LEN] = {0};

// Characteristic "Sensor4Data" CCCD
static gattCharCfg_t *STEMMeter_Service_Sensor4DataConfig;
// Characteristic "Sensor1Config" Properties (for declaration)
static uint8_t STEMMeter_Service_Sensor1ConfigProps = GATT_PROP_WRITE | GATT_PROP_NOTIFY;

// Characteristic "Sensor1Config" Value variable
static uint8_t STEMMeter_Service_Sensor1ConfigVal[STEMMETER_SERVICE_SENSOR1CONFIG_LEN] = {0};

// Characteristic "Sensor1Config" CCCD
static gattCharCfg_t *STEMMeter_Service_Sensor1ConfigConfig;
// Characteristic "Sensor2Config" Properties (for declaration)
static uint8_t STEMMeter_Service_Sensor2ConfigProps = GATT_PROP_WRITE | GATT_PROP_NOTIFY;

// Characteristic "Sensor2Config" Value variable
static uint8_t STEMMeter_Service_Sensor2ConfigVal[STEMMETER_SERVICE_SENSOR2CONFIG_LEN] = {0};

// Characteristic "Sensor2Config" CCCD
static gattCharCfg_t *STEMMeter_Service_Sensor2ConfigConfig;
// Characteristic "Sensor3Config" Properties (for declaration)
static uint8_t STEMMeter_Service_Sensor3ConfigProps = GATT_PROP_WRITE | GATT_PROP_NOTIFY;

// Characteristic "Sensor3Config" Value variable
static uint8_t STEMMeter_Service_Sensor3ConfigVal[STEMMETER_SERVICE_SENSOR3CONFIG_LEN] = {0};

// Characteristic "Sensor3Config" CCCD
static gattCharCfg_t *STEMMeter_Service_Sensor3ConfigConfig;
// Characteristic "Sensor4Config" Properties (for declaration)
static uint8_t STEMMeter_Service_Sensor4ConfigProps = GATT_PROP_WRITE | GATT_PROP_NOTIFY;

// Characteristic "Sensor4Config" Value variable
static uint8_t STEMMeter_Service_Sensor4ConfigVal[STEMMETER_SERVICE_SENSOR4CONFIG_LEN] = {0};

// Characteristic "Sensor4Config" CCCD
static gattCharCfg_t *STEMMeter_Service_Sensor4ConfigConfig;
// Characteristic "BatteryData" Properties (for declaration)
static uint8_t STEMMeter_Service_BatteryDataProps = GATT_PROP_READ | GATT_PROP_NOTIFY;

// Characteristic "BatteryData" Value variable
static uint8_t STEMMeter_Service_BatteryDataVal[STEMMETER_SERVICE_BATTERYDATA_LEN] = {0};

// Characteristic "BatteryData" CCCD
static gattCharCfg_t *STEMMeter_Service_BatteryDataConfig;
// Characteristic "GeneralConfig" Properties (for declaration)
static uint8_t STEMMeter_Service_GeneralConfigProps = GATT_PROP_WRITE | GATT_PROP_NOTIFY;

// Characteristic "GeneralConfig" Value variable
static uint8_t STEMMeter_Service_GeneralConfigVal[STEMMETER_SERVICE_GENERALCONFIG_LEN] = {0};

// Characteristic "GeneralConfig" CCCD
static gattCharCfg_t *STEMMeter_Service_GeneralConfigConfig;

/*********************************************************************
* Profile Attributes - Table
*/

static gattAttribute_t STEMMeter_ServiceAttrTbl[] =
{
  // STEMMeter_Service Service Declaration
  {
    { ATT_BT_UUID_SIZE, primaryServiceUUID },
    GATT_PERMIT_READ,
    0,
    (uint8_t *)&STEMMeter_ServiceDecl
  },
    // Sensor1Data Characteristic Declaration
    {
      { ATT_BT_UUID_SIZE, characterUUID },
      GATT_PERMIT_READ,
      0,
      &STEMMeter_Service_Sensor1DataProps
    },
      // Sensor1Data Characteristic Value
      {
        { ATT_UUID_SIZE, STEMMeter_Service_Sensor1DataUUID },
        GATT_PERMIT_READ | GATT_PERMIT_WRITE,
        0,
        STEMMeter_Service_Sensor1DataVal
      },
      // Sensor1Data CCCD
      {
        { ATT_BT_UUID_SIZE, clientCharCfgUUID },
        GATT_PERMIT_READ | GATT_PERMIT_WRITE,
        0,
        (uint8 *)&STEMMeter_Service_Sensor1DataConfig
      },
    // Sensor2Data Characteristic Declaration
    {
      { ATT_BT_UUID_SIZE, characterUUID },
      GATT_PERMIT_READ,
      0,
      &STEMMeter_Service_Sensor2DataProps
    },
      // Sensor2Data Characteristic Value
      {
        { ATT_UUID_SIZE, STEMMeter_Service_Sensor2DataUUID },
        GATT_PERMIT_READ | GATT_PERMIT_WRITE,
        0,
        STEMMeter_Service_Sensor2DataVal
      },
      // Sensor2Data CCCD
      {
        { ATT_BT_UUID_SIZE, clientCharCfgUUID },
        GATT_PERMIT_READ | GATT_PERMIT_WRITE,
        0,
        (uint8 *)&STEMMeter_Service_Sensor2DataConfig
      },
    // Sensor3Data Characteristic Declaration
    {
      { ATT_BT_UUID_SIZE, characterUUID },
      GATT_PERMIT_READ,
      0,
      &STEMMeter_Service_Sensor3DataProps
    },
      // Sensor3Data Characteristic Value
      {
        { ATT_UUID_SIZE, STEMMeter_Service_Sensor3DataUUID },
        GATT_PERMIT_READ | GATT_PERMIT_WRITE,
        0,
        STEMMeter_Service_Sensor3DataVal
      },
      // Sensor3Data CCCD
      {
        { ATT_BT_UUID_SIZE, clientCharCfgUUID },
        GATT_PERMIT_READ | GATT_PERMIT_WRITE,
        0,
        (uint8 *)&STEMMeter_Service_Sensor3DataConfig
      },
    // Sensor4Data Characteristic Declaration
    {
      { ATT_BT_UUID_SIZE, characterUUID },
      GATT_PERMIT_READ,
      0,
      &STEMMeter_Service_Sensor4DataProps
    },
      // Sensor4Data Characteristic Value
      {
        { ATT_UUID_SIZE, STEMMeter_Service_Sensor4DataUUID },
        GATT_PERMIT_READ | GATT_PERMIT_WRITE,
        0,
        STEMMeter_Service_Sensor4DataVal
      },
      // Sensor4Data CCCD
      {
        { ATT_BT_UUID_SIZE, clientCharCfgUUID },
        GATT_PERMIT_READ | GATT_PERMIT_WRITE,
        0,
        (uint8 *)&STEMMeter_Service_Sensor4DataConfig
      },
    // Sensor1Config Characteristic Declaration
    {
      { ATT_BT_UUID_SIZE, characterUUID },
      GATT_PERMIT_READ,
      0,
      &STEMMeter_Service_Sensor1ConfigProps
    },
      // Sensor1Config Characteristic Value
      {
        { ATT_UUID_SIZE, STEMMeter_Service_Sensor1ConfigUUID },
        GATT_PERMIT_READ | GATT_PERMIT_WRITE,
        0,
        STEMMeter_Service_Sensor1ConfigVal
      },
      // Sensor1Config CCCD
      {
        { ATT_BT_UUID_SIZE, clientCharCfgUUID },
        GATT_PERMIT_READ | GATT_PERMIT_WRITE,
        0,
        (uint8 *)&STEMMeter_Service_Sensor1ConfigConfig
      },
    // Sensor2Config Characteristic Declaration
    {
      { ATT_BT_UUID_SIZE, characterUUID },
      GATT_PERMIT_READ,
      0,
      &STEMMeter_Service_Sensor2ConfigProps
    },
      // Sensor2Config Characteristic Value
      {
        { ATT_UUID_SIZE, STEMMeter_Service_Sensor2ConfigUUID },
        GATT_PERMIT_READ | GATT_PERMIT_WRITE,
        0,
        STEMMeter_Service_Sensor2ConfigVal
      },
      // Sensor2Config CCCD
      {
        { ATT_BT_UUID_SIZE, clientCharCfgUUID },
        GATT_PERMIT_READ | GATT_PERMIT_WRITE,
        0,
        (uint8 *)&STEMMeter_Service_Sensor2ConfigConfig
      },
    // Sensor3Config Characteristic Declaration
    {
      { ATT_BT_UUID_SIZE, characterUUID },
      GATT_PERMIT_READ,
      0,
      &STEMMeter_Service_Sensor3ConfigProps
    },
      // Sensor3Config Characteristic Value
      {
        { ATT_UUID_SIZE, STEMMeter_Service_Sensor3ConfigUUID },
        GATT_PERMIT_READ | GATT_PERMIT_WRITE,
        0,
        STEMMeter_Service_Sensor3ConfigVal
      },
      // Sensor3Config CCCD
      {
        { ATT_BT_UUID_SIZE, clientCharCfgUUID },
        GATT_PERMIT_READ | GATT_PERMIT_WRITE,
        0,
        (uint8 *)&STEMMeter_Service_Sensor3ConfigConfig
      },
    // Sensor4Config Characteristic Declaration
    {
      { ATT_BT_UUID_SIZE, characterUUID },
      GATT_PERMIT_READ,
      0,
      &STEMMeter_Service_Sensor4ConfigProps
    },
      // Sensor4Config Characteristic Value
      {
        { ATT_UUID_SIZE, STEMMeter_Service_Sensor4ConfigUUID },
        GATT_PERMIT_READ | GATT_PERMIT_WRITE,
        0,
        STEMMeter_Service_Sensor4ConfigVal
      },
      // Sensor4Config CCCD
      {
        { ATT_BT_UUID_SIZE, clientCharCfgUUID },
        GATT_PERMIT_READ | GATT_PERMIT_WRITE,
        0,
        (uint8 *)&STEMMeter_Service_Sensor4ConfigConfig
      },
    // BatteryData Characteristic Declaration
    {
      { ATT_BT_UUID_SIZE, characterUUID },
      GATT_PERMIT_READ,
      0,
      &STEMMeter_Service_BatteryDataProps
    },
      // BatteryData Characteristic Value
      {
        { ATT_UUID_SIZE, STEMMeter_Service_BatteryDataUUID },
        GATT_PERMIT_READ | GATT_PERMIT_WRITE,
        0,
        STEMMeter_Service_BatteryDataVal
      },
      // BatteryData CCCD
      {
        { ATT_BT_UUID_SIZE, clientCharCfgUUID },
        GATT_PERMIT_READ | GATT_PERMIT_WRITE,
        0,
        (uint8 *)&STEMMeter_Service_BatteryDataConfig
      },
    // GeneralConfig Characteristic Declaration
    {
      { ATT_BT_UUID_SIZE, characterUUID },
      GATT_PERMIT_READ,
      0,
      &STEMMeter_Service_GeneralConfigProps
    },
      // GeneralConfig Characteristic Value
      {
        { ATT_UUID_SIZE, STEMMeter_Service_GeneralConfigUUID },
        GATT_PERMIT_READ | GATT_PERMIT_WRITE,
        0,
        STEMMeter_Service_GeneralConfigVal
      },
      // GeneralConfig CCCD
      {
        { ATT_BT_UUID_SIZE, clientCharCfgUUID },
        GATT_PERMIT_READ | GATT_PERMIT_WRITE,
        0,
        (uint8 *)&STEMMeter_Service_GeneralConfigConfig
      },
};

/*********************************************************************
 * LOCAL FUNCTIONS
 */
static bStatus_t STEMMeter_Service_ReadAttrCB( uint16 connHandle, gattAttribute_t *pAttr,
                                           uint8 *pValue, uint16 *pLen, uint16 offset,
                                           uint16 maxLen, uint8 method );
static bStatus_t STEMMeter_Service_WriteAttrCB( uint16 connHandle, gattAttribute_t *pAttr,
                                            uint8 *pValue, uint16 len, uint16 offset,
                                            uint8 method );

/*********************************************************************
 * PROFILE CALLBACKS
 */
// Simple Profile Service Callbacks
CONST gattServiceCBs_t STEMMeter_ServiceCBs =
{
  STEMMeter_Service_ReadAttrCB,  // Read callback function pointer
  STEMMeter_Service_WriteAttrCB, // Write callback function pointer
  NULL                       // Authorization callback function pointer
};

/*********************************************************************
* PUBLIC FUNCTIONS
*/

/*
 * STEMMeter_Service_AddService- Initializes the STEMMeter_Service service by registering
 *          GATT attributes with the GATT server.
 *
 */
bStatus_t STEMMeter_Service_AddService( void )
{
  uint8_t status;

  // Allocate Client Characteristic Configuration table
  STEMMeter_Service_Sensor1DataConfig = (gattCharCfg_t *)ICall_malloc( sizeof(gattCharCfg_t) * linkDBNumConns );
  if ( STEMMeter_Service_Sensor1DataConfig == NULL )
  {
    return ( bleMemAllocError );
  }

  // Initialize Client Characteristic Configuration attributes
  GATTServApp_InitCharCfg( INVALID_CONNHANDLE, STEMMeter_Service_Sensor1DataConfig );
  // Allocate Client Characteristic Configuration table
  STEMMeter_Service_Sensor2DataConfig = (gattCharCfg_t *)ICall_malloc( sizeof(gattCharCfg_t) * linkDBNumConns );
  if ( STEMMeter_Service_Sensor2DataConfig == NULL )
  {
    return ( bleMemAllocError );
  }

  // Initialize Client Characteristic Configuration attributes
  GATTServApp_InitCharCfg( INVALID_CONNHANDLE, STEMMeter_Service_Sensor2DataConfig );
  // Allocate Client Characteristic Configuration table
  STEMMeter_Service_Sensor3DataConfig = (gattCharCfg_t *)ICall_malloc( sizeof(gattCharCfg_t) * linkDBNumConns );
  if ( STEMMeter_Service_Sensor3DataConfig == NULL )
  {
    return ( bleMemAllocError );
  }

  // Initialize Client Characteristic Configuration attributes
  GATTServApp_InitCharCfg( INVALID_CONNHANDLE, STEMMeter_Service_Sensor3DataConfig );
  // Allocate Client Characteristic Configuration table
  STEMMeter_Service_Sensor4DataConfig = (gattCharCfg_t *)ICall_malloc( sizeof(gattCharCfg_t) * linkDBNumConns );
  if ( STEMMeter_Service_Sensor4DataConfig == NULL )
  {
    return ( bleMemAllocError );
  }

  // Initialize Client Characteristic Configuration attributes
  GATTServApp_InitCharCfg( INVALID_CONNHANDLE, STEMMeter_Service_Sensor4DataConfig );
  // Allocate Client Characteristic Configuration table
  STEMMeter_Service_Sensor1ConfigConfig = (gattCharCfg_t *)ICall_malloc( sizeof(gattCharCfg_t) * linkDBNumConns );
  if ( STEMMeter_Service_Sensor1ConfigConfig == NULL )
  {
    return ( bleMemAllocError );
  }

  // Initialize Client Characteristic Configuration attributes
  GATTServApp_InitCharCfg( INVALID_CONNHANDLE, STEMMeter_Service_Sensor1ConfigConfig );
  // Allocate Client Characteristic Configuration table
  STEMMeter_Service_Sensor2ConfigConfig = (gattCharCfg_t *)ICall_malloc( sizeof(gattCharCfg_t) * linkDBNumConns );
  if ( STEMMeter_Service_Sensor2ConfigConfig == NULL )
  {
    return ( bleMemAllocError );
  }

  // Initialize Client Characteristic Configuration attributes
  GATTServApp_InitCharCfg( INVALID_CONNHANDLE, STEMMeter_Service_Sensor2ConfigConfig );
  // Allocate Client Characteristic Configuration table
  STEMMeter_Service_Sensor3ConfigConfig = (gattCharCfg_t *)ICall_malloc( sizeof(gattCharCfg_t) * linkDBNumConns );
  if ( STEMMeter_Service_Sensor3ConfigConfig == NULL )
  {
    return ( bleMemAllocError );
  }

  // Initialize Client Characteristic Configuration attributes
  GATTServApp_InitCharCfg( INVALID_CONNHANDLE, STEMMeter_Service_Sensor3ConfigConfig );
  // Allocate Client Characteristic Configuration table
  STEMMeter_Service_Sensor4ConfigConfig = (gattCharCfg_t *)ICall_malloc( sizeof(gattCharCfg_t) * linkDBNumConns );
  if ( STEMMeter_Service_Sensor4ConfigConfig == NULL )
  {
    return ( bleMemAllocError );
  }

  // Initialize Client Characteristic Configuration attributes
  GATTServApp_InitCharCfg( INVALID_CONNHANDLE, STEMMeter_Service_Sensor4ConfigConfig );
  // Allocate Client Characteristic Configuration table
  STEMMeter_Service_BatteryDataConfig = (gattCharCfg_t *)ICall_malloc( sizeof(gattCharCfg_t) * linkDBNumConns );
  if ( STEMMeter_Service_BatteryDataConfig == NULL )
  {
    return ( bleMemAllocError );
  }

  // Initialize Client Characteristic Configuration attributes
  GATTServApp_InitCharCfg( INVALID_CONNHANDLE, STEMMeter_Service_BatteryDataConfig );
  // Allocate Client Characteristic Configuration table
  STEMMeter_Service_GeneralConfigConfig = (gattCharCfg_t *)ICall_malloc( sizeof(gattCharCfg_t) * linkDBNumConns );
  if ( STEMMeter_Service_GeneralConfigConfig == NULL )
  {
    return ( bleMemAllocError );
  }

  // Initialize Client Characteristic Configuration attributes
  GATTServApp_InitCharCfg( INVALID_CONNHANDLE, STEMMeter_Service_GeneralConfigConfig );
  // Register GATT attribute list and CBs with GATT Server App
  status = GATTServApp_RegisterService( STEMMeter_ServiceAttrTbl,
                                        GATT_NUM_ATTRS( STEMMeter_ServiceAttrTbl ),
                                        GATT_MAX_ENCRYPT_KEY_SIZE,
                                        &STEMMeter_ServiceCBs );

  return ( status );
}

/*
 * STEMMeter_Service_RegisterAppCBs - Registers the application callback function.
 *                    Only call this function once.
 *
 *    appCallbacks - pointer to application callbacks.
 */
bStatus_t STEMMeter_Service_RegisterAppCBs( STEMMeter_ServiceCBs_t *appCallbacks )
{
  if ( appCallbacks )
  {
    pAppCBs = appCallbacks;

    return ( SUCCESS );
  }
  else
  {
    return ( bleAlreadyInRequestedMode );
  }
}

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
bStatus_t STEMMeter_Service_SetParameter( uint8 param, uint8 len, void *value )
{
  bStatus_t ret = SUCCESS;
  switch ( param )
  {
    case STEMMETER_SERVICE_SENSOR1DATA:
      if ( len == STEMMETER_SERVICE_SENSOR1DATA_LEN )
      {
        memcpy(STEMMeter_Service_Sensor1DataVal, value, len);

        // Try to send notification.
        GATTServApp_ProcessCharCfg( STEMMeter_Service_Sensor1DataConfig, (uint8_t *)&STEMMeter_Service_Sensor1DataVal, FALSE,
                                    STEMMeter_ServiceAttrTbl, GATT_NUM_ATTRS( STEMMeter_ServiceAttrTbl ),
                                    INVALID_TASK_ID,  STEMMeter_Service_ReadAttrCB);
      }
      else
      {
        ret = bleInvalidRange;
      }
      break;

    case STEMMETER_SERVICE_SENSOR2DATA:
      if ( len == STEMMETER_SERVICE_SENSOR2DATA_LEN )
      {
        memcpy(STEMMeter_Service_Sensor2DataVal, value, len);

        // Try to send notification.
        GATTServApp_ProcessCharCfg( STEMMeter_Service_Sensor2DataConfig, (uint8_t *)&STEMMeter_Service_Sensor2DataVal, FALSE,
                                    STEMMeter_ServiceAttrTbl, GATT_NUM_ATTRS( STEMMeter_ServiceAttrTbl ),
                                    INVALID_TASK_ID,  STEMMeter_Service_ReadAttrCB);
      }
      else
      {
        ret = bleInvalidRange;
      }
      break;

    case STEMMETER_SERVICE_SENSOR3DATA:
      if ( len == STEMMETER_SERVICE_SENSOR3DATA_LEN )
      {
        memcpy(STEMMeter_Service_Sensor3DataVal, value, len);

        // Try to send notification.
        GATTServApp_ProcessCharCfg( STEMMeter_Service_Sensor3DataConfig, (uint8_t *)&STEMMeter_Service_Sensor3DataVal, FALSE,
                                    STEMMeter_ServiceAttrTbl, GATT_NUM_ATTRS( STEMMeter_ServiceAttrTbl ),
                                    INVALID_TASK_ID,  STEMMeter_Service_ReadAttrCB);
      }
      else
      {
        ret = bleInvalidRange;
      }
      break;

    case STEMMETER_SERVICE_SENSOR4DATA:
      if ( len == STEMMETER_SERVICE_SENSOR4DATA_LEN )
      {
        memcpy(STEMMeter_Service_Sensor4DataVal, value, len);

        // Try to send notification.
        GATTServApp_ProcessCharCfg( STEMMeter_Service_Sensor4DataConfig, (uint8_t *)&STEMMeter_Service_Sensor4DataVal, FALSE,
                                    STEMMeter_ServiceAttrTbl, GATT_NUM_ATTRS( STEMMeter_ServiceAttrTbl ),
                                    INVALID_TASK_ID,  STEMMeter_Service_ReadAttrCB);
      }
      else
      {
        ret = bleInvalidRange;
      }
      break;

    case STEMMETER_SERVICE_SENSOR1CONFIG:
      if ( len == STEMMETER_SERVICE_SENSOR1CONFIG_LEN )
      {
        memcpy(STEMMeter_Service_Sensor1ConfigVal, value, len);

        // Try to send notification.
        GATTServApp_ProcessCharCfg( STEMMeter_Service_Sensor1ConfigConfig, (uint8_t *)&STEMMeter_Service_Sensor1ConfigVal, FALSE,
                                    STEMMeter_ServiceAttrTbl, GATT_NUM_ATTRS( STEMMeter_ServiceAttrTbl ),
                                    INVALID_TASK_ID,  STEMMeter_Service_ReadAttrCB);
      }
      else
      {
        ret = bleInvalidRange;
      }
      break;

    case STEMMETER_SERVICE_SENSOR2CONFIG:
      if ( len == STEMMETER_SERVICE_SENSOR2CONFIG_LEN )
      {
        memcpy(STEMMeter_Service_Sensor2ConfigVal, value, len);

        // Try to send notification.
        GATTServApp_ProcessCharCfg( STEMMeter_Service_Sensor2ConfigConfig, (uint8_t *)&STEMMeter_Service_Sensor2ConfigVal, FALSE,
                                    STEMMeter_ServiceAttrTbl, GATT_NUM_ATTRS( STEMMeter_ServiceAttrTbl ),
                                    INVALID_TASK_ID,  STEMMeter_Service_ReadAttrCB);
      }
      else
      {
        ret = bleInvalidRange;
      }
      break;

    case STEMMETER_SERVICE_SENSOR3CONFIG:
      if ( len == STEMMETER_SERVICE_SENSOR3CONFIG_LEN )
      {
        memcpy(STEMMeter_Service_Sensor3ConfigVal, value, len);

        // Try to send notification.
        GATTServApp_ProcessCharCfg( STEMMeter_Service_Sensor3ConfigConfig, (uint8_t *)&STEMMeter_Service_Sensor3ConfigVal, FALSE,
                                    STEMMeter_ServiceAttrTbl, GATT_NUM_ATTRS( STEMMeter_ServiceAttrTbl ),
                                    INVALID_TASK_ID,  STEMMeter_Service_ReadAttrCB);
      }
      else
      {
        ret = bleInvalidRange;
      }
      break;

    case STEMMETER_SERVICE_SENSOR4CONFIG:
      if ( len == STEMMETER_SERVICE_SENSOR4CONFIG_LEN )
      {
        memcpy(STEMMeter_Service_Sensor4ConfigVal, value, len);

        // Try to send notification.
        GATTServApp_ProcessCharCfg( STEMMeter_Service_Sensor4ConfigConfig, (uint8_t *)&STEMMeter_Service_Sensor4ConfigVal, FALSE,
                                    STEMMeter_ServiceAttrTbl, GATT_NUM_ATTRS( STEMMeter_ServiceAttrTbl ),
                                    INVALID_TASK_ID,  STEMMeter_Service_ReadAttrCB);
      }
      else
      {
        ret = bleInvalidRange;
      }
      break;

    case STEMMETER_SERVICE_BATTERYDATA:
      if ( len == STEMMETER_SERVICE_BATTERYDATA_LEN )
      {
        memcpy(STEMMeter_Service_BatteryDataVal, value, len);

        // Try to send notification.
        GATTServApp_ProcessCharCfg( STEMMeter_Service_BatteryDataConfig, (uint8_t *)&STEMMeter_Service_BatteryDataVal, FALSE,
                                    STEMMeter_ServiceAttrTbl, GATT_NUM_ATTRS( STEMMeter_ServiceAttrTbl ),
                                    INVALID_TASK_ID,  STEMMeter_Service_ReadAttrCB);
      }
      else
      {
        ret = bleInvalidRange;
      }
      break;

    case STEMMETER_SERVICE_GENERALCONFIG:
      if ( len == STEMMETER_SERVICE_GENERALCONFIG_LEN )
      {
        memcpy(STEMMeter_Service_GeneralConfigVal, value, len);

        // Try to send notification.
        GATTServApp_ProcessCharCfg( STEMMeter_Service_GeneralConfigConfig, (uint8_t *)&STEMMeter_Service_GeneralConfigVal, FALSE,
                                    STEMMeter_ServiceAttrTbl, GATT_NUM_ATTRS( STEMMeter_ServiceAttrTbl ),
                                    INVALID_TASK_ID,  STEMMeter_Service_ReadAttrCB);
      }
      else
      {
        ret = bleInvalidRange;
      }
      break;

    default:
      ret = INVALIDPARAMETER;
      break;
  }
  return ret;
}


/*
 * STEMMeter_Service_GetParameter - Get a STEMMeter_Service parameter.
 *
 *    param - Profile parameter ID
 *    value - pointer to data to write.  This is dependent on
 *          the parameter ID and WILL be cast to the appropriate
 *          data type (example: data type of uint16 will be cast to
 *          uint16 pointer).
 *          THIS FUNCTION HAS BUG. memcpy args needed to be switched around
 */
bStatus_t STEMMeter_Service_GetParameter( uint8 param, void *value )
{
  bStatus_t ret = SUCCESS;
  switch ( param )
  {
    case STEMMETER_SERVICE_SENSOR1DATA:
      memcpy(value, STEMMeter_Service_Sensor1DataVal, STEMMETER_SERVICE_SENSOR1DATA_LEN);
      break;

    case STEMMETER_SERVICE_SENSOR2DATA:
      memcpy(value, STEMMeter_Service_Sensor2DataVal, STEMMETER_SERVICE_SENSOR2DATA_LEN);
      break;

    case STEMMETER_SERVICE_SENSOR3DATA:
      memcpy(value, STEMMeter_Service_Sensor3DataVal, STEMMETER_SERVICE_SENSOR3DATA_LEN);
      break;

    case STEMMETER_SERVICE_SENSOR4DATA:
      memcpy(value, STEMMeter_Service_Sensor4DataVal, STEMMETER_SERVICE_SENSOR4DATA_LEN);
      break;

    case STEMMETER_SERVICE_SENSOR1CONFIG:
      memcpy(value, STEMMeter_Service_Sensor1ConfigVal, STEMMETER_SERVICE_SENSOR1CONFIG_LEN);
      break;

    case STEMMETER_SERVICE_SENSOR2CONFIG:
      memcpy(value, STEMMeter_Service_Sensor2ConfigVal, STEMMETER_SERVICE_SENSOR2CONFIG_LEN);
      break;

    case STEMMETER_SERVICE_SENSOR3CONFIG:
      memcpy(value, STEMMeter_Service_Sensor3ConfigVal, STEMMETER_SERVICE_SENSOR3CONFIG_LEN);
      break;

    case STEMMETER_SERVICE_SENSOR4CONFIG:
      memcpy(value, STEMMeter_Service_Sensor4ConfigVal, STEMMETER_SERVICE_SENSOR4CONFIG_LEN);
      break;

    case STEMMETER_SERVICE_BATTERYDATA:
      memcpy(value, STEMMeter_Service_BatteryDataVal, STEMMETER_SERVICE_BATTERYDATA_LEN);
      break;

    case STEMMETER_SERVICE_GENERALCONFIG:
      memcpy(value, STEMMeter_Service_GeneralConfigVal, STEMMETER_SERVICE_GENERALCONFIG_LEN);
      break;

    default:
      ret = INVALIDPARAMETER;
      break;
  }
  return ret;
}


/*********************************************************************
 * @fn          STEMMeter_Service_ReadAttrCB
 *
 * @brief       Read an attribute.
 *
 * @param       connHandle - connection message was received on
 * @param       pAttr - pointer to attribute
 * @param       pValue - pointer to data to be read
 * @param       pLen - length of data to be read
 * @param       offset - offset of the first octet to be read
 * @param       maxLen - maximum length of data to be read
 * @param       method - type of read message
 *
 * @return      SUCCESS, blePending or Failure
 */
static bStatus_t STEMMeter_Service_ReadAttrCB( uint16 connHandle, gattAttribute_t *pAttr,
                                       uint8 *pValue, uint16 *pLen, uint16 offset,
                                       uint16 maxLen, uint8 method )
{
  bStatus_t status = SUCCESS;

  // See if request is regarding the Sensor1Data Characteristic Value
if ( ! memcmp(pAttr->type.uuid, STEMMeter_Service_Sensor1DataUUID, pAttr->type.len) )
  {
    if ( offset > STEMMETER_SERVICE_SENSOR1DATA_LEN )  // Prevent malicious ATT ReadBlob offsets.
    {
      status = ATT_ERR_INVALID_OFFSET;
    }
    else
    {
      *pLen = MIN(maxLen, STEMMETER_SERVICE_SENSOR1DATA_LEN - offset);  // Transmit as much as possible
      memcpy(pValue, pAttr->pValue + offset, *pLen);
    }
  }
  // See if request is regarding the Sensor2Data Characteristic Value
else if ( ! memcmp(pAttr->type.uuid, STEMMeter_Service_Sensor2DataUUID, pAttr->type.len) )
  {
    if ( offset > STEMMETER_SERVICE_SENSOR2DATA_LEN )  // Prevent malicious ATT ReadBlob offsets.
    {
      status = ATT_ERR_INVALID_OFFSET;
    }
    else
    {
      *pLen = MIN(maxLen, STEMMETER_SERVICE_SENSOR2DATA_LEN - offset);  // Transmit as much as possible
      memcpy(pValue, pAttr->pValue + offset, *pLen);
    }
  }
  // See if request is regarding the Sensor3Data Characteristic Value
else if ( ! memcmp(pAttr->type.uuid, STEMMeter_Service_Sensor3DataUUID, pAttr->type.len) )
  {
    if ( offset > STEMMETER_SERVICE_SENSOR3DATA_LEN )  // Prevent malicious ATT ReadBlob offsets.
    {
      status = ATT_ERR_INVALID_OFFSET;
    }
    else
    {
      *pLen = MIN(maxLen, STEMMETER_SERVICE_SENSOR3DATA_LEN - offset);  // Transmit as much as possible
      memcpy(pValue, pAttr->pValue + offset, *pLen);
    }
  }
  // See if request is regarding the Sensor4Data Characteristic Value
else if ( ! memcmp(pAttr->type.uuid, STEMMeter_Service_Sensor4DataUUID, pAttr->type.len) )
  {
    if ( offset > STEMMETER_SERVICE_SENSOR4DATA_LEN )  // Prevent malicious ATT ReadBlob offsets.
    {
      status = ATT_ERR_INVALID_OFFSET;
    }
    else
    {
      *pLen = MIN(maxLen, STEMMETER_SERVICE_SENSOR4DATA_LEN - offset);  // Transmit as much as possible
      memcpy(pValue, pAttr->pValue + offset, *pLen);
    }
  }
  // See if request is regarding the Sensor1Config Characteristic Value
else if ( ! memcmp(pAttr->type.uuid, STEMMeter_Service_Sensor1ConfigUUID, pAttr->type.len) )
  {
    if ( offset > STEMMETER_SERVICE_SENSOR1CONFIG_LEN )  // Prevent malicious ATT ReadBlob offsets.
    {
      status = ATT_ERR_INVALID_OFFSET;
    }
    else
    {
      *pLen = MIN(maxLen, STEMMETER_SERVICE_SENSOR1CONFIG_LEN - offset);  // Transmit as much as possible
      memcpy(pValue, pAttr->pValue + offset, *pLen);
    }
  }
  // See if request is regarding the Sensor2Config Characteristic Value
else if ( ! memcmp(pAttr->type.uuid, STEMMeter_Service_Sensor2ConfigUUID, pAttr->type.len) )
  {
    if ( offset > STEMMETER_SERVICE_SENSOR2CONFIG_LEN )  // Prevent malicious ATT ReadBlob offsets.
    {
      status = ATT_ERR_INVALID_OFFSET;
    }
    else
    {
      *pLen = MIN(maxLen, STEMMETER_SERVICE_SENSOR2CONFIG_LEN - offset);  // Transmit as much as possible
      memcpy(pValue, pAttr->pValue + offset, *pLen);
    }
  }
  // See if request is regarding the Sensor3Config Characteristic Value
else if ( ! memcmp(pAttr->type.uuid, STEMMeter_Service_Sensor3ConfigUUID, pAttr->type.len) )
  {
    if ( offset > STEMMETER_SERVICE_SENSOR3CONFIG_LEN )  // Prevent malicious ATT ReadBlob offsets.
    {
      status = ATT_ERR_INVALID_OFFSET;
    }
    else
    {
      *pLen = MIN(maxLen, STEMMETER_SERVICE_SENSOR3CONFIG_LEN - offset);  // Transmit as much as possible
      memcpy(pValue, pAttr->pValue + offset, *pLen);
    }
  }
  // See if request is regarding the Sensor4Config Characteristic Value
else if ( ! memcmp(pAttr->type.uuid, STEMMeter_Service_Sensor4ConfigUUID, pAttr->type.len) )
  {
    if ( offset > STEMMETER_SERVICE_SENSOR4CONFIG_LEN )  // Prevent malicious ATT ReadBlob offsets.
    {
      status = ATT_ERR_INVALID_OFFSET;
    }
    else
    {
      *pLen = MIN(maxLen, STEMMETER_SERVICE_SENSOR4CONFIG_LEN - offset);  // Transmit as much as possible
      memcpy(pValue, pAttr->pValue + offset, *pLen);
    }
  }
  // See if request is regarding the BatteryData Characteristic Value
else if ( ! memcmp(pAttr->type.uuid, STEMMeter_Service_BatteryDataUUID, pAttr->type.len) )
  {
    if ( offset > STEMMETER_SERVICE_BATTERYDATA_LEN )  // Prevent malicious ATT ReadBlob offsets.
    {
      status = ATT_ERR_INVALID_OFFSET;
    }
    else
    {
      *pLen = MIN(maxLen, STEMMETER_SERVICE_BATTERYDATA_LEN - offset);  // Transmit as much as possible
      memcpy(pValue, pAttr->pValue + offset, *pLen);
    }
  }
  // See if request is regarding the GeneralConfig Characteristic Value
else if ( ! memcmp(pAttr->type.uuid, STEMMeter_Service_GeneralConfigUUID, pAttr->type.len) )
  {
    if ( offset > STEMMETER_SERVICE_GENERALCONFIG_LEN )  // Prevent malicious ATT ReadBlob offsets.
    {
      status = ATT_ERR_INVALID_OFFSET;
    }
    else
    {
      *pLen = MIN(maxLen, STEMMETER_SERVICE_GENERALCONFIG_LEN - offset);  // Transmit as much as possible
      memcpy(pValue, pAttr->pValue + offset, *pLen);
    }
  }
  else
  {
    // If we get here, that means you've forgotten to add an if clause for a
    // characteristic value attribute in the attribute table that has READ permissions.
    *pLen = 0;
    status = ATT_ERR_ATTR_NOT_FOUND;
  }

  return status;
}


/*********************************************************************
 * @fn      STEMMeter_Service_WriteAttrCB
 *
 * @brief   Validate attribute data prior to a write operation
 *
 * @param   connHandle - connection message was received on
 * @param   pAttr - pointer to attribute
 * @param   pValue - pointer to data to be written
 * @param   len - length of data
 * @param   offset - offset of the first octet to be written
 * @param   method - type of write message
 *
 * @return  SUCCESS, blePending or Failure
 */
static bStatus_t STEMMeter_Service_WriteAttrCB( uint16 connHandle, gattAttribute_t *pAttr,
                                        uint8 *pValue, uint16 len, uint16 offset,
                                        uint8 method )
{
  bStatus_t status  = SUCCESS;
  uint8_t   paramID = 0xFF;

  // See if request is regarding a Client Characterisic Configuration
  if ( ! memcmp(pAttr->type.uuid, clientCharCfgUUID, pAttr->type.len) )
  {
    // Allow only notifications.
    status = GATTServApp_ProcessCCCWriteReq( connHandle, pAttr, pValue, len,
                                             offset, GATT_CLIENT_CFG_NOTIFY);
  }
  // See if request is regarding the Sensor1Data Characteristic Value
  else if ( ! memcmp(pAttr->type.uuid, STEMMeter_Service_Sensor1DataUUID, pAttr->type.len) )
  {
    if ( offset + len > STEMMETER_SERVICE_SENSOR1DATA_LEN )
    {
      status = ATT_ERR_INVALID_OFFSET;
    }
    else
    {
      // Copy pValue into the variable we point to from the attribute table.
      memcpy(pAttr->pValue + offset, pValue, len);

      // Only notify application if entire expected value is written
      if ( offset + len == STEMMETER_SERVICE_SENSOR1DATA_LEN)
        paramID = STEMMETER_SERVICE_SENSOR1DATA;
    }
  }
  // See if request is regarding the Sensor2Data Characteristic Value
  else if ( ! memcmp(pAttr->type.uuid, STEMMeter_Service_Sensor2DataUUID, pAttr->type.len) )
  {
    if ( offset + len > STEMMETER_SERVICE_SENSOR2DATA_LEN )
    {
      status = ATT_ERR_INVALID_OFFSET;
    }
    else
    {
      // Copy pValue into the variable we point to from the attribute table.
      memcpy(pAttr->pValue + offset, pValue, len);

      // Only notify application if entire expected value is written
      if ( offset + len == STEMMETER_SERVICE_SENSOR2DATA_LEN)
        paramID = STEMMETER_SERVICE_SENSOR2DATA;
    }
  }
  // See if request is regarding the Sensor3Data Characteristic Value
  else if ( ! memcmp(pAttr->type.uuid, STEMMeter_Service_Sensor3DataUUID, pAttr->type.len) )
  {
    if ( offset + len > STEMMETER_SERVICE_SENSOR3DATA_LEN )
    {
      status = ATT_ERR_INVALID_OFFSET;
    }
    else
    {
      // Copy pValue into the variable we point to from the attribute table.
      memcpy(pAttr->pValue + offset, pValue, len);

      // Only notify application if entire expected value is written
      if ( offset + len == STEMMETER_SERVICE_SENSOR3DATA_LEN)
        paramID = STEMMETER_SERVICE_SENSOR3DATA;
    }
  }
  // See if request is regarding the Sensor4Data Characteristic Value
  else if ( ! memcmp(pAttr->type.uuid, STEMMeter_Service_Sensor4DataUUID, pAttr->type.len) )
  {
    if ( offset + len > STEMMETER_SERVICE_SENSOR4DATA_LEN )
    {
      status = ATT_ERR_INVALID_OFFSET;
    }
    else
    {
      // Copy pValue into the variable we point to from the attribute table.
      memcpy(pAttr->pValue + offset, pValue, len);

      // Only notify application if entire expected value is written
      if ( offset + len == STEMMETER_SERVICE_SENSOR4DATA_LEN)
        paramID = STEMMETER_SERVICE_SENSOR4DATA;
    }
  }
  // See if request is regarding the Sensor1Config Characteristic Value
  else if ( ! memcmp(pAttr->type.uuid, STEMMeter_Service_Sensor1ConfigUUID, pAttr->type.len) )
  {
    if ( offset + len > STEMMETER_SERVICE_SENSOR1CONFIG_LEN )
    {
      status = ATT_ERR_INVALID_OFFSET;
    }
    else
    {
      // Copy pValue into the variable we point to from the attribute table.
      memcpy(pAttr->pValue + offset, pValue, len);

      // Only notify application if entire expected value is written
      if ( offset + len == STEMMETER_SERVICE_SENSOR1CONFIG_LEN)
        paramID = STEMMETER_SERVICE_SENSOR1CONFIG;
    }
  }
  // See if request is regarding the Sensor2Config Characteristic Value
  else if ( ! memcmp(pAttr->type.uuid, STEMMeter_Service_Sensor2ConfigUUID, pAttr->type.len) )
  {
    if ( offset + len > STEMMETER_SERVICE_SENSOR2CONFIG_LEN )
    {
      status = ATT_ERR_INVALID_OFFSET;
    }
    else
    {
      // Copy pValue into the variable we point to from the attribute table.
      memcpy(pAttr->pValue + offset, pValue, len);

      // Only notify application if entire expected value is written
      if ( offset + len == STEMMETER_SERVICE_SENSOR2CONFIG_LEN)
        paramID = STEMMETER_SERVICE_SENSOR2CONFIG;
    }
  }
  // See if request is regarding the Sensor3Config Characteristic Value
  else if ( ! memcmp(pAttr->type.uuid, STEMMeter_Service_Sensor3ConfigUUID, pAttr->type.len) )
  {
    if ( offset + len > STEMMETER_SERVICE_SENSOR3CONFIG_LEN )
    {
      status = ATT_ERR_INVALID_OFFSET;
    }
    else
    {
      // Copy pValue into the variable we point to from the attribute table.
      memcpy(pAttr->pValue + offset, pValue, len);

      // Only notify application if entire expected value is written
      if ( offset + len == STEMMETER_SERVICE_SENSOR3CONFIG_LEN)
        paramID = STEMMETER_SERVICE_SENSOR3CONFIG;
    }
  }
  // See if request is regarding the Sensor4Config Characteristic Value
  else if ( ! memcmp(pAttr->type.uuid, STEMMeter_Service_Sensor4ConfigUUID, pAttr->type.len) )
  {
    if ( offset + len > STEMMETER_SERVICE_SENSOR4CONFIG_LEN )
    {
      status = ATT_ERR_INVALID_OFFSET;
    }
    else
    {
      // Copy pValue into the variable we point to from the attribute table.
      memcpy(pAttr->pValue + offset, pValue, len);

      // Only notify application if entire expected value is written
      if ( offset + len == STEMMETER_SERVICE_SENSOR4CONFIG_LEN)
        paramID = STEMMETER_SERVICE_SENSOR4CONFIG;
    }
  }
  // See if request is regarding the BatteryData Characteristic Value
  else if ( ! memcmp(pAttr->type.uuid, STEMMeter_Service_BatteryDataUUID, pAttr->type.len) )
  {
    if ( offset + len > STEMMETER_SERVICE_BATTERYDATA_LEN )
    {
      status = ATT_ERR_INVALID_OFFSET;
    }
    else
    {
      // Copy pValue into the variable we point to from the attribute table.
      memcpy(pAttr->pValue + offset, pValue, len);

      // Only notify application if entire expected value is written
      if ( offset + len == STEMMETER_SERVICE_BATTERYDATA_LEN)
        paramID = STEMMETER_SERVICE_BATTERYDATA;
    }
  }
  // See if request is regarding the GeneralConfig Characteristic Value
  else if ( ! memcmp(pAttr->type.uuid, STEMMeter_Service_GeneralConfigUUID, pAttr->type.len) )
  {
    if ( offset + len > STEMMETER_SERVICE_GENERALCONFIG_LEN )
    {
      status = ATT_ERR_INVALID_OFFSET;
    }
    else
    {
      // Copy pValue into the variable we point to from the attribute table.
      memcpy(pAttr->pValue + offset, pValue, len);

      // Only notify application if entire expected value is written
      if ( offset + len == STEMMETER_SERVICE_GENERALCONFIG_LEN)
        paramID = STEMMETER_SERVICE_GENERALCONFIG;
    }
  }
  else
  {
    // If we get here, that means you've forgotten to add an if clause for a
    // characteristic value attribute in the attribute table that has WRITE permissions.
    status = ATT_ERR_ATTR_NOT_FOUND;
  }

  // Let the application know something changed (if it did) by using the
  // callback it registered earlier (if it did).
  if (paramID != 0xFF)
    if ( pAppCBs && pAppCBs->pfnChangeCb )
      pAppCBs->pfnChangeCb( paramID ); // Call app function from stack task context.

  return status;
}
