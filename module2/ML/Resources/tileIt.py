import Image
import string
import glob
import re

images = list(glob.glob('Training_Fonts/*.bmp'))
white = 255

let = list(string.ascii_uppercase)

fonts = 0

while images:
  im = Image.open(images.pop(0)).convert('L')

  pixels = list(im.getdata())
  pixels = [0 if x == 0 else white for x in pixels]
  width, height = im.size
  pixels = [pixels[i * width:(i + 1) * width] for i in xrange(height)]

  bars = [] # a list of vertical bars


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

#  print bars

  leftx = 0
  for l in let:
    if(l == 'Z'):
      rightx = width
    else:
      rightx = bars.pop(0)
#    print (leftx, 0, rightx, height)
    im.crop((leftx, 1, rightx, height-1)).save('tiles/{0}{1}.bmp'.format(l, fonts))
    leftx = rightx + 1

  fonts += 1

tilt = list(glob.glob('tiles/*.bmp'))
angle = 2
while tilt:
  fpath = tilt.pop(0)

  im = Image.open(fpath).convert('RGBA')
  white_bg = Image.new('RGBA', im.size, (255,)*4)
  rot = im.rotate(angle)
  combined = Image.composite(rot, white_bg, rot)

  word = re.sub('tiles/|.bmp', '', fpath)

  combined.convert('L').save('tiles/{0}.bmp'.format(word+'.0'))

  rot = im.rotate(-2*angle)
  combined = Image.composite(rot, white_bg, rot)
  combined.convert('L').save('tiles/{0}.bmp'.format(word+'.1'))

tiles = list(glob.glob('tiles/*.bmp'))

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
    left = vbars.pop(0)
    while vbars:
      if (vbars[0] - left) != 1:
        right = vbars.pop(0)
        break
      else:
        left = vbars.pop(0)

  if(hbars):
    top = hbars.pop(0)
    while hbars:
      if (hbars[0] - top) != 1:
        bottom = hbars.pop(0)
        break
      else:
        top = hbars.pop(0)

  # print left, right
  # print top, bottom

  word = re.sub('tiles/|.bmp', '', fpath) + '.9'
  im.crop((left, top, right, bottom)).save('tiles/{0}.bmp'.format(word))
