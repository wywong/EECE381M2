load('theta.mat');
load('test.mat');

char_count=26;

count=zeros(char_count+1, 1);

pred = predict(Theta1, Theta2, XTest);
fprintf('\nTraining Set Accuracy (Test Data): %f\n', mean(double(pred == yTest)) * 100);

for ii=1:size(pred,1)
  if pred(ii) == yTest(ii)
    count(yTest(ii)) += 1;
  else
    count(char_count+1) += 1;
  end
end


bar(count);
set(gca, 'xtick', 1:27);
set(gca, 'XTickLabel', 'A|B|C|D|E|F|G|H|I|J|K|L|M|N|O|P|Q|R|S|T|U|V|W|X|Y|Z|?');
pause;
