load('nnTrain/train.mat');
load('nnTrain/test.mat');

pixels = 12*12;

% Randomly select 100 data points to display
sel = randperm(size(XTrain, 1));
sel = sel(1:100);

displayData(XTrain(sel, 1:pixels));

pause;

% Randomly select 100 data points to display
sel = randperm(size(XTest, 1));
sel = sel(1:100);

displayData(XTest(sel, 1:pixels));
pause;
