import Image
import string
import glob
import re
import random

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
        im.crop((leftx, 1, rightx, height-1)).save('tmp/{0}{1}.bmp'.format(l, fonts))
        leftx = rightx + 1

    fonts += 1

# get list of filenames for all the character bitmaps
tiles = list(glob.glob('tmp/*.bmp'))

# strip excess whitespace surrounding each character
while tiles:
    fpath = tiles.pop(0)
    im = Image.open(fpath).convert('L')
    pixels = list(im.getdata())
    width, height = im.size
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

    word = re.sub('tmp/|.bmp', '', fpath)
    im.crop((left, top, right, bottom)).save('tiles/{0}.bmp'.format(word))

# generate slightly tilted images
tilt = list(glob.glob('tiles/*.bmp'))
while tilt:
    fpath = tilt.pop(0)
    angle = random.uniform(0.5, 3)

    im = Image.open(fpath).convert('RGBA')
    white_bg = Image.new('RGBA', im.size, (255,)*4)
    rot = im.rotate(angle)
    combined = Image.composite(rot, white_bg, rot)

    word = re.sub('tiles/|.bmp', '', fpath)

    combined.convert('L').save('tiles/{0}.bmp'.format(word+'.0'))
    rot = im.rotate(-angle)

    angle = random.uniform(0.5, 3)
    rot = im.rotate(-angle)
    combined = Image.composite(rot, white_bg, rot)
    combined.convert('L').save('tiles/{0}.bmp'.format(word+'.1'))
