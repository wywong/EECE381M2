import Image
import string
import glob

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



