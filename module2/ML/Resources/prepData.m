imageFiles = dir('tiles/*.bmp');

X = [];
y = [];

numFonts = 50;
count = 0;

% 1 to 26 represents A to Z
f = 1;

for ii = imageFiles'
%  disp(strcat('tiles/', ii.name));
  if count == 2*numFonts
    count = 0;
    f = f + 1;
  end

  count = count + 1;

  y = [y; f];

  t = imread(strcat('tiles/', ii.name));
  t = reshape(t, 1, prod(size(t)));
  t = double(t) / 255.0;
  X = [X; t];
end

shuffle = [y X];
shuffle = shuffle(randperm(size(shuffle, 1)),:);

y = shuffle(:, 1);
X = shuffle(:, 2:end);

yTrain = y(1:0.8*size(y,1),:);
XTrain = X(1:0.8*size(X,1),:);

yTest = y(0.8*size(y,1):end,:);
XTest = X(0.8*size(X,1):end,:);

save('nnTrain/train.mat', 'XTrain', 'yTrain');
save('nnTrain/test.mat', 'XTest', 'yTest');
