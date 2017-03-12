/*
 * uart.h
 *
 * Created: 11/3/2016 12:44:19 PM
 *  Author: Josh
 */ 


#ifndef UART_H_
#define UART_H_

void UARTInit(void);
void UARTWriteString(char *string);
void UARTWrite(uint8_t data);
void writeBaseUnitData(sensorData_t *data);


#endif /* UART_H_ */