
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
	char buffer[256];
	initBuffers();

	printf("UART Initialization\n");
	alt_up_rs232_dev* uart = alt_up_rs232_open_dev(RS232_0_NAME);

	printf("Clearing read buffer to start\n");

	while (alt_up_rs232_get_used_space_in_read_FIFO(uart)) {
		alt_up_rs232_read_data(uart, &data, &parity);
	}

	printf("Sending the message to the Middleman\n");

	// Start with the number of bytes in our message

	alt_up_rs232_write_data(uart, (unsigned char) strlen(message));

	// Now send the actual message to the Middleman

	for (i = 0; i < strlen(message); i++) {
		alt_up_rs232_write_data(uart, message[i]);
	}

	// Now receive the message from the Middleman

	printf("Waiting for data to come back from the Middleman\n");

	while (alt_up_rs232_get_used_space_in_read_FIFO(uart) == 0) ;

	// First byte is the number of characters in our message

	alt_up_rs232_read_data(uart, &data, &parity);
	int num_to_receive = (int)data;

	printf("About to receive %d characters:\n", num_to_receive);

	for (i = 0; i < num_to_receive; i++) {
		while (alt_up_rs232_get_used_space_in_read_FIFO(uart) == 0) ;

		alt_up_rs232_read_data(uart, &data, &parity);

		buffer[i] = (char) data;
	}
	buffer[i] = '\0';\
	writeString(buffer, 0, 0);
	printf("\n");
	printf("Message Echo Complete\n");

	return 0;
}

