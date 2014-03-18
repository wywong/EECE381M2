load('theta.mat');

%  You can now "visualize" what the neural network is learning by
%  displaying the hidden units to see what features they are capturing in
%  the data.

fprintf('\nVisualizing Neural Network... \n')

displayData(Theta1(:, 2:end));
