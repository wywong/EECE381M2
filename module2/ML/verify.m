load('nnTrain/train.mat');
load('nnTrain/test.mat');

% Randomly select 100 data points to display
sel = randperm(size(XTrain, 1));
sel = sel(1:100);

displayData(XTrain(sel, :));

pause;

% Randomly select 100 data points to display
sel = randperm(size(XTest, 1));
sel = sel(1:100);

displayData(XTest(sel, :));
pause;
