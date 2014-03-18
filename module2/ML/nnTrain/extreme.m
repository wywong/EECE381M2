images = dir('photo/*.bmp');

for ii=images'
  x = imread(strcat('photo/',ii.name));
  x = x > 100;
  imwrite(x, ii.name);
end
