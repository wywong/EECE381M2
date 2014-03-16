%% Initialization
clear ; close all; clc

%% Setup the parameters you will use for this exercise
input_layer_size  = 400;  % 20x20 Input Images of Digits
num_labels = 26;          % A to Z , 1 to 26

load('train.mat');
load('test.mat');

A = [];
B = [];

for ii=24:12:120
  hidden_layer_size = ii;   % hidden units
%% ================ Part 6: Initializing Pameters ================
%  In this part of the exercise, you will be starting to implment a two
%  layer neural network that classifies digits. You will start by
%  implementing a function to initialize the weights of the neural network
%  (randInitializeWeights.m)

fprintf('\nInitializing Neural Network Parameters ...\n')

initial_Theta1 = randInitializeWeights(input_layer_size, hidden_layer_size);
initial_Theta2 = randInitializeWeights(hidden_layer_size, num_labels);

% Unroll parameters
initial_nn_params = [initial_Theta1(:) ; initial_Theta2(:)];


%% =================== Part 8: Training NN ===================
%  You have now implemented all the code necessary to train a neural
%  network. To train your neural network, we will now use "fmincg", which
%  is a function which works similarly to "fminunc". Recall that these
%  advanced optimizers are able to train our cost functions efficiently as
%  long as we provide them with the gradient computations.
%
fprintf('\nTraining Neural Network... with %i hidden units\n', hidden_layer_size)

%  After you have completed the assignment, change the MaxIter to a larger
%  value to see how more training helps.
options = optimset('MaxIter', 125);

%  You should also try different values of lambda
lambda = 1.4;

% Create "short hand" for the cost function to be minimized
costFunction = @(p) nnCostFunction(p, ...
                                   input_layer_size, ...
                                   hidden_layer_size, ...
                                   num_labels, XTrain, yTrain, lambda);
  % Now, costFunction is a function that takes in only one argument (the
  % neural network parameters)
  [nn_params, cost] = fmincg(costFunction, initial_nn_params, options);

  % Obtain Theta1 and Theta2 back from nn_params
  Theta1 = reshape(nn_params(1:hidden_layer_size * (input_layer_size + 1)), ...
                   hidden_layer_size, (input_layer_size + 1));

  Theta2 = reshape(nn_params((1 + (hidden_layer_size * (input_layer_size + 1))):end), ...
                   num_labels, (hidden_layer_size + 1));

  %% ================= Part 10: Implement Predict =================
  %  After training the neural network, we would like to use it to predict
  %  the labels. You will now implement the "predict" function to use the
  %  neural network to predict the labels of the training set. This lets
  %  you compute the training set accuracy.

  pred = predict(Theta1, Theta2, XTest);
  [Jtest, grad] = nnCostFunction(nn_params, input_layer_size,
      hidden_layer_size, num_labels, XTest, yTest, 0);

  A = [A ii];
%  B = [B mean(double(pred == y))];
  B = [B Jtest];
end

plot(A, B);
save('hidden.mat', 'A', 'B');
pause;
