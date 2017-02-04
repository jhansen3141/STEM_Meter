#include <stdint.h>
#include <stdlib.h>
#include <string.h>
#include "cmd.h"


// command line message buffer and pointer
static uint8_t msg[MAX_MSG_SIZE];
static uint8_t *msg_ptr;

// linked list for command table
static cmd_t *cmd_tbl_list, *cmd_tbl;

void cmd_parse(char *cmd) {
    uint8_t argc, i = 0;
    char *argv[30];
    cmd_t *cmd_entry;


    // parse the command line statement and break it up into space-delimited
    // strings. the array of strings will be saved in the argv array.
    argv[i] = strtok(cmd, " ");
    do
    {
        argv[++i] = strtok(NULL, " ");
    } while ((i < 30) && (argv[i] != NULL));

    // save off the number of arguments for the particular command.
    argc = i;


    // parse the command table for valid command. use argv[0] which is the
    // actual command name typed in at the prompt
    for (cmd_entry = cmd_tbl; cmd_entry != NULL; cmd_entry = cmd_entry->next)
    {
        if (!strcmp(argv[0], cmd_entry->cmd))
        {
            cmd_entry->func(argc, argv);
            return;
        }
    }

    // command not recognized. print message and re-generate prompt.
   // UARTSend("Not recognized\n",15);
    msg_ptr = msg;
}

void cmd_handler(char c) {
//    static uint8_t MsgLength = 0;

    static char CmdRepeatable = 0;

    switch (c)
    {
    case '\r':
        // terminate the msg and reset the msg ptr. then send
        // it to the handler for processing.
        *msg_ptr = '\0';
        cmd_parse((char *)msg);
        // indicate that a valid command was previously entered
        CmdRepeatable = 1;
        break;

    case '\b':
        if (msg_ptr > msg)
        {
            msg_ptr--;
        }
        break;

    default:
        // added to repeat commands with a <CR>
        if (CmdRepeatable == 1)
        {
          msg_ptr = msg;
          CmdRepeatable = 0;
        }
        // normal character entered. add it to the buffer
        *msg_ptr++ = c;
        break;
    }
}

/**************************************************************************/
/*!
    This function should be set inside the main loop. It needs to be called
    constantly to check if there is any available input at the command prompt.
*/
/**************************************************************************/
//void cmdPoll()
//{
//	if(UARTCharsAvail(CONSOLE)) {
//    	cmd_handler();
//	}
//}


void cmdInit() {
    // init the msg ptr
    msg_ptr = msg;
    // init the command table
    cmd_tbl_list = NULL;
}

void cmdAdd(char *name, void (*func)(int argc, char **argv)) {
    // alloc memory for command struct
    cmd_tbl = (cmd_t *)malloc(sizeof(cmd_t));

    // alloc memory for command name
    char *cmd_name = (char *)malloc(strlen(name)+1);

    // copy command name
    strcpy(cmd_name, name);

    // terminate the command name
    cmd_name[strlen(name)] = '\0';

    // fill out structure
    cmd_tbl->cmd = cmd_name;
    cmd_tbl->func = func;
    cmd_tbl->next = cmd_tbl_list;
    cmd_tbl_list = cmd_tbl;
}

uint32_t cmdStr2Num(char *str, uint8_t base) {
    return strtol(str, NULL, base);
}



