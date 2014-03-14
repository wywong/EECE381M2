import Image
import string
import glob

images = list(glob.glob('fonts/*.bmp'))
white = (255, 255, 255, 255)

let = list(string.ascii_lowercase)

fonts = 0

while images:
  im = Image.open(images.pop(0))

  pixels = list(im.getdata())
  width, height = im.size
  pixels = [pixels[i * width:(i + 1) * width] for i in xrange(height)]

  bars = [] # a list of vertical bars


  for y in range(0, width):
    vertLine = False
    for x in range(0, height):
      if(pixels[x][y] == white):
        vertLine = False
        break
      else:
        vertLine = True
    if(vertLine):
      bars.append(y)

  print bars

  leftx = 0
  for l in let:
    if(l == 'z'):
      rightx = width
    else:
      rightx = bars.pop(0)
    print (leftx, 0, rightx, height)
    im.crop((leftx, 0, rightx, height)).save('tiles/{0}{1}.bmp'.format(l, fonts))
    leftx = rightx + 1

  im.close()



