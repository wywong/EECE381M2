/*
 * string_graphics.h
 *
 *  Created on: 2014-01-26
 *      Author: Brendan
 */

#ifndef STRING_GRAPHICS_H_
#define STRING_GRAPHICS_H_

/* Function Declaration */
/* Initialze the character and pixel buffers for writing */
void initBuffers(void);
/* Writes a character string to the given xy coordinates */
void writeString(char* string, int x, int y);
/* Clear Character buffer */
void clearCharBuff(void);

#endif /* STRING_GRAPHICS_H_ */
