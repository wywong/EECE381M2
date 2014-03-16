load('theta.mat');
X = imread('Selection_001.bmp');
X = reshape(X, 1, prod(size(X)));
X = double(X)/255;
predict(Theta1, Theta2, X)
