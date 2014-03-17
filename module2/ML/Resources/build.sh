#!/bin/bash

rm -rf tiles;
rm -rf tmp;
mkdir tiles;
mkdir tmp;

python tileIt.py;

mogrify -background white -gravity center -extent 20x20 -format bmp tiles/*;

octave prepData.m;
octave verify.m;
rm -rf tmp;
