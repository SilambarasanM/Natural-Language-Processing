#Earley Parser
Paul Chase

	This assignment may be run from the bin directory, and has two modes:
	interactive and bash.
	INTERACTIVE: java earleyParser <grammar>
	BATCH: java earleyParser <grammar> <sentence_file>

	Batch will output to command line, so pipe it to a file like so:
	java earleyParser <grammer> <sentence_file> > parses.txt

	Be sure to comment out the printTable command; this is only there as
	an aide, final code should print actual parses only.
	
	The grammars are in the /grammars directory.
##Author Note:
	The implementation is a direct Earley parser, with some optimizations
based on doing smaller checks to try and improve performance on small grammars.
Basically I try to limit the number of times we need to search the entire
grammar; speed improved threefold as a result, but it's still not that great;
string compares are SLOW.  The main implementation is in the Grammar.java file,
including all the functions required to parse a sentence.  I took out support
for parsing sgml files; input instead one sentence per line, nonterminals
separated by spaces.

	This is one of the first things I wrote in java; it's somewhat poorly
done, and could use much improvement.  I fixed some glaring errors, but many
still abound; as such, I'd suggest you start early and feel free to ask
questions or come to my office hours (mon & wed 3-5) or email me:
chaspau@iit.edu.  There will also be a discussion section on Blackboard; I'll
try to check it at least once a day.
