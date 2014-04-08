
#include <stdio.h>
#include "altera_up_avalon_rs232.h"
#include <string.h>
#include <system.h>
#include "../inc/string_graphics.h"

#define PUSH 0	// 0 push
#define PULL 1	// 1 pull

#define TRUE 1
#define FALSE 0

int main()
{
	int i;
	unsigned char data;
	unsigned char parity;
	char* buffer;
	int bufSize = 0;
	initBuffers();

	//INITIALIZATION
	printf("UART Initialization\n");
	alt_up_rs232_dev* uart = alt_up_rs232_open_dev(RS232_0_NAME);

	printf("Clearing read buffer to start\n");

	while (alt_up_rs232_get_used_space_in_read_FIFO(uart)) {
		alt_up_rs232_read_data(uart, &data, &parity);
	}

	while(TRUE) {
		// WAIT FOR MESSAGE TO COME FROM MIDDLEMAN (SENT FROM ANDROID)
		printf("Waiting for data to come from the Middleman\n");

		while (alt_up_rs232_get_used_space_in_read_FIFO(uart) == 0) ;
		// First byte is the number of characters in our message
		alt_up_rs232_read_data(uart, &data, &parity);
		int num_to_receive1 = (int)data;

		while (alt_up_rs232_get_used_space_in_read_FIFO(uart) == 0) ;
		// First byte is the number of characters in our message
		alt_up_rs232_read_data(uart, &data, &parity);
		int num_to_receive2 = (int)data;

		int num_to_receive = (num_to_receive2 << 8) | num_to_receive1;

		// action code
		// 0 push
		// 1 pull
		while (alt_up_rs232_get_used_space_in_read_FIFO(uart) == 0) ;

		alt_up_rs232_read_data(uart, &data, &parity);
		int actionCode = (int) data;

		if(actionCode == PULL) {
			if(bufSize == 0) {
				printf("Clearing read buffer to start\n");

				while (alt_up_rs232_get_used_space_in_read_FIFO(uart)) {
					alt_up_rs232_read_data(uart, &data, &parity);
				}

				printf("Sending the message to the Middleman\n");

				// send msg size of 0
				alt_up_rs232_write_data(uart, (unsigned char) 0);
			} else {

				printf("Clearing read buffer to start\n");

				while (alt_up_rs232_get_used_space_in_read_FIFO(uart)) {
					alt_up_rs232_read_data(uart, &data, &parity);
				}


				printf("Sending the message to the Middleman\n");


				// Now send the actual message to the Middleman

				for (i = 0; i < strlen(buffer); i++) {
						alt_up_rs232_write_data(uart, buffer[i]);
				}

				printf("Message Echo Complete\n");
			}
		} else {
			if(bufSize != 0) {
				free(buffer);
			}
			//create a buffer the same as the size of the message sent from android
			buffer = (char*) malloc(num_to_receive * sizeof(unsigned char));

			bufSize = num_to_receive;

			printf("About to receive %d characters:\n", num_to_receive);

			for (i = 0; i < num_to_receive; i++) {
				while (alt_up_rs232_get_used_space_in_read_FIFO(uart) == 0) ;

				alt_up_rs232_read_data(uart, &data, &parity);

				buffer[i] = (char) data;
			}
			buffer[i] = '\0';

			while (alt_up_rs232_get_used_space_in_read_FIFO(uart)) {
				alt_up_rs232_read_data(uart, &data, &parity);
			}

			printf("Sending the message to the Middleman\n");

			// send msg size of 0
			alt_up_rs232_write_data(uart, (unsigned char) 0);

			//write string to vga display
			clearCharBuff();
			int row = 0;
			char *pch;
			char *dispBuf = (char*) malloc(num_to_receive * sizeof(unsigned char));
			strcpy(dispBuf, buffer);
			pch = strtok(dispBuf, "\n");
			while(pch != NULL) {
				writeString(pch, 0, row);
				printf("%s\n", pch);
				pch = strtok(NULL, "\n");
				row += 1;
			}

			printf("\n");
		}

	}



	return 0;
}

