#!/bin/bash
# Don't forget to "chmod ugo+x ./gol.sh"

cd ..
#  Javadoc
javadoc -locale en_US -version -private -encoding UTF-8 -d "./docs/javadoc" -Xdoclint:none -tag requires:a:"Requires:" -tag modifies:a:"Modifies:" -tag effects:a:"Effects:" -cp "./lib/*;./src/java" -sourcepath "./src/java" -subpackages .

cd scripts