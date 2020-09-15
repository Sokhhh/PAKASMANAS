-------------------------------- Compile and run -----------------------------------------
In order to compile and run this code, several scripts are provided. Access to the
folder scripts/ and run the script by:

# On windows systems:
compile.cmd
compile_tests.cmd
javadoc.cmd
run.cmd

# On Linux/macOs systems:
bash compile.sh
bash compile_tests.sh
bash javadoc.sh
bash run.sh

These scripts will compile to code under src/ and generate *.class files under
the directory bin/, with java docs under docs/javadoc/ folder, and tests file under
bin_tests/ folder

Javadocs will be at docs/javadoc

The minimum java edition required for this application is JDK 1.8

--------------------------------- Development environment --------------------------------
The application is developed and tested under Microsoft Windows 10 Pro 2004 (OS Build
19041.329).
It has also been tested under Ubuntu 18.04.4 LTS (dual-booting), and Microsoft Windows 10
Education 1903 (OS Build 18362.900).

------------------------------------- Input ----------------------------------------------
If you want to load a custom maze, the input file of this application should be a *.txt
file with the specified grammar:

'P': start location of a pacman
'G': start location of a ghost
' ': empty space
'.': food
'0' or 'o': pellet
'+' or '%': wall
';': comment out a line

A sample input file and a preview of what the maze will look like is provided in data/
directory.

----------------------------------- Coding conventions -----------------------------------
The source code in this application uses Google checkstyle with only two exceptions:
indentation.
Indentation in the source codes are following Sun's Java Checkstyle (4-space indentation):
https://checkstyle.sourceforge.io/sun_style.html

abbreviation as word in names:
My coding convention allows up to 4 capitalized letter in the names (meant to fit the
abbreviation "HTML")


--------------------------- Additional directories and files -----------------------------

-- Directory bin/ contains all compiled *.class files.
-- Directory data/ contains sample input files


-------------------------------------- Additional Notes ----------------------------------
If the application is run under a command prompt, a warning message may be shown:

WARNING: Could not open/create prefs root node Software\JavaSoft\Prefs at root 0x80000002. Windows RegCreateKeyEx(...) returned error code 5.

This is a bug with Java itself and Windows. Please ignore it and it cause no harm to the
system and to the application.