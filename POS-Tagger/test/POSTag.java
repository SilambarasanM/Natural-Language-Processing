package cs481.postag;

import cs481.token.*;
import cs481.util.*;

import java.io.*;
import java.util.*;

/**
 * Determines the part of speech tags based on Viterbi.
 *
 * <pre>
 * Typical use:
 * POSTag pt = new POSTag();
 * pt.train(training);
 * pt.tag(testing);
 * </pre>
 *
 * Run from the commandline.
 *
 * @author Sterling Stuart Stein
 * @author Shlomo Argamon
 */
public class POSTag
{
    /**
     * Special start tag
     */
    public static String StartTag = "*START*";
    
    /**
     * Small probability for when not found
     */
    public static float epsilon = -10000000f;
    
    /**
     * Array of all tags
     */
    protected String[] tags;
    
    /**
     * Probability of tags given specific words
     */
    protected HashMap pTagWord;
	
	//Good-Turing Implemetantion
    /**
     * Count of bigrams
     */
    protected HashMap cBigram;
	
	//Good-Turing Implemetantion
    /**
     * Probability of tags given previous tag
     */
    protected HashMap pGT;

	//Good-Turing Implemetantion
    /**
     * Probability of unseen tags
     */
    protected float pGT0;
	
	//Bigram Implemetantion
    /**
     * Probability of tags given previous tag
     */
    protected HashMap pTagTag;
    
    /**
     * Probability of individual tags (i.e., P(tag)
     */
    protected HashMap pTag;	

	//Bigram Implementation
    /**
     * Hashmap of all known tag pairs
     */
    protected HashMap allTagPairs;
    
    /**
     * Hashmap of all known words
     */
    protected HashMap allWords;	
    
    /**
     * Make an untrained part of speech tagger.
     */
    public POSTag()
    {
	pTagWord    = new HashMap();
	pTag        = new HashMap();
	allWords    = new HashMap();
	
	//Bigram Implementation
	pTagTag		= new HashMap();
	allTagPairs = new HashMap();
	
	//Good-Turing Implementation
	cBigram		= new HashMap();
	pGT			= new HashMap();
    
	}
    
    /**
     * Remove all training information.
     */
    public void clear()
    {
	pTag.clear();
	pTagWord.clear();
	allWords.clear();
	tags = null;
	
	//Bigram Implementation
	pTagTag.clear();
	allTagPairs.clear();
	
	//Good-Turing Implementation
	cBigram.clear();
	pGT.clear();
    }
    
    /**
     * Increment the count in a HashMap for t.
     *
     * @param h1 The HashMap to be modified
     * @param t  The key of the field to increment
     */
    protected void inc1(HashMap h1, String t)
    {
	if(h1.containsKey(t))
	    {
		int[] ip = (int[])h1.get(t);  //Used as int *
		ip[0]++;
	    }
	else
	    {
		int[] ip = new int[1];
		ip[0] = 1;
		h1.put(t, ip);
	    }
    }
    
    /**
     * Increment the count in a HashMap for [t1,t2].
     *
     * @param h2 The HashMap to be modified
     * @param t1 The 1st part of the key of the field to increment
     * @param t2 The 2nd part of the key of the field to increment
     */
    protected void inc2(HashMap h2, String t1, String t2)
    {
	//Have to use Vector because arrays aren't hashable
	Vector key = new Vector(2);
	key.setSize(2);
	key.set(0, t1);
	key.set(1, t2);
	
	if(h2.containsKey(key)) {
		int[] ip = (int[])h2.get(key);  //Used as int *
		ip[0]++;
	} else {
		int[] ip = new int[1];
		ip[0] = 1;
		h2.put(key, ip);
	    }
    }

	//Good-Turing Implementation
    /**
     * Increment the count in a HashMap for [Nn,count].
     *
     * @param h3 The HashMap to be modified
     * @param Nn The key of the field to increment
     */
    protected void inc3(HashMap h3, int n)
    {
	if(h3.containsKey(n))
	    {
		int[] ip = (int[])h3.get(n);  //Used as int *
		ip[0]++;
	    }
	else
	    {
		int[] ip = new int[1];
		ip[0] = 1;
		h3.put(n, ip);
	    }
    }
    
    /**
     * Train the part of speech tagger.
     *
     * @param training A vector of paragraphs which have tokens with the attribute &quot;pos&quot;.
     */
    public void train(Vector training)
    {
	int cTokens = 0;
	HashMap cWord    = new HashMap();
	HashMap cTag     = new HashMap();
	HashMap cTagWord = new HashMap();
	boolean[] bTrue = new boolean[1];
	bTrue[0] = true;
	
	//Bigram Implementation
	HashMap cTagTag  = new HashMap();
	
	clear();

	//Count word and tag occurrences
	for(Iterator i = training.iterator(); i.hasNext();) {
		Vector para = (Vector)i.next();
		
		for(Iterator j = para.iterator(); j.hasNext();) {
			Vector sent    = (Vector)j.next();
			String curtag  = StartTag;
			inc1(cTag, curtag);
			
			//Bigram Implementation
			String prevtag = StartTag;
				
			for(Iterator k = sent.iterator(); k.hasNext(); ) {
				Token tok = (Token)k.next();

				curtag = (String)tok.getAttrib("pos");
				//Delete the next line
				//System.out.println("Curtag: "+curtag);
				inc1(cTag, curtag);
				
				String name = tok.getName().toLowerCase();
				//Delete the next line
				//System.out.println("Name: "+name);
				inc1(cWord, name);
				allWords.put(name, bTrue);
				inc2(cTagWord, curtag, name);
				
				//Bigram Implementation
				inc2(cTagTag,prevtag,curtag);
				prevtag = curtag;
				cTokens++;
		    }
		}
	}
	
	//Good-Turing Implementation
	for(Iterator i = cTagTag.keySet().iterator(); i.hasNext();) {
	    Vector key = (Vector)i.next();
	    int[]  ip  = (int[])cTagTag.get(key);
	    inc3(cBigram, ip[0]);
	}
    
	//Delete next 10 lines
	System.out.println(">>>>>>>cWord<<<<<<<<");
	debugPrintHashStringInt(cWord);
	System.out.println(">>>>>>>cTag<<<<<<<<");
	debugPrintHashStringInt(cTag);
	System.out.println(">>>>>>>cTagWord<<<<<<<<");
	debugPrintHashInt(cTagWord);
	System.out.println(">>>>>>>cTagTag<<<<<<<<");
	debugPrintHashInt(cTagTag);
	System.out.println(">>>>>>>allWords<<<<<<<<");
	debugPrintHashStringBool(allWords);
	System.out.println(">>>>>>>cBigram<<<<<<<<");
	debugPrintHashIntInt(cBigram);
	//Find probabilities from counts
	for(Iterator i = cTag.keySet().iterator(); i.hasNext();) {
	    String key   = (String)i.next();
	    int[]  count = (int[])cTag.get(key);
			
	    pTag.put(key, new Float(Math.log(((float)count[0]) / (float)cTokens)));
	}
	
	for(Iterator i = cTagWord.keySet().iterator(); i.hasNext();) {
	    Vector key   = (Vector)i.next();
	    int[]  count = (int[])cTagWord.get(key);
	    int[]  total = (int[])cWord.get(key.get(1));
		
	    pTagWord.put(key, new Float(Math.log(((float)count[0]) / ((float)total[0]))));
	}
	
	//Bigram Implementation
	for(Iterator i = cTagTag.keySet().iterator(); i.hasNext();) {
	    Vector key   = (Vector)i.next();
	    int[]  count = (int[])cTagTag.get(key);
	    int[]  total = (int[])cTag.get(key.get(0));
		
	    pTagTag.put(key, new Float(Math.log(((float)count[0]) / ((float)total[0]))));
		
		
		allTagPairs.put(key,bTrue);
	}
	
	//Good-Turing Implementation
	for(Iterator i = cTagTag.keySet().iterator(); i.hasNext();) {
	    Vector key  = (Vector)i.next();
	    int[]  c	= (int[])cTagTag.get(key);
	    int[] nc	= (int[])cBigram.get(c[0]);
		int[] nc11	= (int[])cBigram.get(c[0]+1);
		float nc1	= (nc11 == null)? 0.0f : (float)nc11[0];
		Float cGT	= new Float(Math.log((((float)c[0] + 1.0f) * nc1) / ((float)nc[0]))); 
						
	    //pGT.put(key, new Float(cGT.floatValue()- Math.log((float)cTokens)));
		pGT.put(key, new Float(cGT.floatValue()- Math.log((float)cTagTag.size())));
		
	}
	
	//Good-Turing Implementation
	int[] c0   = (int[]) cBigram.get(1);
	pGT0 = (float)(c0[0]/cTagTag.size());
			
	
	//Make list of all possible tags
	tags = (String[])cTag.keySet().toArray(new String[0]);
	
	
	//Delete next loop
	System.out.println(">>>>>>>tags<<<<<<<<");
	for (String item : tags) {
    System.out.println(item);}
    }
    
    /**
     * Print out a HashMap<Vector,int[1]>.
     *
     * @param h The HashMap to be printed.
     */
    protected void debugPrintHashInt(HashMap h) {
	for(Iterator i = h.keySet().iterator(); i.hasNext();) {
	    Vector key = (Vector)i.next();
	    int[]  ip  = (int[])h.get(key);
	    
	    for(int j = 0; j < key.size(); j++) {
		System.out.print(", " + key.get(j));
	    }
	    
	    System.out.println(": " + ip[0]);
	}
    }
    
	//Good-Turing Implementation
	/**
     * Print out a HashMap<Vector,int[1]>.
     *
     * @param h The HashMap to be printed.
     */
    protected void debugPrintHashIntInt(HashMap h) {
	for(Iterator i = h.keySet().iterator(); i.hasNext();) {
	    int key = (int)i.next();
	    int[]  ip  = (int[])h.get(key);
	    
		System.out.println(", " + key+": " + ip[0]);
	  
	}
    }
	
    /**
     * Print out a HashMap<Vector,Float>.
     *
     * @param h The HashMap to be printed.
     */
    protected void debugPrintHashFloat(HashMap h) {
	for(Iterator i = h.keySet().iterator(); i.hasNext();) {
		Vector key = (Vector)i.next();
		float  f   = ((Float)h.get(key)).floatValue();
		
		for(int j = 0; j < key.size(); j++) {
			System.out.print(", " + key.get(j));
		    }
		
		System.out.println(": " + f);
	    }
    }

	/**
     * Print out a HashMap<Vector,Bool>.
     *
     * @param h The HashMap to be printed.
     */
    protected void debugPrintHashBool(HashMap h) {
	for(Iterator i = h.keySet().iterator(); i.hasNext();) {
		Vector key = (Vector)i.next();
		boolean[]  f  = (boolean[])h.get(key);
		
		for(int j = 0; j < key.size(); j++) {
			System.out.print(", " + key.get(j));
		    }
		
		System.out.println(": " + f[0]);
	    }
    }

	    /**
     * Print out a HashMap<Vector,Float>.
     *
     * @param h The HashMap to be printed.
     */
    protected void debugPrintHashStringFloat(HashMap h) {
	for(Iterator i = h.keySet().iterator(); i.hasNext();) {
		String key = (String)i.next();
		float  f   = ((Float)h.get(key)).floatValue();
			System.out.print(", " + key);
			System.out.println(": " + f);
	    }
    }
	
    protected void debugPrintHashKeys(HashMap h) {
	for(Iterator i = h.keySet().iterator(); i.hasNext();) {
	    String key = ((String)i.next());
	    System.out.println(": " + key);
	}
    }
    
	    /**
     * Print out a HashMap<String,int[1]>.
     *
     * @param h The HashMap to be printed.
     */
    protected void debugPrintHashStringInt(HashMap h) {
	for(Iterator i = h.keySet().iterator(); i.hasNext();) {
	    String key = (String)i.next();
	    int[]  ip  = (int[])h.get(key);
	    
	    
		System.out.print(", " + key);
	    
	    
	    System.out.println(": " + ip[0]);
	}
    }
    
		    /**
     * Print out a HashMap<String,boolean[1]>.
     *
     * @param h The HashMap to be printed.
     */
    protected void debugPrintHashStringBool(HashMap h) {
	for(Iterator i = h.keySet().iterator(); i.hasNext();) {
	    String key = (String)i.next();
	    boolean[]  ip  = (boolean[])h.get(key);
	    
	    
		System.out.print(", " + key);
	    
	    
	    System.out.println(": " + ip[0]);
	}
    }
    
	/**
     * Tags a sentence by setting the &quot;pos&quot; attribute in the Tokens.
     *
     * @param sent The sentence to be tagged.
     */
    public void tagSentence(Vector sent) {
	int len     = sent.size();
	if (len == 0) {
	    return;
	}
	
	int numtags = tags.length;
	
	Vector twkey = new Vector(2);
	twkey.setSize(2);
	
	//Probability of best path to word with tag
	float[][] pathprob = new float[len + 1][numtags]; 
	
	//  Edge to best path to word with tag
	int[][]   backedge = new int[len + 1][numtags];
	
	
	//For words in sentence
	for(int i = 0; i < pathprob.length - 1; i++) {
	    String word = ((Token)sent.get(i)).getName().toLowerCase();
	    twkey.set(1, word);

	    //Loop over tags for this word
	    for(int j = 0; j < numtags; j++) {
		String thistag = tags[j];
		Float tagProb1 = (Float)pTag.get(thistag);
		float tagProb = (tagProb1 == null) ? epsilon : tagProb1.floatValue();
		twkey.set(0, thistag);
		
		boolean[] knownWord = (boolean[])allWords.get(word);
		Float twp1 = (Float)pTagWord.get(twkey);
		float twp  = (((knownWord == null)||(knownWord[0] != true)) ?
			      tagProb : 
			      ((twp1 == null) ?
			       epsilon :
			       twp1.floatValue()));
		// In a unigram model, only the current probability matters
		pathprob[i][j]    = twp;

		// Now create the back link to the max prob tag at the previous stage
		// If we are at the second word or further
		if (i > 0) {
		    int   back = 0;
		    float max  = -100000000f;
		
		    //Loop over previous tags
		    for(int k = 0; k < numtags; k++) {
			String prevtag = tags[k];
		    
			// Probability for path->prevtag k + thistag j->word i
			float test = pathprob[i-1][k];
		    
			String prevword = ((Token)sent.get(i-1)).getName().toLowerCase();

			if (test > max) {
			    max     = test;
			    back    = k;
			}
		    }
		    backedge[i][j]    = back;
		}
	    }
	}

	//Trace back finding most probable path
	{
	    float max    = -100000000f;
	    int   prevtag = 0;
	    
	    //Find final tag
	    for(int i = 0; i < numtags; i++) {
		float test = pathprob[len-1][i];
		
		if(max < test) {
		    max       = test;
		    prevtag    = i;
		}
	    }
	    
	    //Follow back edges to start tag and set tags on words
	    for(int i = len-1; i >= 0; i--) {
		Token tok = (Token)sent.get(i);
		tok.putAttrib("pos", tags[prevtag]);
		prevtag = backedge[i][prevtag];

	    }
	}
	//Delete next loop
	for (int i =0; i<len; i++){
		System.out.print(sent.get(i)+"\t");
		for(int j =0; j<numtags; j++){
		System.out.print("\t\t"+tags[j]+": "+pathprob[i][j]+"|"+tags[backedge[i][j]]);}
	System.out.println();}
    }

	//Bigram Implementation
	/**
     * Tags a sentence by setting the &quot;pos&quot; attribute in the Tokens.
     *
     * @param sent The sentence to be tagged.
     */
    public void tagSentence1(Vector sent) {
	int len     = sent.size();
	if (len == 0) {
	    return;
	}
	
	int numtags = tags.length;
	
	Vector twkey = new Vector(2);
	twkey.setSize(2);
	
	//Bigram Implementation
	Vector ttkey = new Vector(2);
	ttkey.setSize(2);
	
	//Probability of best path to word with tag
	float[][] pathprob = new float[len + 1][numtags]; 
	
	//  Edge to best path to word with tag
	int[][]   backedge = new int[len + 1][numtags];
		
	//For words in sentence
	for(int i = 0; i < pathprob.length - 1; i++) {
	    String word = ((Token)sent.get(i)).getName().toLowerCase();
	    twkey.set(1, word);

	    //Loop over tags for this word
	    for(int j = 0; j < numtags; j++) {
		String thistag = tags[j];
		Float tagProb1 = (Float)pTag.get(thistag);
		float tagProb = (tagProb1 == null) ? epsilon : tagProb1.floatValue();
		twkey.set(0, thistag);
		
		//Bigram Implementation
		ttkey.set(1, thistag);
		
		boolean[] knownWord = (boolean[])allWords.get(word);
		Float twp1 = (Float)pTagWord.get(twkey);
		float twp  = (((knownWord == null)||(knownWord[0] != true)) ?
			      tagProb : 
			      ((twp1 == null) ?
			       epsilon :
			       twp1.floatValue()));
		
		//Bigram Implementation
		if (i==0){
			ttkey.set(0, StartTag);
			boolean[] knownTagPair = (boolean[])allTagPairs.get(ttkey);
			Float ttp1 = (Float)pGT.get(ttkey);
			
			float ttp  = (((knownTagPair == null)||(knownTagPair[0] != true)) ?
						 tagProb : ((ttp1 == null) ? pGT0 : ttp1.floatValue()));
		
			// In a bigram model, only the current probability matters
			pathprob[i][j]    = ttp + twp;
			}
			
			// Now create the back link to the max prob tag at the previous stage
			// If we are at the second word or further
			else if (i>0){
			float max = -100000000f;
			int back =0;
			for (int k = 0; k < numtags; k++){
				ttkey.set(0, tags[k]);
				boolean[] knownTagPair = (boolean[])allTagPairs.get(ttkey);
				Float ttp1 = (Float)pGT.get(ttkey);
				float ttp  = (((knownTagPair == null)||(knownTagPair[0] != true)) ?
							tagProb : ((ttp1 == null) ? pGT0 : ttp1.floatValue()));
							
				// In a bigram model, only the current probability matters
				float test    = pathprob[i-1][k] + ttp + twp;
				if (test > max) {
			    max     = test;
			    back    = k;
				}
				}
			pathprob[i][j] = max;
			backedge[i][j] = back;
			}
	    }
	}

	//Trace back finding most probable path
	{
	    float max    = -100000000f;
	    int   prevtag = 0;
	    
	    //Find final tag
	    for(int i = 0; i < numtags; i++) {
		float test = pathprob[len-1][i];
		
		if(max < test) {
		    max       = test;
		    prevtag    = i;
		}
	    }
	    
	    //Follow back edges to start tag and set tags on words
	    for(int i = len-1; i >= 0; i--) {
		Token tok = (Token)sent.get(i);
		tok.putAttrib("pos", tags[prevtag]);
		prevtag = backedge[i][prevtag];

	    }
	}
	//Delete next loop
	for (int i =0; i<len; i++){
		System.out.print(sent.get(i)+"\t");
		for(int j =0; j<numtags; j++){
		System.out.print("\t\t"+tags[j]+": "+pathprob[i][j]+"|"+tags[backedge[i][j]]);}
	System.out.println();}
    }
    
    /**
     * Tags a Vector of paragraphs by setting the &quot;pos&quot; attribute in the Tokens.
     *
     * @param testing The paragraphs to be tagged.
     */
    public void tag(Vector testing) {
	for(Iterator i = testing.iterator(); i.hasNext();) {
	    Vector para = (Vector)i.next();
	    
	    for(Iterator j = para.iterator(); j.hasNext();) {
		Vector sent = (Vector)j.next();
		
		//Bigram Implementation
		tagSentence1(sent);
	    
		}
	}
    }
    
    /**
     * Train on             the 1st XML file,
     * tag                  the 2nd XML file,
     * write the results in the 3rd XML file.
     *
     * @param argv An array of 3 XML file names.
     */
    public static void main(String[] argv) throws Exception
    {
	if(argv.length != 3) {
	    System.err.println("Wrong number of arguments.");
	    System.err.println(
			       "Format:  java cs481.postag.POSTag <train XML> <test XML> <output XML>");
	    System.err.println(
			       "Example: java cs481.postag.POSTag train.xml untagged.xml nowtagged.xml");
	    System.exit(1);
	}
	
	Vector training = Token.readXML(new BufferedInputStream(
								new FileInputStream(argv[0])));
	System.out.println("Read training file.");
	
	POSTag pt = new POSTag();
	pt.train(training);
	System.out.println("Trained.");
	training = null;  //Done with it, so let garbage collector reclaim
	
	Vector testing = Token.readXML(new BufferedInputStream(
							       new FileInputStream(argv[1])));
	System.out.println("Read testing file.");
	pt.tag(testing);
	System.out.println("Tagged.");
	Token.writeXML(testing,
		       new BufferedOutputStream(new FileOutputStream(argv[2])));
    }
}
