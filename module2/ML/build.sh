#!/bin/bash

# remove old tiles and remake the directories
rm -rf tiles;
mkdir tiles;

# execute tileIt python script to create character tiles from the training fonts
python tileIt.py;

# resize and pad all the tiles to the input size
mogrify -background white -gravity center -resize 12x12 -extent 12x12 -format bmp tiles/*;

# prepare the data to be used for training and testing
# train.mat and test.mat are generated
octave prepData.m;

# verify that train.mat and test.mat were saved correctly
# displays randomly selected characters in a figure
#octave verify.m;
