@ECHO OFF

REM should be run under scripts/ directory

if not exist "../bin" mkdir "../bin"
REM  Compile source
javac -d ../bin -cp "../src/java" ../src/java/pacman/Pacman.java
