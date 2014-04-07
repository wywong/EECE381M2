import os
import Image
import glob
images = list(glob.glob('real/*.bmp'))

while images:
    todelete = True
    fpath = images.pop(0)
    im = Image.open(fpath).convert('L')
    pixels = list(im.getdata())

    for pp in pixels:
        if pp != 255:
            todelete = False
            break

    if todelete:
        os.remove(fpath)
        print fpath
