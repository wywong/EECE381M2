img = imread('tiles/A0.0.bmp');

img = img(:,:,1);

[m,n] = size(img)

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

C_row = [C_L C_R C_T C_B] / 12
