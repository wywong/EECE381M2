load('test.mat');
load('theta.mat');
pred = predict(Theta1, Theta2, XTest(1:10,:));
csvwrite('Xtest.csv', XTest(1:10, :));
csvwrite('ytest.csv', yTest(1:10));
csvwrite('pred.csv', pred);

load('rawdata.mat')
csvwrite('Xraw.csv', X(1:10, :));
csvwrite('yraw.csv', y(1:10));
pred = predict(Theta1, Theta2, X(1:10,:));
csvwrite('predraw.csv', pred);
