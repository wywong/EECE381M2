#!/bin/bash

rm -rf tiles;
mkdir tiles;

python tileIt.py;

mogrify -resize 20x20! -format bmp tiles/*;

octave prepData.m;
octave verify.m;
