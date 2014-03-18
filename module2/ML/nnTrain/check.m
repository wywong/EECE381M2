load('theta.mat');

letters = ['A' 'B' 'C' 'D' 'E' 'F' 'G' 'H' 'I' 'J' 'K' 'L' 'M' 'N' 'O'
          'P' 'Q' 'R' 'S' 'T' 'U' 'V' 'W' 'X' 'Y' 'Z']';

imageFiles = dir('photo/*.bmp');

for ii = imageFiles'
  X = imread(strcat('photo/', ii.name));
  X = reshape(X, 1, prod(size(X)));
  bSize = sizeof(X(1,1));
  X = double(X) / (2^(8*bSize) - 1);
  fprintf('%s maps to %s\n', ii.name, letters(predict(Theta1, Theta2, X)))
end
