#!/bin/bash

# remove old tiles and remake the directories
rm -rf tiles;
mkdir tiles;

# execute retile python script to create character tiles from the training fonts
python retile.py;

# prepare the data to be used for training and testing
# train.mat and test.mat are generated
octave prepRealData.m;

# verify that train.mat and test.mat were saved correctly
# displays randomly selected characters in a figure
#octave verify.m;

