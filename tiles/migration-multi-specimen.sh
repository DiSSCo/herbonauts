#!/bin/bash

TILES_DIR=~/tmp/herbonautes-tiles-2

cd $TILES_DIR

for DIR in */*/*; do
  echo "$TILES_DIR/$DIR"
  cd $TILES_DIR/$DIR
  mkdir -p "$TILES_DIR/$DIR/1"
  mv *.jpg 1/
  ln -s 1/original.jpg original.jpg
  ln -s 1/crop.jpg crop.jpg
  ln -s 1/tile_0_0_0.jpg.jpg tile_0_0_0.jpg.jpg
done



