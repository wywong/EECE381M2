import Image
import glob
import re
import string

white = 255
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
