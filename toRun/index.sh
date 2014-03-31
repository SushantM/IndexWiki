#!/bin/bash
javac -nowarn -d "../bin/" ../src/mainClass.java ../src/LineProcess.java ../src/ReadXML.java ../src/PageScore.java ../src/PrimaryCreator.java ../src/Searcher.java ../src/Stemmer.java ../src/StopWords.java
if [ -f "$1" ]
then
	if [ -d "$2" ]
	then
		cd "../bin/"
		java mainClass "$1" "$2"
		cd "../toRun"
	else
		echo "$2 not a directory"
	fi
else
	echo "$1 Not a file"
fi
