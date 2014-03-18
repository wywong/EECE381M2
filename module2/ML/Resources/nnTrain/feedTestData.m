load('test.mat');
load('theta.mat');
pred = predict(Theta1, Theta2, XTest(1:10,:));
csvwrite('Xtest.csv', XTest(1:10, :));
csvwrite('ytest.csv', yTest(1:10));
csvwrite('pred.csv', pred);
