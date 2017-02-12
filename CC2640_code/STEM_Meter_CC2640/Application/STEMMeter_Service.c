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
CONST uint8_t STEMMeter_ServiceUUID[ATT_UUID_SIZE] =
{
  TI_BASE_UUID_128(STEMMETER_SERVICE_SERV_UUID)
};

// SENSOR1DATA UUID
CONST uint8_t STEMMeter_Service_SENSOR1DATAUUID[ATT_UUID_SIZE] =
{
  TI_BASE_UUID_128(STEMMETER_SERVICE_SENSOR1DATA_UUID)
};
// SENSOR2DATA UUID
CONST uint8_t STEMMeter_Service_SENSOR2DATAUUID[ATT_UUID_SIZE] =
{
  TI_BASE_UUID_128(STEMMETER_SERVICE_SENSOR2DATA_UUID)
};
// SENSOR3DATA UUID
CONST uint8_t STEMMeter_Service_SENSOR3DATAUUID[ATT_UUID_SIZE] =
{
  TI_BASE_UUID_128(STEMMETER_SERVICE_SENSOR3DATA_UUID)
};
// SENSOR4DATA UUID
CONST uint8_t STEMMeter_Service_SENSOR4DATAUUID[ATT_UUID_SIZE] =
{
  TI_BASE_UUID_128(STEMMETER_SERVICE_SENSOR4DATA_UUID)
};
// CONFIG UUID
CONST uint8_t STEMMeter_Service_CONFIGUUID[ATT_UUID_SIZE] =
{
  TI_BASE_UUID_128(STEMMETER_SERVICE_CONFIG_UUID)
};
// BATTERYDATA UUID
CONST uint8_t STEMMeter_Service_BATTERYDATAUUID[ATT_UUID_SIZE] =
{
  TI_BASE_UUID_128(STEMMETER_SERVICE_BATTERYDATA_UUID)
};
// TIME UUID
CONST uint8_t STEMMeter_Service_TIMEUUID[ATT_UUID_SIZE] =
{
  TI_BASE_UUID_128(STEMMETER_SERVICE_TIME_UUID)
};

/*********************************************************************
 * LOCAL VARIABLES
 */

static STEMMeter_ServiceCBs_t *pAppCBs = NULL;

/*********************************************************************
* Profile Attributes - variables
*/

// Service declaration
static CONST gattAttrType_t STEMMeter_ServiceDecl = { ATT_UUID_SIZE, STEMMeter_ServiceUUID };

// Characteristic "SENSOR1DATA" Properties (for declaration)
static uint8_t STEMMeter_Service_SENSOR1DATAProps = GATT_PROP_READ | GATT_PROP_NOTIFY;

// Characteristic "SENSOR1DATA" Value variable
static uint8_t STEMMeter_Service_SENSOR1DATAVal[STEMMETER_SERVICE_SENSOR1DATA_LEN] = {0};

// Characteristic "SENSOR1DATA" CCCD
static gattCharCfg_t *STEMMeter_Service_SENSOR1DATAConfig;
// Characteristic "SENSOR2DATA" Properties (for declaration)
static uint8_t STEMMeter_Service_SENSOR2DATAProps = GATT_PROP_READ | GATT_PROP_NOTIFY;

// Characteristic "SENSOR2DATA" Value variable
static uint8_t STEMMeter_Service_SENSOR2DATAVal[STEMMETER_SERVICE_SENSOR2DATA_LEN] = {0};

// Characteristic "SENSOR2DATA" CCCD
static gattCharCfg_t *STEMMeter_Service_SENSOR2DATAConfig;
// Characteristic "SENSOR3DATA" Properties (for declaration)
static uint8_t STEMMeter_Service_SENSOR3DATAProps = GATT_PROP_READ | GATT_PROP_NOTIFY;

// Characteristic "SENSOR3DATA" Value variable
static uint8_t STEMMeter_Service_SENSOR3DATAVal[STEMMETER_SERVICE_SENSOR3DATA_LEN] = {0};

// Characteristic "SENSOR3DATA" CCCD
static gattCharCfg_t *STEMMeter_Service_SENSOR3DATAConfig;
// Characteristic "SENSOR4DATA" Properties (for declaration)
static uint8_t STEMMeter_Service_SENSOR4DATAProps = GATT_PROP_READ | GATT_PROP_NOTIFY;

// Characteristic "SENSOR4DATA" Value variable
static uint8_t STEMMeter_Service_SENSOR4DATAVal[STEMMETER_SERVICE_SENSOR4DATA_LEN] = {0};

// Characteristic "SENSOR4DATA" CCCD
static gattCharCfg_t *STEMMeter_Service_SENSOR4DATAConfig;
// Characteristic "CONFIG" Properties (for declaration)
static uint8_t STEMMeter_Service_CONFIGProps = GATT_PROP_READ | GATT_PROP_WRITE | GATT_PROP_NOTIFY;

// Characteristic "CONFIG" Value variable
static uint8_t STEMMeter_Service_CONFIGVal[STEMMETER_SERVICE_CONFIG_LEN] = {0};

// Characteristic "CONFIG" CCCD
static gattCharCfg_t *STEMMeter_Service_CONFIGConfig;
// Characteristic "BATTERYDATA" Properties (for declaration)
static uint8_t STEMMeter_Service_BATTERYDATAProps = GATT_PROP_READ | GATT_PROP_NOTIFY;

// Characteristic "BATTERYDATA" Value variable
static uint8_t STEMMeter_Service_BATTERYDATAVal[STEMMETER_SERVICE_BATTERYDATA_LEN] = {0};

// Characteristic "BATTERYDATA" CCCD
static gattCharCfg_t *STEMMeter_Service_BATTERYDATAConfig;
// Characteristic "TIME" Properties (for declaration)
static uint8_t STEMMeter_Service_TIMEProps = GATT_PROP_READ | GATT_PROP_WRITE | GATT_PROP_NOTIFY;

// Characteristic "TIME" Value variable
static uint8_t STEMMeter_Service_TIMEVal[STEMMETER_SERVICE_TIME_LEN] = {0};

// Characteristic "TIME" CCCD
static gattCharCfg_t *STEMMeter_Service_TIMEConfig;

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
    // SENSOR1DATA Characteristic Declaration
    {
      { ATT_BT_UUID_SIZE, characterUUID },
      GATT_PERMIT_READ,
      0,
      &STEMMeter_Service_SENSOR1DATAProps
    },
      // SENSOR1DATA Characteristic Value
      {
        { ATT_UUID_SIZE, STEMMeter_Service_SENSOR1DATAUUID },
        GATT_PERMIT_READ,
        0,
        STEMMeter_Service_SENSOR1DATAVal
      },
      // SENSOR1DATA CCCD
      {
        { ATT_BT_UUID_SIZE, clientCharCfgUUID },
        GATT_PERMIT_READ | GATT_PERMIT_WRITE,
        0,
        (uint8 *)&STEMMeter_Service_SENSOR1DATAConfig
      },
    // SENSOR2DATA Characteristic Declaration
    {
      { ATT_BT_UUID_SIZE, characterUUID },
      GATT_PERMIT_READ,
      0,
      &STEMMeter_Service_SENSOR2DATAProps
    },
      // SENSOR2DATA Characteristic Value
      {
        { ATT_UUID_SIZE, STEMMeter_Service_SENSOR2DATAUUID },
        GATT_PERMIT_READ,
        0,
        STEMMeter_Service_SENSOR2DATAVal
      },
      // SENSOR2DATA CCCD
      {
        { ATT_BT_UUID_SIZE, clientCharCfgUUID },
        GATT_PERMIT_READ | GATT_PERMIT_WRITE,
        0,
        (uint8 *)&STEMMeter_Service_SENSOR2DATAConfig
      },
    // SENSOR3DATA Characteristic Declaration
    {
      { ATT_BT_UUID_SIZE, characterUUID },
      GATT_PERMIT_READ,
      0,
      &STEMMeter_Service_SENSOR3DATAProps
    },
      // SENSOR3DATA Characteristic Value
      {
        { ATT_UUID_SIZE, STEMMeter_Service_SENSOR3DATAUUID },
        GATT_PERMIT_READ,
        0,
        STEMMeter_Service_SENSOR3DATAVal
      },
      // SENSOR3DATA CCCD
      {
        { ATT_BT_UUID_SIZE, clientCharCfgUUID },
        GATT_PERMIT_READ | GATT_PERMIT_WRITE,
        0,
        (uint8 *)&STEMMeter_Service_SENSOR3DATAConfig
      },
    // SENSOR4DATA Characteristic Declaration
    {
      { ATT_BT_UUID_SIZE, characterUUID },
      GATT_PERMIT_READ,
      0,
      &STEMMeter_Service_SENSOR4DATAProps
    },
      // SENSOR4DATA Characteristic Value
      {
        { ATT_UUID_SIZE, STEMMeter_Service_SENSOR4DATAUUID },
        GATT_PERMIT_READ,
        0,
        STEMMeter_Service_SENSOR4DATAVal
      },
      // SENSOR4DATA CCCD
      {
        { ATT_BT_UUID_SIZE, clientCharCfgUUID },
        GATT_PERMIT_READ | GATT_PERMIT_WRITE,
        0,
        (uint8 *)&STEMMeter_Service_SENSOR4DATAConfig
      },
    // CONFIG Characteristic Declaration
    {
      { ATT_BT_UUID_SIZE, characterUUID },
      GATT_PERMIT_READ,
      0,
      &STEMMeter_Service_CONFIGProps
    },
      // CONFIG Characteristic Value
      {
        { ATT_UUID_SIZE, STEMMeter_Service_CONFIGUUID },
        GATT_PERMIT_READ | GATT_PERMIT_WRITE,
        0,
        STEMMeter_Service_CONFIGVal
      },
      // CONFIG CCCD
      {
        { ATT_BT_UUID_SIZE, clientCharCfgUUID },
        GATT_PERMIT_READ | GATT_PERMIT_WRITE,
        0,
        (uint8 *)&STEMMeter_Service_CONFIGConfig
      },
    // BATTERYDATA Characteristic Declaration
    {
      { ATT_BT_UUID_SIZE, characterUUID },
      GATT_PERMIT_READ,
      0,
      &STEMMeter_Service_BATTERYDATAProps
    },
      // BATTERYDATA Characteristic Value
      {
        { ATT_UUID_SIZE, STEMMeter_Service_BATTERYDATAUUID },
        GATT_PERMIT_READ,
        0,
        STEMMeter_Service_BATTERYDATAVal
      },
      // BATTERYDATA CCCD
      {
        { ATT_BT_UUID_SIZE, clientCharCfgUUID },
        GATT_PERMIT_READ | GATT_PERMIT_WRITE,
        0,
        (uint8 *)&STEMMeter_Service_BATTERYDATAConfig
      },
    // TIME Characteristic Declaration
    {
      { ATT_BT_UUID_SIZE, characterUUID },
      GATT_PERMIT_READ,
      0,
      &STEMMeter_Service_TIMEProps
    },
      // TIME Characteristic Value
      {
        { ATT_UUID_SIZE, STEMMeter_Service_TIMEUUID },
        GATT_PERMIT_READ | GATT_PERMIT_WRITE,
        0,
        STEMMeter_Service_TIMEVal
      },
      // TIME CCCD
      {
        { ATT_BT_UUID_SIZE, clientCharCfgUUID },
        GATT_PERMIT_READ | GATT_PERMIT_WRITE,
        0,
        (uint8 *)&STEMMeter_Service_TIMEConfig
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
  STEMMeter_Service_SENSOR1DATAConfig = (gattCharCfg_t *)ICall_malloc( sizeof(gattCharCfg_t) * linkDBNumConns );
  if ( STEMMeter_Service_SENSOR1DATAConfig == NULL )
  {
    return ( bleMemAllocError );
  }

  // Initialize Client Characteristic Configuration attributes
  GATTServApp_InitCharCfg( INVALID_CONNHANDLE, STEMMeter_Service_SENSOR1DATAConfig );
  // Allocate Client Characteristic Configuration table
  STEMMeter_Service_SENSOR2DATAConfig = (gattCharCfg_t *)ICall_malloc( sizeof(gattCharCfg_t) * linkDBNumConns );
  if ( STEMMeter_Service_SENSOR2DATAConfig == NULL )
  {
    return ( bleMemAllocError );
  }

  // Initialize Client Characteristic Configuration attributes
  GATTServApp_InitCharCfg( INVALID_CONNHANDLE, STEMMeter_Service_SENSOR2DATAConfig );
  // Allocate Client Characteristic Configuration table
  STEMMeter_Service_SENSOR3DATAConfig = (gattCharCfg_t *)ICall_malloc( sizeof(gattCharCfg_t) * linkDBNumConns );
  if ( STEMMeter_Service_SENSOR3DATAConfig == NULL )
  {
    return ( bleMemAllocError );
  }

  // Initialize Client Characteristic Configuration attributes
  GATTServApp_InitCharCfg( INVALID_CONNHANDLE, STEMMeter_Service_SENSOR3DATAConfig );
  // Allocate Client Characteristic Configuration table
  STEMMeter_Service_SENSOR4DATAConfig = (gattCharCfg_t *)ICall_malloc( sizeof(gattCharCfg_t) * linkDBNumConns );
  if ( STEMMeter_Service_SENSOR4DATAConfig == NULL )
  {
    return ( bleMemAllocError );
  }

  // Initialize Client Characteristic Configuration attributes
  GATTServApp_InitCharCfg( INVALID_CONNHANDLE, STEMMeter_Service_SENSOR4DATAConfig );
  // Allocate Client Characteristic Configuration table
  STEMMeter_Service_CONFIGConfig = (gattCharCfg_t *)ICall_malloc( sizeof(gattCharCfg_t) * linkDBNumConns );
  if ( STEMMeter_Service_CONFIGConfig == NULL )
  {
    return ( bleMemAllocError );
  }

  // Initialize Client Characteristic Configuration attributes
  GATTServApp_InitCharCfg( INVALID_CONNHANDLE, STEMMeter_Service_CONFIGConfig );
  // Allocate Client Characteristic Configuration table
  STEMMeter_Service_BATTERYDATAConfig = (gattCharCfg_t *)ICall_malloc( sizeof(gattCharCfg_t) * linkDBNumConns );
  if ( STEMMeter_Service_BATTERYDATAConfig == NULL )
  {
    return ( bleMemAllocError );
  }

  // Initialize Client Characteristic Configuration attributes
  GATTServApp_InitCharCfg( INVALID_CONNHANDLE, STEMMeter_Service_BATTERYDATAConfig );
  // Allocate Client Characteristic Configuration table
  STEMMeter_Service_TIMEConfig = (gattCharCfg_t *)ICall_malloc( sizeof(gattCharCfg_t) * linkDBNumConns );
  if ( STEMMeter_Service_TIMEConfig == NULL )
  {
    return ( bleMemAllocError );
  }

  // Initialize Client Characteristic Configuration attributes
  GATTServApp_InitCharCfg( INVALID_CONNHANDLE, STEMMeter_Service_TIMEConfig );
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
        memcpy(STEMMeter_Service_SENSOR1DATAVal, value, len);

        // Try to send notification.
        GATTServApp_ProcessCharCfg( STEMMeter_Service_SENSOR1DATAConfig, (uint8_t *)&STEMMeter_Service_SENSOR1DATAVal, FALSE,
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
        memcpy(STEMMeter_Service_SENSOR2DATAVal, value, len);

        // Try to send notification.
        GATTServApp_ProcessCharCfg( STEMMeter_Service_SENSOR2DATAConfig, (uint8_t *)&STEMMeter_Service_SENSOR2DATAVal, FALSE,
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
        memcpy(STEMMeter_Service_SENSOR3DATAVal, value, len);

        // Try to send notification.
        GATTServApp_ProcessCharCfg( STEMMeter_Service_SENSOR3DATAConfig, (uint8_t *)&STEMMeter_Service_SENSOR3DATAVal, FALSE,
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
        memcpy(STEMMeter_Service_SENSOR4DATAVal, value, len);

        // Try to send notification.
        GATTServApp_ProcessCharCfg( STEMMeter_Service_SENSOR4DATAConfig, (uint8_t *)&STEMMeter_Service_SENSOR4DATAVal, FALSE,
                                    STEMMeter_ServiceAttrTbl, GATT_NUM_ATTRS( STEMMeter_ServiceAttrTbl ),
                                    INVALID_TASK_ID,  STEMMeter_Service_ReadAttrCB);
      }
      else
      {
        ret = bleInvalidRange;
      }
      break;

    case STEMMETER_SERVICE_CONFIG:
      if ( len == STEMMETER_SERVICE_CONFIG_LEN )
      {
        memcpy(STEMMeter_Service_CONFIGVal, value, len);

        // Try to send notification.
        GATTServApp_ProcessCharCfg( STEMMeter_Service_CONFIGConfig, (uint8_t *)&STEMMeter_Service_CONFIGVal, FALSE,
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
        memcpy(STEMMeter_Service_BATTERYDATAVal, value, len);

        // Try to send notification.
        GATTServApp_ProcessCharCfg( STEMMeter_Service_BATTERYDATAConfig, (uint8_t *)&STEMMeter_Service_BATTERYDATAVal, FALSE,
                                    STEMMeter_ServiceAttrTbl, GATT_NUM_ATTRS( STEMMeter_ServiceAttrTbl ),
                                    INVALID_TASK_ID,  STEMMeter_Service_ReadAttrCB);
      }
      else
      {
        ret = bleInvalidRange;
      }
      break;

    case STEMMETER_SERVICE_TIME:
      if ( len == STEMMETER_SERVICE_TIME_LEN )
      {
        memcpy(STEMMeter_Service_TIMEVal, value, len);

        // Try to send notification.
        GATTServApp_ProcessCharCfg( STEMMeter_Service_TIMEConfig, (uint8_t *)&STEMMeter_Service_TIMEVal, FALSE,
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
 */
bStatus_t STEMMeter_Service_GetParameter( uint8 param, void *value )
{
  bStatus_t ret = SUCCESS;
  switch ( param )
  {
    case STEMMETER_SERVICE_CONFIG:
      memcpy(value, STEMMeter_Service_CONFIGVal, STEMMETER_SERVICE_CONFIG_LEN);
      break;

    case STEMMETER_SERVICE_TIME:
      memcpy(value, STEMMeter_Service_TIMEVal, STEMMETER_SERVICE_TIME_LEN);
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

  // See if request is regarding the SENSOR1DATA Characteristic Value
if ( ! memcmp(pAttr->type.uuid, STEMMeter_Service_SENSOR1DATAUUID, pAttr->type.len) )
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
  // See if request is regarding the SENSOR2DATA Characteristic Value
else if ( ! memcmp(pAttr->type.uuid, STEMMeter_Service_SENSOR2DATAUUID, pAttr->type.len) )
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
  // See if request is regarding the SENSOR3DATA Characteristic Value
else if ( ! memcmp(pAttr->type.uuid, STEMMeter_Service_SENSOR3DATAUUID, pAttr->type.len) )
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
  // See if request is regarding the SENSOR4DATA Characteristic Value
else if ( ! memcmp(pAttr->type.uuid, STEMMeter_Service_SENSOR4DATAUUID, pAttr->type.len) )
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
  // See if request is regarding the CONFIG Characteristic Value
else if ( ! memcmp(pAttr->type.uuid, STEMMeter_Service_CONFIGUUID, pAttr->type.len) )
  {
    if ( offset > STEMMETER_SERVICE_CONFIG_LEN )  // Prevent malicious ATT ReadBlob offsets.
    {
      status = ATT_ERR_INVALID_OFFSET;
    }
    else
    {
      *pLen = MIN(maxLen, STEMMETER_SERVICE_CONFIG_LEN - offset);  // Transmit as much as possible
      memcpy(pValue, pAttr->pValue + offset, *pLen);
    }
  }
  // See if request is regarding the BATTERYDATA Characteristic Value
else if ( ! memcmp(pAttr->type.uuid, STEMMeter_Service_BATTERYDATAUUID, pAttr->type.len) )
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
  // See if request is regarding the TIME Characteristic Value
else if ( ! memcmp(pAttr->type.uuid, STEMMeter_Service_TIMEUUID, pAttr->type.len) )
  {
    if ( offset > STEMMETER_SERVICE_TIME_LEN )  // Prevent malicious ATT ReadBlob offsets.
    {
      status = ATT_ERR_INVALID_OFFSET;
    }
    else
    {
      *pLen = MIN(maxLen, STEMMETER_SERVICE_TIME_LEN - offset);  // Transmit as much as possible
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
  // See if request is regarding the CONFIG Characteristic Value
  else if ( ! memcmp(pAttr->type.uuid, STEMMeter_Service_CONFIGUUID, pAttr->type.len) )
  {
    if ( offset + len > STEMMETER_SERVICE_CONFIG_LEN )
    {
      status = ATT_ERR_INVALID_OFFSET;
    }
    else
    {
      // Copy pValue into the variable we point to from the attribute table.
      memcpy(pAttr->pValue + offset, pValue, len);

      // Only notify application if entire expected value is written
      if ( offset + len == STEMMETER_SERVICE_CONFIG_LEN)
        paramID = STEMMETER_SERVICE_CONFIG;
    }
  }
  // See if request is regarding the TIME Characteristic Value
  else if ( ! memcmp(pAttr->type.uuid, STEMMeter_Service_TIMEUUID, pAttr->type.len) )
  {
    if ( offset + len > STEMMETER_SERVICE_TIME_LEN )
    {
      status = ATT_ERR_INVALID_OFFSET;
    }
    else
    {
      // Copy pValue into the variable we point to from the attribute table.
      memcpy(pAttr->pValue + offset, pValue, len);

      // Only notify application if entire expected value is written
      if ( offset + len == STEMMETER_SERVICE_TIME_LEN)
        paramID = STEMMETER_SERVICE_TIME;
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
