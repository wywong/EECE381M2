% get the filenames of the bitmap files in the tiles folder
imageFiles = dir('tiles/*.bmp');

X = [];
y = [];

numFonts = 7;
tilesPerFont = 10;
count = 0;

% 1 to 26 represents A to Z
f = 1;

for ii = imageFiles'
%  disp(strcat('tiles/', ii.name));
  if count == tilesPerFont*numFonts
    count = 0;
    f = f + 1;
  end

  count = count + 1;

  y = [y; f];

  t = imread(strcat('tiles/', ii.name));
  t = t(:,:,1);
  t = reshape(t, 1, prod(size(t)));
  bSize = sizeof(t(1,1));
  t = double(t) / (2^(8*bSize) - 1);
  X = [X; t];
end

save('nnTrain/rawdata.mat', 'X', 'y');

% randomize the training set
shuffle = [y X];
shuffle = shuffle(randperm(size(shuffle, 1)),:);

y = shuffle(:, 1);
X = shuffle(:, 2:end);

% 80% of data is used for training the Neural Network
yTrain = y(1:round(0.8*size(y,1)),:);
XTrain = X(1:round(0.8*size(X,1)),:);

% 20% is used for testing purposes
yTest = y(round(0.8*size(y,1)):end,:);
XTest = X(round(0.8*size(X,1)):end,:);

% save the data
save('nnTrain/train.mat', 'XTrain', 'yTrain');
save('nnTrain/test.mat', 'XTest', 'yTest');
