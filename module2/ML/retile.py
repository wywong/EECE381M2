import Image
import glob
import os
import random
import re
import string
import sys

# get the list of training fonts to be parsed
images = list(glob.glob('upper/*.bmp'))
white = 255

let = list(string.ascii_uppercase)

rawTiles = []

# cut up font images into tiles
while images:
    iname = images.pop(0)
    tile = Image.open(iname).convert('RGBA')
    tname = '{0}'.format(re.sub('upper/|.bmp', '', iname))
    rawTiles.append((tile.copy().convert('L'), tname + '.0'))

    # generate slightly tilted images
    white_bg = Image.new('RGBA', tile.size, (255,)*4)

    for zz in range(1, 7):
        # left tilt
        angle = random.uniform(-5, 5)
        rot = tile.rotate(angle, Image.BILINEAR, expand=0)
        combined = Image.composite(rot, white_bg, rot)
        rawTiles.append((combined.convert('L').copy(), tname + '.{0}'.format(zz)))
        rot = tile.rotate(-angle, Image.BILINEAR, expand=0)

while rawTiles:
    tile, tname = rawTiles.pop(0)

    tile.save('tiles/{0}.bmp'.format(tname))
