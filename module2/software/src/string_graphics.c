/*
 * VGA
 * Display functions tron graphics
 */

#include <stdio.h>
#include <stdlib.h>
#include <system.h>
#include "io.h"
#include "../inc/string_graphics.h"
#include "altera_up_avalon_video_pixel_buffer_dma.h"
#include "altera_up_avalon_video_character_buffer_with_dma.h"

// Declare buffer pointer
alt_up_char_buffer_dev *char_buffer;
alt_up_pixel_buffer_dma_dev* pixel_buffer;


/* Initialze the character and pixel buffers for writing */
void initBuffers(void)
{
	// Use the name of your pixel buffer DMA core
	pixel_buffer =	alt_up_pixel_buffer_dma_open_dev(PIXEL_BUFFER_DMA_NAME);
	unsigned int pixel_buffer_addr1 = PIXEL_BUFFER_BASE;
	unsigned int pixel_buffer_addr2 = PIXEL_BUFFER_BASE + (320 * 240 * 2);
	// Set the 1st buffer address
	alt_up_pixel_buffer_dma_change_back_buffer_address(pixel_buffer, pixel_buffer_addr1);
	// Swap buffers – we have to swap because there is only an API function
	// to set the address of the background buffer.
	alt_up_pixel_buffer_dma_swap_buffers(pixel_buffer);
	while (alt_up_pixel_buffer_dma_check_swap_buffers_status(pixel_buffer));
	// Set the 2nd buffer address
	alt_up_pixel_buffer_dma_change_back_buffer_address(pixel_buffer, pixel_buffer_addr2);
	// Clear both buffers (this makes all pixels black)
	alt_up_pixel_buffer_dma_clear_screen(pixel_buffer, 0);
	alt_up_pixel_buffer_dma_clear_screen(pixel_buffer, 1);

	char_buffer = alt_up_char_buffer_open_dev("/dev/char_drawer");
	alt_up_char_buffer_init(char_buffer);
	alt_up_char_buffer_clear(char_buffer);
}

/* Writes a character string to the given xy coordinates */
void writeString(char* string, int x, int y)
{
	// Write some text
	if(alt_up_char_buffer_string(char_buffer, string, x, y) != 0)
		printf("Character Buffer Print Fail/n");
}

/* Clear Character buffer */
void clearCharBuff(void)
{
	alt_up_char_buffer_clear(char_buffer);
}


