#!/bin/bash

rm -rf tiles;
mkdir tiles;

python tileIt.py;

mogrify -resize 32x32! -format bmp tiles/*;

octave prepData.m;
octave verify.m;
