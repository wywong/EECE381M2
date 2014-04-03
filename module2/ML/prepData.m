% get the filenames of the bitmap files in the tiles folder
imageFiles = dir('tiles/*.bmp');

X = [];
y = [];

numFonts = 77;
tilesPerFont = 3;
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
  bSize = sizeof(t(1,1));
%  [m, n] = size(t);
%  s = zeros(m/2, n/2);
%  for jj = 1:m/2
%    for kk = 1:n/2
%      s(jj, kk) = mean(mean(t(jj:jj+1, kk:kk+1) > 0.94));
%    end
%  end
%  s = reshape(s, 1, prod(size(s)));
%  bSize = sizeof(s(1,1));
%  s = double(s) / (2^(8*bSize) - 1);
  t = reshape(t, 1, prod(size(t)));
  t = double(t) / (2^(8*bSize) - 1);
  X = [X; t];
end

C = [];
for kk = imageFiles'
  img = imread(strcat('tiles/', kk.name));
  img = img(:,:,1);

  [m,n] = size(img);

  img = img > 0.94;

  C_L = zeros(1, n);
  C_R = zeros(1, n);
  C_T = zeros(1, n);
  C_B = zeros(1, n);

  for ii=1:m
    for jj=1:n
      if img(ii, jj) == 1
        C_L(1, ii) = C_L(1, ii) + 1;
      else
        break;
      end
    end
    for jj=n:-1:1
      if img(ii, jj) == 1
        C_R(1, ii) = C_R(1, ii) + 1;
      else
        break;
      end
    end
  end

  for ii=1:n
    for jj=1:m
      if img(ii, jj) == 1
        C_T(1, ii) = C_T(1, ii) + 1;
      else
        break;
      end
    end
    for jj=m:-1:1
      if img(ii, jj) == 1
        C_B(1, ii) = C_B(1, ii) + 1;
      else
        break;
      end
    end
  end

  C_T = C_T / n;
  C_B = C_B / n;
  C_L = C_L / m;
  C_R = C_R / m;

  C_row = [C_L C_R C_T C_B];
  C = [C; C_row];
end

X = [X C];

save('nnTrain/rawdata.mat', 'X', 'y', 'C');

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
