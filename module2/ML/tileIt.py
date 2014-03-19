import Image
import glob
import os
import random
import re
import string
import sys

# get the list of training fonts to be parsed
images = list(glob.glob('Training_Fonts/*.bmp'))
white = 255

let = list(string.ascii_uppercase)

fonts = 0

rawTiles = []

# cut up font images into tiles
while images:
    im = Image.open(images.pop(0)).convert('L')

    pixels = list(im.getdata())
    pixels = [0 if x <= 240 else white for x in pixels]
    width, height = im.size
    pixels = [pixels[i * width:(i + 1) * width] for i in xrange(height)]

    bars = [] # a list of vertical bars


# scan for the vertical bars
    for y in range(0, width):
        vertLine = False
        for x in range(1, height-1):
            if(pixels[x][y] == white):
                vertLine = False
                break
            else:
                vertLine = True
        if(vertLine):
            bars.append(y)

#    print bars

# using the bars as a reference crop the image into 26 tiles
    leftx = 0
    for l in let:
        if(l == 'Z'):
            rightx = width
        else:
            rightx = bars.pop(0)
#        print (leftx, 0, rightx, height)
        tname = '{0}{1}'.format(l, fonts)
        tile = im.crop((leftx, 1, rightx, height-1))
        rawTiles.append((tile.copy(), tname + '.0'))
        leftx = rightx + 1

        # generate slightly tilted images
        tile = tile.convert('RGBA')
        white_bg = Image.new('RGBA', tile.size, (255,)*4)

        # left tilt
        angle = random.uniform(0.5, 1)
        rot = tile.rotate(angle, Image.BILINEAR, expand=0)
        combined = Image.composite(rot, white_bg, rot)
        rawTiles.append((combined.convert('L').copy(), tname + '.1'))
        rot = tile.rotate(-angle, Image.BILINEAR, expand=0)

        # right tilt
        angle = random.uniform(0.5, 2.5)
        rot = tile.rotate(-angle, Image.BILINEAR, expand=0)
        combined = Image.composite(rot, white_bg, rot)
        rawTiles.append((combined.convert('L').copy(), tname + '.2'))
        rot = tile.rotate(angle, Image.BILINEAR, expand=0)

    fonts += 1

strippedTiles = []
# strip excess whitespace surrounding each character
while rawTiles:
    tile, tname = rawTiles.pop(0)
    pixels = list(tile.getdata())
    width, height = tile.size
    pixels = [pixels[i * width:(i + 1) * width] for i in xrange(height)]

    vbars = []
    hbars = []

    for y in range(0, width):
        vertLine = False
        for x in range(0, height):
            if(pixels[x][y] <= white-15):
                vertLine = False
                break
            else:
                vertLine = True
        if(vertLine):
            vbars.append(y)

    for x in range(0, height):
        horLine = False
        for y in range(0, width):
            if(pixels[x][y] <= white-15):
                horLine = False
                break
            else:
                horLine = True
        if(horLine):
            hbars.append(x)

    left = 0
    right = width
    top = 0
    bottom = height

    # print vbars
    # print hbars

    if(vbars):
        if vbars[0] == 0:
            left = vbars.pop(0)
            while vbars:
                if (vbars[0] - left) != 1:
                    right = vbars.pop(0)
                    break
                else:
                    left = vbars.pop(0)
        else:
            right = vbars.pop(0)

    if(hbars):
        if hbars[0] == 0:
            top = hbars.pop(0)
            while hbars:
                if (hbars[0] - top) != 1:
                    bottom = hbars.pop(0)
                    break
                else:
                    top = hbars.pop(0)
        else:
            bottom = hbars.pop(0)

    # print left, right
    # print top, bottom

    tile.crop((left, top, right, bottom)).save('tiles/{0}.bmp'.format(tname))
