#!/bin/bash
if [ -d "$1" ]
then
	javac -nowarn -d "../bin/" ../src/mainClass.java ../src/LineProcess.java ../src/ReadXML.java ../src/PageScore.java ../src/PrimaryCreator.java ../src/Searcher.java ../src/Stemmer.java ../src/StopWords.java

		cd "../bin/"
		java mainClass "$1"
		cd "../toRun"
else
	echo "$1 is not a directory"
fi

