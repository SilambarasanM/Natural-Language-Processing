#Text Summarization using Lexical Chains

##Structure:

<LexicalChains>
	- build.xml
	- properties.xml
	- sample-input.txt
	- sample-output.txt
	- ReadMe.txt
	- LexicalChains.pdf
	- <src>
		- wordnet
			- LexicalChains.java
			- WordNetHelper.java
	- <bin>
		- wordnet
			- LexicalChains.class
			- WordNetHelper.class
	- <dat>
		- Contains all the dictionary files for Wordnet.
	- <lib>
		- jwnl.jar
		- commons-logging-1.2.jar
		- stanford-postagger.jar

##Description:

build.xml			--> Ant build file exported from Eclipse IDE. This can be used to create a 'New Project' using 'Java Project from existing Ant Buildfile' option.

properties.xml		--> Line 40 of this file need to be updated to the actual path of the wordnet dictionary files that are in this project's <dat> directory. Without doing this, the
						project may not function as expected. The value for the dictionary_path need to updated to the actual location of the dictionary files.
						Line#40: <param name="dictionary_path" value="path_to_dict"/>

sample-input.txt	--> A sample input for the Lexical Chain building program

sample-output.txt	--> A sample output from the program for the text in the sample-input.txt file.

ReadMe.txt			--> This very same file. (Self-pointer)

LexicalChains.pdf	--> Results and Evaluation of the LexicalChains project with Automatic Text Summarization implementation.

LexicalChains.java	--> Implementation of the LexicalChains Project that contains the main function.

WordNetHelper.java	--> Helper Class for using the Wordnet dictionary.

jwnl.jar			--> Java WordNet Library files for acceesing Wordnet dictionary files.

commons-logging-1.2.jar	--> Apache Commons-logging java library files for jwnl libraries.

stanford-postagger.jar	--> Java Libraries for POS Tagger implementation for the initial filtering noun from the given text.


##JAVA Version Used:
java version "1.8.0_60"
Java(TM) SE Runtime Environment (build 1.8.0_60-b27)
Java Hotspot(TM) 64-Bit Server VM (build 25.60-b23, mixed mode)

##Notes for Execution:

The input file name along with the complete path need to be entered when the program prompts for the filename. LexicalChains.java file contains the main thread for the execution of this project.
