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
        stem = 'tmp/{0}{1}'.format(l, fonts)
        tile = im.crop((leftx, 1, rightx, height-1))
        tile.save(stem + '.0.bmp')
        leftx = rightx + 1

        # generate slightly tilted images
        tile = tile.convert('RGBA')
        white_bg = Image.new('RGBA', tile.size, (255,)*4)

        # left tilt
        angle = random.uniform(0.5, 1)
        rot = tile.rotate(angle)
        combined = Image.composite(rot, white_bg, rot)
        combined.convert('L').save(stem + '.1.bmp')
        rot = tile.rotate(-angle)

        # right tilt
        angle = random.uniform(0.5, 2.5)
        rot = tile.rotate(-angle)
        combined = Image.composite(rot, white_bg, rot)
        combined.convert('L').save(stem + '.2.bmp')
        rot = tile.rotate(angle)

    fonts += 1



#
#    angle = random.uniform(1.5, 3)
#    rot = im.rotate(angle)
#    combined = Image.composite(rot, white_bg, rot)
#    combined.convert('L').save('tmp/{0}.bmp'.format(word+'.2'))
#    rot = im.rotate(-angle)
#
#    angle = random.uniform(1.5, 3)
#    rot = im.rotate(-angle)
#    combined = Image.composite(rot, white_bg, rot)
#    combined.convert('L').save('tmp/{0}.bmp'.format(word+'.3'))
#    rot = im.rotate(angle)

if sys.argv[1] == 'strip':
    os.system("python stripIt.py")
