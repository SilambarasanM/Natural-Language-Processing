#Project: Part of speech tagger

The assignment is in Java.
http://java.sun.com/
The JDK is needed to compile and run this assignment.

To build, you should have ant installed.
http://ant.apache.org/
It might also be convenient to have make.
After making your changes,


The file src/cs481/postag/POSTag.java
contains a Majority Tag POS tagger.
This should be the only file that is modified to create a bigram HMM POS tagger.

Training and testing data is provided in dat.


To compile:
~> ant

Or, if you also have make:
~> make

(Your IDE may take care of that step automatically.)

##To run:
~> cd bin
~/bin> java -Xmx512m cs481.postag.POSTag ../dat/train.xml ../dat/test_1.xml out.xml

Note that -Xmx512m specifies how much RAM to use.  Set it to an appropriate value for your machine.

To find the accuracy of your tagging:
~/bin> java -Xmx512m cs481.postag.POSDiff out.xml ../dat/test_1.xml > log

In the default configuration, that should return:
*** Similarity = 27308 / 28432 = 96.04670793472144 %


