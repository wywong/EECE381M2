#!/bin/bash

rm -rf tiles;
mkdir tiles;

python tileIt.py;

mogrify -resize 20x20! -format bmp tiles/*;

#for i in {a..z}
#do
#  mkdir -p tiles/$i;
#  mv tiles/${i}*.bmp tiles/${i}/
#done
