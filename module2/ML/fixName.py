import os
import glob
import re
import string

images = list(glob.glob('upper/*.bmp'))
images.sort()

charsPerFont = 26

numFonts = 7

images = [images[z*charsPerFont:(z+1) * charsPerFont] for z in range(0, numFonts)]

for ii in range(0, len(images)):
    print ii
    print images[ii]
    for jj in range(0, charsPerFont):
        fname = images[ii][jj]
        print fname
        os.rename(fname, re.sub('\d+', '{0}{1}'.format(string.uppercase[jj], ii), fname))
