#!/bin/bash

rm -rf {a..z};
rm -rf tiles;
mkdir tiles;

python tileIt.py;

for i in {a..z}
do
  mkdir -p $i;
  mv tiles/${i}* ${i}/
done
