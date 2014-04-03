load('theta.mat');

letters = ['A' 'B' 'C' 'D' 'E' 'F' 'G' 'H' 'I' 'J' 'K' 'L' 'M' 'N' 'O'
          'P' 'Q' 'R' 'S' 'T' 'U' 'V' 'W' 'X' 'Y' 'Z']';

imageFiles = dir('photo/*.bmp');

for kk = imageFiles'
  X = imread(strcat('photo/', kk.name));
  X = X(:, :, 1);
  X = reshape(X, 1, prod(size(X)));
  bSize = sizeof(X(1,1));
  X = double(X) / (2^(8*bSize) - 1);

  img = imread(strcat('photo/', kk.name));
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
  X = [X C_row];

  fprintf('%s maps to %s\n', kk.name, letters(predict(Theta1, Theta2, X)))
end
