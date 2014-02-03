**************IRE MINI PROJECT PHASE1 ******************

This is a readme file about the Phase1 of Wikipedia Search Engine.

How to run:
1) Source files are placed in "src" folder.
2) To create index, run index.sh in "bin" folder with two arguments 1) Path of XML corpus 2) Output directory
3) To search, run query.sh in "bin" folder with Output directory as argument

Approach:
1) First ReadXML class reads the XML corpus though SAX Parser and gives the lines for processing.
2) The LineProcess class parses the Line into words.
3) These words are checked for Stopword removal by StopWord class.
4) Then they are stemmed with help of Stemmer class.
5) Finally , the word and its pageId in which it is occurring are stored into TreeMap.
6) Once, TreeMap gets 4096 records, the content of it are dumped to the disk using the fuction dumpOnDisk
7) Existing index file is merged with TreeMap being dumped.
8) After first level of Index( indexDataFile ), secondary index (secondaryIndex) is prepared which stores the starting addresses of each alphabet. eg. Start address of 'a', 'b', ...'z'.
9) Once a query is submitted to Searcher class, it is parsed , stemmed and given to binary search for searching.
10) Results along with PageIds are displayed.


