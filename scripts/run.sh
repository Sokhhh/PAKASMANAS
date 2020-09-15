#!/bin/bash
# Don't forget to "chmod ugo+x ./gol.sh"

cd ..
java -cp "./bin;./src/resources" pacman.Pacman
cd scripts