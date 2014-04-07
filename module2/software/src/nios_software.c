
#include <stdio.h>
#include "altera_up_avalon_rs232.h"
#include <string.h>
#include <system.h>
#include "../inc/string_graphics.h"

int main()
{
	int i;
	unsigned char data;
	unsigned char parity;
	unsigned char message[] = "EECE381 is so much fun";
	unsigned char* buffer;
	initBuffers();

	//INITIALIZATION
	printf("UART Initialization\n");
	alt_up_rs232_dev* uart = alt_up_rs232_open_dev(RS232_0_NAME);

	printf("Clearing read buffer to start\n");

	while (alt_up_rs232_get_used_space_in_read_FIFO(uart)) {
		alt_up_rs232_read_data(uart, &data, &parity);
	}

	// WAIT FOR MESSAGE TO COME FROM MIDDLEMAN (SENT FROM ANDROID)

	printf("Waiting for data to come from the Middleman\n");

	while (alt_up_rs232_get_used_space_in_read_FIFO(uart) == 0) ;

	// First byte is the number of characters in our message
	alt_up_rs232_read_data(uart, &data, &parity);
	int num_to_receive = (int)data;

	//create a buffer the same as the size of the message sent from android
	buffer = (unsigned char*) malloc(num_to_receive * sizeof(unsigned char));

	printf("About to receive %d characters:\n", num_to_receive);

	for (i = 0; i < num_to_receive; i++) {
		while (alt_up_rs232_get_used_space_in_read_FIFO(uart) == 0) ;

		alt_up_rs232_read_data(uart, &data, &parity);

		buffer[i] = (char) data;
	}
	buffer[i] = '\0';

	//write string to vga display
	writeString(buffer, 0, 0);
	printf("\n");

	//BEGIN SENDING MESSAGE FROM DE2 TO ANDROID (RETURNS ORIGINAL MESSAGE)
	printf("Clearing read buffer to start\n");

	while (alt_up_rs232_get_used_space_in_read_FIFO(uart)) {
		alt_up_rs232_read_data(uart, &data, &parity);
	}

	printf("Sending the message to the Middleman\n");

	// Start with the number of bytes in our message

	alt_up_rs232_write_data(uart, (unsigned char) strlen(buffer));

	// Now send the actual message to the Middleman

	for (i = 0; i < strlen(buffer); i++) {
			alt_up_rs232_write_data(uart, buffer[i]);
	}

	printf("Message Echo Complete\n");

	free(buffer);

	return 0;
}
