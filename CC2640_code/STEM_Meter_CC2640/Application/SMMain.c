// Josh Hansen
// CEEN 4360 - Fall 2016
// Phase II

#include <string.h>

#define xdc_runtime_Log_DISABLE_ALL 1  // Add to disable logs from this file

#include <ti/sysbios/knl/Task.h>
#include <ti/sysbios/knl/Semaphore.h>
#include <ti/sysbios/knl/Queue.h>

#include <ti/drivers/PIN.h>

#include <xdc/runtime/Log.h>
#include <xdc/runtime/Diags.h>

// Stack headers
#include <hci_tl.h>
#include <gap.h>
#include <gatt.h>
#include <gapgattserver.h>
#include <gattservapp.h>
#include <osal_snv.h>
#include <gapbondmgr.h>
#include <peripheral.h>
#include <ICallBleAPIMSG.h>

#include <devinfoservice.h>
#include "SMMain.h"

#include "util.h"
#include "STEMMeter_Service.h"
#include "Board.h"
#include "SPICommands.h"
#include "BatteryMonitor.h"

/*********************************************************************
 * CONSTANTS
 */
// Advertising interval when device is discoverable (units of 625us, 160=100ms)
#define DEFAULT_ADVERTISING_INTERVAL          100

// Limited discoverable mode advertises for 30.72s, and then stops
// General discoverable mode advertises indefinitely
#define DEFAULT_DISCOVERABLE_MODE             GAP_ADTYPE_FLAGS_GENERAL

// Default pass-code used for pairing.
#define DEFAULT_PASSCODE                      000000

// Task configuration
#define PRZ_TASK_PRIORITY                     1

#ifndef PRZ_TASK_STACK_SIZE
#define PRZ_TASK_STACK_SIZE                   1140
#endif

// Internal Events for RTOS application
#define PRZ_STATE_CHANGE_EVT                  0x0001
#define PRZ_CHAR_CHANGE_EVT                   0x0002
#define PRZ_PERIODIC_EVT                      0x0004
#define PRZ_CONN_EVT_END_EVT                  0x0008

/*********************************************************************
 * TYPEDEFS
 */
// Types of messages that can be sent to the user application task from other
// tasks or interrupts. Note: Messages from BLE Stack are sent differently.

// Struct for messages sent to the application task
typedef struct
{
  Queue_Elem       _elem;
  app_msg_types_t  type;
  uint8_t          pdu[];
} app_msg_t;

//// Struct for messages about characteristic data
//typedef struct
//{
//  uint16_t svcUUID; // UUID of the service
//  uint16_t dataLen; //
//  uint8_t  paramID; // Index of the characteristic
//  uint8_t  data[];  // Flexible array member, extended to malloc - sizeof(.)
//} char_data_t;

// Struct for message about sending/requesting passcode from peer.
typedef struct
{
  uint16_t connHandle;
  uint8_t  uiInputs;
  uint8_t  uiOutputs;
} passcode_req_t;

// Struct for messages from a service
typedef struct
{
  Queue_Elem _elem;
  uint16_t svcUUID;
  uint16_t dataLen;
  uint8_t  paramID;
  uint8_t  data[]; // Flexible array member, extended to malloc - sizeof(.)
} server_char_write_t;


/*********************************************************************
 * LOCAL VARIABLES
 */

// Entity ID globally used to check for source and/or destination of messages
static ICall_EntityID selfEntity;

// Semaphore globally used to post events to the application thread
static ICall_Semaphore BLESem;

// Queue object used for application messages.
static Queue_Struct applicationMsgQ;
static Queue_Handle hApplicationMsgQ;

// Queue object used for service messages.
static Queue_Struct serviceMsgQ;
static Queue_Handle hServiceMsgQ;

// Task configuration
Task_Struct przTask;
Char przTaskStack[PRZ_TASK_STACK_SIZE];


// GAP - SCAN RSP data (max size = 31 bytes)
static uint8_t scanRspData[] =
{
  // No scan response data provided.
  0x00 // Placeholder to keep the compiler happy.
};

// GAP - Advertisement data (max size = 31 bytes, though this is
// best kept short to conserve power while advertisting)
static uint8_t advertData[] =
{
  // Flags; this sets the device to use limited discoverable
  // mode (advertises for 30 seconds at a time) or general
  // discoverable mode (advertises indefinitely), depending
  // on the DEFAULT_DISCOVERY_MODE define.
  0x02,   // length of this data
  GAP_ADTYPE_FLAGS,
  DEFAULT_DISCOVERABLE_MODE | GAP_ADTYPE_FLAGS_BREDR_NOT_SUPPORTED,

  // complete name
  11,
  GAP_ADTYPE_LOCAL_NAME_COMPLETE,
  'S', 'T', 'E', 'M', ' ', 'M', 'e', 't', 'e', 'r',

};

// GAP GATT Attributes
static uint8_t attDeviceName[GAP_DEVICE_NAME_LEN] = "STEM Meter";
static bool isAdvertising = TRUE;

// Globals used for ATT Response retransmission
static gattMsgEvent_t *pAttRsp = NULL;
static uint8_t rspTxRetry = 0;

/*********************************************************************
 * LOCAL FUNCTIONS
 */

static void ProjectZero_init( void );
static void ProjectZero_taskFxn(UArg a0, UArg a1);

static void user_processApplicationMessage(app_msg_t *pMsg);
static uint8_t ProjectZero_processStackMsg(ICall_Hdr *pMsg);
static uint8_t ProjectZero_processGATTMsg(gattMsgEvent_t *pMsg);

static void ProjectZero_sendAttRsp(void);
static uint8_t ProjectZero_processGATTMsg(gattMsgEvent_t *pMsg);
static void ProjectZero_freeAttRsp(uint8_t status);

static void user_processGapStateChangeEvt(gaprole_States_t newState);
static void user_gapStateChangeCB(gaprole_States_t newState);
static void user_gapBondMgr_passcodeCB(uint8_t *deviceAddr, uint16_t connHandle,
                                       uint8_t uiInputs, uint8_t uiOutputs);
static void user_gapBondMgr_pairStateCB(uint16_t connHandle, uint8_t state,
                                        uint8_t status);

// Declaration of service callback handlers
static void user_STEMMeter_ServiceValueChangeCB(uint8_t paramID); // Callback from the service.
static void user_STEMMeter_Service_ValueChangeDispatchHandler(server_char_write_t *pWrite); // Local handler called from the Task context of this task.
// Task handler for sending notifications.
static void user_updateCharVal(char_data_t *pCharData);

// Utility functions
static void user_enqueueRawAppMsg(app_msg_types_t appMsgType, uint8_t *pData, uint16_t len );


/*********************************************************************
 * PROFILE CALLBACKS
 */

// GAP Role Callbacks
static gapRolesCBs_t user_gapRoleCBs =
{
  user_gapStateChangeCB     // Profile State Change Callbacks
};

// GAP Bond Manager Callbacks
static gapBondCBs_t user_bondMgrCBs =
{
  user_gapBondMgr_passcodeCB, // Passcode callback
  user_gapBondMgr_pairStateCB // Pairing / Bonding state Callback
};

/*
 * Callbacks in the user application for events originating from BLE services.
 */

// Service callback function implementation
// STEMMeter_Service callback handler. The type STEMMeter_ServiceCBs_t is defined in STEMMeter_Service.h
static STEMMeter_ServiceCBs_t user_STEMMeter_ServiceCBs =
{
  user_STEMMeter_ServiceValueChangeCB // Characteristic value change callback handler
};
/*********************************************************************
 * PUBLIC FUNCTIONS
 */

/*
 * @brief   Task creation function for the user task.
 *
 * @param   None.
 *
 * @return  None.
 */
void STEMMeterBLE_createTask(void) {
  Task_Params taskParams;

  // Configure task
  Task_Params_init(&taskParams);
  taskParams.stack = przTaskStack;
  taskParams.stackSize = PRZ_TASK_STACK_SIZE;
  taskParams.priority = PRZ_TASK_PRIORITY;

  Task_construct(&przTask, ProjectZero_taskFxn, &taskParams, NULL);
}


static void ProjectZero_init(void) {
  // ******************************************************************
  // NO STACK API CALLS CAN OCCUR BEFORE THIS CALL TO ICall_registerApp
  // ******************************************************************
  // Register the current thread as an ICall dispatcher application
  // so that the application can send and receive messages via ICall to Stack.
  ICall_registerApp(&selfEntity, &BLESem);

  // Initialize queue for application messages.
  // Note: Used to transfer control to application thread from e.g. interrupts.
  Queue_construct(&applicationMsgQ, NULL);
  hApplicationMsgQ = Queue_handle(&applicationMsgQ);

  Queue_construct(&serviceMsgQ,NULL);
  hServiceMsgQ = Queue_handle(&serviceMsgQ);


  // ******************************************************************
  // Hardware initialization
  // ******************************************************************



  // ******************************************************************
  // BLE Stack initialization
  // ******************************************************************

  // Setup the GAP Peripheral Role Profile
  uint8_t initialAdvertEnable = TRUE;  // Advertise on power-up

  // By setting this to zero, the device will go into the waiting state after
  // being discoverable. Otherwise wait this long [ms] before advertising again.
  uint16_t advertOffTime = 0; // miliseconds

  // Set advertisement enabled.
  GAPRole_SetParameter(GAPROLE_ADVERT_ENABLED, sizeof(uint8_t),
                       &initialAdvertEnable);

  // Configure the wait-time before restarting advertisement automatically
  GAPRole_SetParameter(GAPROLE_ADVERT_OFF_TIME, sizeof(uint16_t),
                       &advertOffTime);

  // Initialize Scan Response data
  GAPRole_SetParameter(GAPROLE_SCAN_RSP_DATA, sizeof(scanRspData), scanRspData);

  // Initialize Advertisement data
  GAPRole_SetParameter(GAPROLE_ADVERT_DATA, sizeof(advertData), advertData);


  // Set advertising interval
  uint16_t advInt = DEFAULT_ADVERTISING_INTERVAL;

  GAP_SetParamValue(TGAP_LIM_DISC_ADV_INT_MIN, advInt);
  GAP_SetParamValue(TGAP_LIM_DISC_ADV_INT_MAX, advInt);
  GAP_SetParamValue(TGAP_GEN_DISC_ADV_INT_MIN, advInt);
  GAP_SetParamValue(TGAP_GEN_DISC_ADV_INT_MAX, advInt);

  // Set duration of advertisement before stopping in Limited adv mode.
  GAP_SetParamValue(TGAP_LIM_ADV_TIMEOUT, 30); // Seconds

  // ******************************************************************
  // BLE Bond Manager initialization
  // ******************************************************************
  uint32_t passkey = 0; // passkey "000000"
  uint8_t pairMode = GAPBOND_PAIRING_MODE_WAIT_FOR_REQ;
  uint8_t mitm = TRUE;
  uint8_t ioCap = GAPBOND_IO_CAP_DISPLAY_ONLY;
  uint8_t bonding = TRUE;

  GAPBondMgr_SetParameter(GAPBOND_DEFAULT_PASSCODE, sizeof(uint32_t),
                          &passkey);
  GAPBondMgr_SetParameter(GAPBOND_PAIRING_MODE, sizeof(uint8_t), &pairMode);
  GAPBondMgr_SetParameter(GAPBOND_MITM_PROTECTION, sizeof(uint8_t), &mitm);
  GAPBondMgr_SetParameter(GAPBOND_IO_CAPABILITIES, sizeof(uint8_t), &ioCap);
  GAPBondMgr_SetParameter(GAPBOND_BONDING_ENABLED, sizeof(uint8_t), &bonding);

  // ******************************************************************
  // BLE Service initialization
  // ******************************************************************

  // Add services to GATT server
  GGS_AddService(GATT_ALL_SERVICES);           // GAP
  GATTServApp_AddService(GATT_ALL_SERVICES);   // GATT attributes
  DevInfo_AddService();                        // Device Information Service

  // Set the device name characteristic in the GAP Profile
  GGS_SetParameter(GGS_DEVICE_NAME_ATT, GAP_DEVICE_NAME_LEN, attDeviceName);

  // Add services to GATT server and give ID of this task for Indication acks.

  // Placeholder variable for characteristic intialization
  uint8_t someVal[20] = {0};

  STEMMeter_Service_AddService();
  STEMMeter_Service_RegisterAppCBs(&user_STEMMeter_ServiceCBs);

  // Initalization of characteristics in STEMMeter_Service that are readable.
  STEMMeter_Service_SetParameter(STEMMETER_SERVICE_SENSOR1DATA, STEMMETER_SERVICE_SENSOR1DATA_LEN, &someVal);
  STEMMeter_Service_SetParameter(STEMMETER_SERVICE_SENSOR2DATA, STEMMETER_SERVICE_SENSOR2DATA_LEN, &someVal);
  STEMMeter_Service_SetParameter(STEMMETER_SERVICE_SENSOR3DATA, STEMMETER_SERVICE_SENSOR3DATA_LEN, &someVal);
  STEMMeter_Service_SetParameter(STEMMETER_SERVICE_SENSOR4DATA, STEMMETER_SERVICE_SENSOR4DATA_LEN, &someVal);
  STEMMeter_Service_SetParameter(STEMMETER_SERVICE_CONFIG, STEMMETER_SERVICE_CONFIG_LEN, &someVal);
  STEMMeter_Service_SetParameter(STEMMETER_SERVICE_BATTERYDATA, STEMMETER_SERVICE_BATTERYDATA_LEN, &someVal);
  STEMMeter_Service_SetParameter(STEMMETER_SERVICE_TIME, STEMMETER_SERVICE_TIME_LEN, &someVal);

  // Start the stack in Peripheral mode.
  VOID GAPRole_StartDevice(&user_gapRoleCBs);

  // Start Bond Manager
  VOID GAPBondMgr_Register(&user_bondMgrCBs);

  // Register with GAP for HCI/Host messages
  GAP_RegisterForMsgs(selfEntity);

  // Register for GATT local events and ATT Responses pending for transmission
  GATT_RegisterForMsgs(selfEntity);
}


/*
 * @brief   Application task entry point.
 *
 *          Invoked by TI-RTOS when BIOS_start is called. Calls an init function
 *          and enters an infinite loop waiting for messages.
 *
 *          Messages can be either directly from the BLE stack or from user code
 *          like Hardware Interrupt (Hwi) or a callback function.
 *
 *          The reason for sending messages to this task from e.g. Hwi's is that
 *          some RTOS and Stack APIs are not available in callbacks and so the
 *          actions that may need to be taken is dispatched to this Task.
 *
 * @param   a0, a1 - not used.
 *
 * @return  None.
 */
static void ProjectZero_taskFxn(UArg a0, UArg a1) {
	// Initialize application
	ProjectZero_init();

	// Application main loop
	while(1) {
		ICall_Errno errno = ICall_wait(ICALL_TIMEOUT_FOREVER);

		while (!Queue_empty(hServiceMsgQ)) {
			server_char_write_t *pWrite = Queue_dequeue(hServiceMsgQ);

			switch(pWrite->svcUUID) {
				case STEMMETER_SERVICE_SERV_UUID:
					user_STEMMeter_Service_ValueChangeDispatchHandler(pWrite);
					break;
		}
		// Free the message received from the service callback.
		ICall_free(pWrite);
		}

		if (errno == ICALL_ERRNO_SUCCESS) {
			ICall_EntityID dest;
			ICall_ServiceEnum src;
			ICall_HciExtEvt *pMsg = NULL;

			// Check if we got a signal because of a stack message
			if (ICall_fetchServiceMsg(&src, &dest,
								(void **)&pMsg) == ICALL_ERRNO_SUCCESS) {
				uint8 safeToDealloc = TRUE;

				if ((src == ICALL_SERVICE_CLASS_BLE) && (dest == selfEntity)) {
					ICall_Event *pEvt = (ICall_Event *)pMsg;

					// Check for event flags received (event signature 0xffff)
					if (pEvt->signature == 0xffff) {
						// Event received when a connection event is completed
						if (pEvt->event_flag & PRZ_CONN_EVT_END_EVT) {
							  // Try to retransmit pending ATT Response (if any)
							  ProjectZero_sendAttRsp();
						}
					}
					// It's a message from the stack and not an event.
					else {
						// Process inter-task message
						safeToDealloc = ProjectZero_processStackMsg((ICall_Hdr *)pMsg);
					}
				}

				if (pMsg && safeToDealloc) {
					ICall_freeMsg(pMsg);
				}
			}

			// Process messages sent from another task or another context.
			while (!Queue_empty(hApplicationMsgQ)) {
				app_msg_t *pMsg = Queue_dequeue(hApplicationMsgQ);

				// Process application-layer message probably sent from ourselves.
				user_processApplicationMessage(pMsg);

				// Free the received message.
				ICall_free(pMsg);
			}
		}
	}
}

void user_STEMMeter_Service_ValueChangeDispatchHandler(server_char_write_t *pWrite) {
	switch (pWrite->paramID) {

        case STEMMETER_SERVICE_CONFIG:
        	// Equeue the incomming Sensor config data to SPI
        	// task so it can be sent to TM4C123
			user_enqueueRawSPICommandsMsg(SENSOR_UPDATE_CONFIG_MSG,pWrite->data,20);
			break;
        case STEMMETER_SERVICE_TIME:
        	// Enqueue time data to SPI task to write to TM4C123
        	user_enqueueRawSPICommandsMsg(UPDATE_TIME_MSG,pWrite->data,20);
        	break;
      }
}

static void user_STEMMeter_ServiceValueChangeCB(uint8_t paramID) {
  // See STEMMeter_Service.h to compare paramID with characteristic value attribute.
  // Called in Stack Task context, so can't do processing here.

  // Send message to application message queue about received data.
  uint16_t readLen = 0; // How much to read via service API

	switch (paramID) {
		case STEMMETER_SERVICE_CONFIG:
		  readLen = STEMMETER_SERVICE_CONFIG_LEN;
		  break;
		case STEMMETER_SERVICE_TIME:
		  readLen = STEMMETER_SERVICE_TIME_LEN;
		  break;
	}

  // Allocate memory for the message.
  // Note: The message doesn't have to contain the data itself, as that's stored in
  //       a variable in the service. However, to prevent data loss if a new value is received
  //       before GetParameter is called, we call GetParameter now.
  server_char_write_t *pWrite = ICall_malloc(sizeof(server_char_write_t) + readLen);

  if (pWrite != NULL) {
    pWrite->svcUUID = STEMMETER_SERVICE_SERV_UUID;
    pWrite->dataLen = readLen;
    pWrite->paramID = paramID;
    // Get the data from the service API.
    // Note: Fixed length is used here, but changing the GetParameter signature is not
    //       a problem, in case variable length is needed.
    // Note: It could be just as well to send dataLen and a pointer to the received data directly to this callback, avoiding GetParameter alltogether.
    STEMMeter_Service_GetParameter( paramID, pWrite->data );

    // Enqueue the message using pointer to queue node element.
    Queue_enqueue(hServiceMsgQ, &pWrite->_elem);
    // Let application know there's a message
    Semaphore_post(BLESem);
  }
}


/*
 * @brief   Handle application messages
 *
 *          These are messages not from the BLE stack, but from the
 *          application itself.
 *
 *          For example, in a Software Interrupt (Swi) it is not possible to
 *          call any BLE APIs, so instead the Swi function must send a message
 *          to the application Task for processing in Task context.
 *
 * @param   pMsg  Pointer to the message of type app_msg_t.
 *
 * @return  None.
 */
static void user_processApplicationMessage(app_msg_t *pMsg) {
  char_data_t *pCharData = (char_data_t *)pMsg->pdu;

  switch (pMsg->type) {
    case APP_MSG_SERVICE_WRITE: /* Message about received value write */
      /* Call different handler per service */

      break;

    case APP_MSG_SERVICE_CFG: /* Message about received CCCD write */
      /* Call different handler per service */

      break;

    case APP_MSG_UPDATE_CHARVAL: /* Message from ourselves to send  */
      user_updateCharVal(pCharData);
      break;

    case APP_MSG_GAP_STATE_CHANGE: /* Message that GAP state changed  */
      user_processGapStateChangeEvt( *(gaprole_States_t *)pMsg->pdu );
      break;

    case APP_MSG_SEND_PASSCODE: /* Message about pairing PIN request */
      {
        passcode_req_t *pReq = (passcode_req_t *)pMsg->pdu;

        // Send passcode response.
        GAPBondMgr_PasscodeRsp(pReq->connHandle, SUCCESS, DEFAULT_PASSCODE);
      }
      break;

    case APP_MSG_TOGGLE_ADVERTISING:
    {
    	if(isAdvertising) {
    		isAdvertising = FALSE;
    		enqueueBatMonitortTaskMsg(BATMONITOR_MSG_BLU_LED_OFF);
    	}
    	else {
    		isAdvertising = TRUE;
    	}

		// Set advertisement enabled.
		GAPRole_SetParameter(GAPROLE_ADVERT_ENABLED, sizeof(uint8_t),
							 &isAdvertising);
    }
    	break;

  }
}


/******************************************************************************
 *****************************************************************************
 *
 *  Handlers of system/application events deferred to the user Task context.
 *  Invoked from the application Task function above.
 *
 *  Further down you can find the callback handler section containing the
 *  functions that defer their actions via messages to the application task.
 *
 ****************************************************************************
 *****************************************************************************/

/*
 * @brief   Process a pending GAP Role state change event.
 *
 * @param   newState - new state
 *
 * @return  None.
 */
static void user_processGapStateChangeEvt(gaprole_States_t newState)
{
  switch ( newState )
  {
    case GAPROLE_STARTED:
      {
        uint8_t ownAddress[B_ADDR_LEN];
        uint8_t systemId[DEVINFO_SYSTEM_ID_LEN];

        GAPRole_GetParameter(GAPROLE_BD_ADDR, ownAddress);

        // use 6 bytes of device address for 8 bytes of system ID value
        systemId[0] = ownAddress[0];
        systemId[1] = ownAddress[1];
        systemId[2] = ownAddress[2];

        // set middle bytes to zero
        systemId[4] = 0x00;
        systemId[3] = 0x00;

        // shift three bytes up
        systemId[7] = ownAddress[5];
        systemId[6] = ownAddress[4];
        systemId[5] = ownAddress[3];

        DevInfo_SetParameter(DEVINFO_SYSTEM_ID, DEVINFO_SYSTEM_ID_LEN, systemId);

      }
      break;

    case GAPROLE_ADVERTISING:
    	// Blue LED flashing when advertising
    	enqueueBatMonitortTaskMsg(BATMONITOR_MSG_BLE_LEG_TOGGLE);
      break;

    case GAPROLE_CONNECTED:
      {
        uint8_t peerAddress[B_ADDR_LEN];

        GAPRole_GetParameter(GAPROLE_CONN_BD_ADDR, peerAddress);
        // Blue LED Solid when connected
        enqueueBatMonitortTaskMsg(BATMONITOR_MSG_BLU_LED_ON);

       }
      break;

    case GAPROLE_CONNECTED_ADV:

      break;

    case GAPROLE_WAITING:

      break;

    case GAPROLE_WAITING_AFTER_TIMEOUT:

      break;

    case GAPROLE_ERROR:

      break;

    default:
      break;
  }
}


/*
 * @brief   Process an incoming BLE stack message.
 *
 *          This could be a GATT message from a peer device like acknowledgement
 *          of an Indication we sent, or it could be a response from the stack
 *          to an HCI message that the user application sent.
 *
 * @param   pMsg - message to process
 *
 * @return  TRUE if safe to deallocate incoming message, FALSE otherwise.
 */
static uint8_t ProjectZero_processStackMsg(ICall_Hdr *pMsg)
{
  uint8_t safeToDealloc = TRUE;

  switch (pMsg->event)
  {
    case GATT_MSG_EVENT:
      // Process GATT message
      safeToDealloc = ProjectZero_processGATTMsg((gattMsgEvent_t *)pMsg);
      break;

    case HCI_GAP_EVENT_EVENT:
      {
        // Process HCI message
        switch(pMsg->status)
        {
          case HCI_COMMAND_COMPLETE_EVENT_CODE:
            // Process HCI Command Complete Event

            break;

          default:
            break;
        }
      }
      break;

    default:
      // do nothing
      break;
  }

  return (safeToDealloc);
}


/*
 * @brief   Process GATT messages and events.
 *
 * @return  TRUE if safe to deallocate incoming message, FALSE otherwise.
 */
static uint8_t ProjectZero_processGATTMsg(gattMsgEvent_t *pMsg)
{
  // See if GATT server was unable to transmit an ATT response
  if (pMsg->hdr.status == blePending) {


    // No HCI buffer was available. Let's try to retransmit the response
    // on the next connection event.
    if (HCI_EXT_ConnEventNoticeCmd(pMsg->connHandle, selfEntity,
                                   PRZ_CONN_EVT_END_EVT) == SUCCESS)
    {
      // First free any pending response
      ProjectZero_freeAttRsp(FAILURE);

      // Hold on to the response message for retransmission
      pAttRsp = pMsg;

      // Don't free the response message yet
      return (FALSE);
    }
  }
  else if (pMsg->method == ATT_FLOW_CTRL_VIOLATED_EVENT)
  {
    // ATT request-response or indication-confirmation flow control is
    // violated. All subsequent ATT requests or indications will be dropped.
    // The app is informed in case it wants to drop the connection.


  }
  else if (pMsg->method == ATT_MTU_UPDATED_EVENT) {

  }
  else {
    // Got an expected GATT message from a peer.

  }

  // Free message payload. Needed only for ATT Protocol messages
  GATT_bm_free(&pMsg->msg, pMsg->method);

  // It's safe to free the incoming message
  return (TRUE);
}

/*
 *  Application error handling functions
 *****************************************************************************/

/*
 * @brief   Send a pending ATT response message.
 *
 *          The message is one that the stack was trying to send based on a
 *          peer request, but the response couldn't be sent because the
 *          user application had filled the TX queue with other data.
 *
 * @param   none
 *
 * @return  none
 */
static void ProjectZero_sendAttRsp(void)
{
  // See if there's a pending ATT Response to be transmitted
  if (pAttRsp != NULL)
  {
    uint8_t status;

    // Increment retransmission count
    rspTxRetry++;

    // Try to retransmit ATT response till either we're successful or
    // the ATT Client times out (after 30s) and drops the connection.
    status = GATT_SendRsp(pAttRsp->connHandle, pAttRsp->method, &(pAttRsp->msg));
    if ((status != blePending) && (status != MSG_BUFFER_NOT_AVAIL))
    {
      // Disable connection event end notice
      HCI_EXT_ConnEventNoticeCmd(pAttRsp->connHandle, selfEntity, 0);

      // We're done with the response message
      ProjectZero_freeAttRsp(status);
    }
    else {
      // Continue retrying

    }
  }
}

/*
 * @brief   Free ATT response message.
 *
 * @param   status - response transmit status
 *
 * @return  none
 */
static void ProjectZero_freeAttRsp(uint8_t status)
{
  // See if there's a pending ATT response message
  if (pAttRsp != NULL)
  {
    // See if the response was sent out successfully
    if (status == SUCCESS) {

    }
    else {


      // Free response payload
      GATT_bm_free(&pAttRsp->msg, pAttRsp->method);
    }

    // Free response message
    ICall_freeMsg(pAttRsp);

    // Reset our globals
    pAttRsp = NULL;
    rspTxRetry = 0;
  }
}


/******************************************************************************
 *****************************************************************************
 *
 *  Handlers of direct system callbacks.
 *
 *  Typically enqueue the information or request as a message for the
 *  application Task for handling.
 *
 ****************************************************************************
 *****************************************************************************/


/*
 *  Callbacks from the Stack Task context (GAP or Service changes)
 *****************************************************************************/

/**
 * Callback from GAP Role indicating a role state change.
 */
static void user_gapStateChangeCB(gaprole_States_t newState) {

  user_enqueueRawAppMsg( APP_MSG_GAP_STATE_CHANGE, (uint8_t *)&newState, sizeof(newState) );
}

/*
 * @brief   Passcode callback.
 *
 * @param   connHandle - connection handle
 * @param   uiInputs   - input passcode?
 * @param   uiOutputs  - display passcode?
 *
 * @return  none
 */
static void user_gapBondMgr_passcodeCB(uint8_t *deviceAddr, uint16_t connHandle,
                                       uint8_t uiInputs, uint8_t uiOutputs)
{
  passcode_req_t req =
  {
    .connHandle = connHandle,
    .uiInputs = uiInputs,
    .uiOutputs = uiOutputs
  };

  // Defer handling of the passcode request to the application, in case
  // user input is required, and because a BLE API must be used from Task.
  user_enqueueRawAppMsg(APP_MSG_SEND_PASSCODE, (uint8_t *)&req, sizeof(req));
}

/*
 * @brief   Pairing state callback.
 *
 * @param   connHandle - connection handle
 * @param   state      - pairing state
 * @param   status     - pairing status
 *
 * @return  none
 */
static void user_gapBondMgr_pairStateCB(uint16_t connHandle, uint8_t state,
                                        uint8_t status)
{
  if (state == GAPBOND_PAIRING_STATE_STARTED) {

  }
  else if (state == GAPBOND_PAIRING_STATE_COMPLETE)
  {
    if (status == SUCCESS) {

    }
    else {

    }
  }
  else if (state == GAPBOND_PAIRING_STATE_BONDED)
  {
    if (status == SUCCESS) {

    }
  }
}

/*
 * @brief  Generic message constructor for application messages.
 *
 *         Sends a message to the application for handling in Task context.
 *
 * @param  appMsgType    Enumerated type of message being sent.
 * @oaram  *pValue       Pointer to characteristic value
 * @param  len           Length of characteristic data
 */
static void user_enqueueRawAppMsg(app_msg_types_t appMsgType, uint8_t *pData,
                                  uint16_t len)
{
  // Allocate memory for the message.
  app_msg_t *pMsg = ICall_malloc( sizeof(app_msg_t) + len );

  if (pMsg != NULL)
  {
    pMsg->type = appMsgType;

    // Copy data into message
    memcpy(pMsg->pdu, pData, len);

    // Enqueue the message using pointer to queue node element.
    Queue_enqueue(hApplicationMsgQ, &pMsg->_elem);
    // Let application know there's a message.
    Semaphore_post(BLESem);
  }
}


/*
 * @brief  Convenience function for updating characteristic data via char_data_t
 *         structured message.
 *
 * @note   Must run in Task context in case BLE Stack APIs are invoked.
 *
 * @param  *pCharData  Pointer to struct with value to update.
 */
static void user_updateCharVal(char_data_t *pCharData) {
  switch(pCharData->svcUUID) {
    case STEMMETER_SERVICE_SENSOR1DATA_UUID:
    	STEMMeter_Service_SetParameter(STEMMETER_SERVICE_SENSOR1DATA, STEMMETER_SERVICE_SENSOR1DATA_LEN, pCharData->data);
    	break;

    case STEMMETER_SERVICE_SENSOR2DATA_UUID:
    	  STEMMeter_Service_SetParameter(STEMMETER_SERVICE_SENSOR2DATA, STEMMETER_SERVICE_SENSOR2DATA_LEN, pCharData->data);
    	  break;

    case STEMMETER_SERVICE_SENSOR3DATA_UUID:
    	STEMMeter_Service_SetParameter(STEMMETER_SERVICE_SENSOR3DATA, STEMMETER_SERVICE_SENSOR3DATA_LEN, pCharData->data);
    	break;

    case STEMMETER_SERVICE_SENSOR4DATA_UUID:
    	STEMMeter_Service_SetParameter(STEMMETER_SERVICE_SENSOR4DATA, STEMMETER_SERVICE_SENSOR4DATA_LEN, pCharData->data);
    break;

    case STEMMETER_SERVICE_BATTERYDATA_UUID:
    	STEMMeter_Service_SetParameter(STEMMETER_SERVICE_BATTERYDATA, STEMMETER_SERVICE_BATTERYDATA_LEN, pCharData->data);
    break;

  }
}


void enqueueSensorCharUpdate(uint16_t charUUID, uint8_t *pValue) {
	app_msg_t *pMsg = ICall_malloc( sizeof(app_msg_t) + sizeof(char_data_t) + 20);
	if (pMsg != NULL) {
		pMsg->type = APP_MSG_UPDATE_CHARVAL;
		char_data_t *pCharData = (char_data_t *)pMsg->pdu;
		pCharData->svcUUID = charUUID;
		memcpy(pCharData->data, pValue, 20);
		pCharData->dataLen = 20;

		Queue_enqueue(hApplicationMsgQ, &pMsg->_elem);
		Semaphore_post(BLESem);
	}
}

void enqueueBLEMainMsg(app_msg_types_t msgType) {
	app_msg_t *pMsg = ICall_malloc( sizeof(app_msg_t) );
	if (pMsg != NULL) {
		pMsg->type = msgType;
		Queue_enqueue(hApplicationMsgQ, &pMsg->_elem);
		Semaphore_post(BLESem);
	}
}



