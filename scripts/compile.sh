#!/bin/bash
# Don't forget to "chmod ugo+x ./gol.sh"

mkdir -p "../bin"
# Compile source
javac -d ../bin -cp "../src/java" ../src/java/pacman/Pacman.java

