package wordnet;

import java.io.*;
import java.util.*;

import net.didion.jwnl.*;
import net.didion.jwnl.data.*;
import net.didion.jwnl.data.list.*;
import net.didion.jwnl.data.relationship.*;
import net.didion.jwnl.dictionary.*;
import org.apache.commons.logging.*;

import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class LexicalChains {
	
	//For POS tagger model
	protected MaxentTagger tagger;
	
	//Initial LexChains <Synset, word chain>
	protected HashMap<Synset, ArrayList<String>> lChain;
	
	//Non-redundant Lex Chains <Synset, word chain>
	protected HashMap<Synset, ArrayList<String>> LexChain;
	
	//Lex Chain Count <Synset, word count>
	protected HashMap<Synset, Integer> lCount;
	
	//Lex Chain Length <Synset, Chain Length>
	protected HashMap<Synset, Integer> chainLength;
	
	//Word and number of occurrences <word, count>
	protected HashMap<String, Integer> wCount;
	
	//Input sentences
	protected List<List<HasWord>> sentences;
	
	//Scores for the non-redundant chains
	protected HashMap<Synset, Float> Scores;
	
	public LexicalChains(String propFile) throws JWNLException{
		//Initialize the WordNet Dictionary
		WordNetHelper.initialize(propFile);
		
		lChain  = new HashMap<Synset, ArrayList<String>>();
		LexChain  = new HashMap<Synset, ArrayList<String>>();
		lCount  = new HashMap<Synset, Integer>();
		wCount	= new HashMap<String, Integer>();
		chainLength	= new HashMap<Synset, Integer>();
		Scores	= new HashMap<Synset, Float>();
	}
	
	/*
	 * To Build Lexical Chains for the given text
	 * 
	 * @param inputFile The input text file name.
	 * 
	 */
	protected void build(String inputFile)throws JWNLException, FileNotFoundException{
		
		
		//Stanford POS tagger model initialization
		tagger	= new MaxentTagger("dat/english-bidirectional-distsim.tagger");
		
		//Tokenizing the text into sentences and words
		sentences = MaxentTagger.tokenizeText(new BufferedReader(new FileReader(inputFile)));
		
		System.out.println("Building Lexical Chains...");
		
		for (List<HasWord> sentence : sentences) {
		   	List<TaggedWord> tSentence = tagger.tagSentence(sentence);
		    for (TaggedWord tw : tSentence) {
		    	
		    	//Filtering only noun words with tags NN, NNP, NNS, NNPS
		        if (tw.tag().startsWith("NN")) {	
		        	String curWord = tw.word().toLowerCase();
		        	//System.out.print(tw.word()+"/"+tw.tag()+":\t");
		        	
		        	//Looking up the dictionary for the word, else look for base form
		        	IndexWord w = WordNetHelper.getWord(POS.NOUN, curWord) == null?
		   			WordNetHelper.getBaseWord(curWord):WordNetHelper.getWord(POS.NOUN, curWord);
		   			
		   			boolean wExists = addWordCount(wCount, curWord);
		   			
		            //System.out.println(w+"\n\tSenses:");
		   			//Ignoring words already occurred or not available in WordNet
		            if (w!= null && !wExists){	
		            	Synset[] senses = w.getSenses();
			            for (int i = 0; i < senses.length; i++) {
			               //System.out.println("\t\t"+senses[i]);
			               
			               addWordChain(lChain, senses[i], curWord);
			            } 
			            ArrayList hyponyms = WordNetHelper.getRelated(w,PointerType.HYPONYM);
		               //System.out.println("\t\tHYPONYMS: ");
              
		               if (hyponyms!=null) {
		                   // Adding all the hyponyms for the sense
		                   for (int j = 0; j < hyponyms.size(); j++) {
		                       Synset s = (Synset) hyponyms.get(j);
		                       addWordChain(lChain, s, curWord);
		                       //System.out.println("\t\t\t"+s);
		                       }
		               } 
		               ArrayList hypernyms 
			               		= WordNetHelper.getRelated(w,PointerType.HYPERNYM);
			               //System.out.println("\t\tHYPERNYMS: ");
			               
			           if (hypernyms!=null) {
			        	   // Adding all the hypernyms for the sense
			        	   for (int j = 0; j < hypernyms.size(); j++) {
		                       Synset s = (Synset) hypernyms.get(j);
		                       addWordChain(lChain, s, curWord);
		                       //System.out.println("\t\t\t"+s);
	                       }
		               }
		               ArrayList antonyms 
		               		= WordNetHelper.getRelated(w,PointerType.ANTONYM);
			                   //System.out.println("\t\tANTONYMS: ");
				               
			           if (antonyms!=null) {
			        	   // Adding all the antonyms for the sense
			               for (int j = 0; j < antonyms.size(); j++) {
			            	   Synset s = (Synset) antonyms.get(j);
			                   addWordChain(lChain, s, curWord);
			                   //System.out.println("\t\t\t"+s);
			               }
		               }
				               
			           //Enhancements
			           ArrayList partMeronyms 
			           		= WordNetHelper.getRelated(w,PointerType.PART_MERONYM);
			               //System.out.println("\t\tPART_MERONYM: ");
				               
			           if (partMeronyms!=null) {
			        	   // Adding all the part meronyms for the sense
			               for (int j = 0; j < partMeronyms.size(); j++) {
			            	   Synset s = (Synset) partMeronyms.get(j);
			                   addWordChain(lChain, s, curWord);
			                   //System.out.println("\t\t\t"+s);
			               }
		               }
				           
			           ArrayList memberMeronyms 
			           		= WordNetHelper.getRelated(w,PointerType.MEMBER_MERONYM);
		               //System.out.println("\t\tMEMBER_MERONYM: ");
			           
			           if (memberMeronyms!=null) {
			        	   // Adding all the member meronyms for the sense
			               for (int j = 0; j < memberMeronyms.size(); j++) {
			            	   Synset s = (Synset) memberMeronyms.get(j);
			                   addWordChain(lChain, s, curWord);
			                   //System.out.println("\t\t\t"+s);
			               }
		               }
				           
			           ArrayList substanceMeronyms 
			           		= WordNetHelper.getRelated(w,PointerType.SUBSTANCE_MERONYM);
		               //System.out.println("\t\tSUBSTANCE_MERONYM: ");
			              
			           if (substanceMeronyms!=null) {
			        	   // Adding all the substance meronyms for the sense
			        	   for (int j = 0; j < substanceMeronyms.size(); j++) {
			        		   Synset s = (Synset) substanceMeronyms.get(j);
			                   addWordChain(lChain, s, curWord);
			                   //System.out.println("\t\t\t"+s);
			               }
		               }
				           
			           ArrayList similarTo 
			           		= WordNetHelper.getRelated(w,PointerType.SIMILAR_TO);
		               //System.out.println("\t\tSIBLINGS: ");
			              
			           if (similarTo!=null){
			        	   // Adding all the siblings for the sense
			               for (int j = 0; j < similarTo.size(); j++) {
			            	   Synset s = (Synset) similarTo.get(j);
			                   addWordChain(lChain, s, curWord);
			                   //System.out.println("\t\t\t"+s);
			               }
		               }
			           
			           ArrayList partHolonyms 
		           			= WordNetHelper.getRelated(w,PointerType.PART_HOLONYM);
			           //System.out.println("\t\tPART_HOLONYM: ");
		              
			           if (partHolonyms!=null){
			        	   // Adding all the part holonyms for the sense
			        	   for (int j = 0; j < partHolonyms.size(); j++) {
			        		   Synset s = (Synset) partHolonyms.get(j);
			        		   addWordChain(lChain, s, curWord);
			        		   //System.out.println("\t\t\t"+s);
			        	   }
			           }
			           
			           ArrayList memberHolonyms 
	           				= WordNetHelper.getRelated(w,PointerType.MEMBER_HOLONYM);
			           //System.out.println("\t\tMEMBER_HOLONYM: ");
	              
			           if (memberHolonyms!=null){
			        	   // Adding all the siblings for the sense	
			        	   	for (int j = 0; j < memberHolonyms.size(); j++) {
			        	   		Synset s = (Synset) memberHolonyms.get(j);
			        	   		addWordChain(lChain, s, curWord);
			        	   		//System.out.println("\t\t\t"+s);
			        	   	}
			           }
			           
			           ArrayList subsHolonyms 
          					= WordNetHelper.getRelated(w,PointerType.SUBSTANCE_HOLONYM);
			           //System.out.println("\t\tSUBSTANCE_HOLONYM: ");
             
			           if (subsHolonyms!=null){
			        	   // Adding all the siblings for the sense	
		        	   		for (int j = 0; j < subsHolonyms.size(); j++) {
		        	   			Synset s = (Synset) subsHolonyms.get(j);
		        	   			addWordChain(lChain, s, curWord);
		        	   			//System.out.println("\t\t\t"+s);
		        	   		}
			           }
		            }
		       // }
		      }
		    }
	    }
		
		/* Debug Mode Statements
		System.out.println("lChain :");
		printHashStringList(lChain);
		System.out.println("lCount : ");
		debugPrintHashLongInt(lCount);
		System.out.println("wCount");
		debugPrintHashStringInt(wCount);
		*/
	}
	
	/**
	 * To remove redundant lexical chains
	 */
	public void getLexicalChains(){
		System.out.println("Done.");
		for (int i = lChain.size(); i>=0 ;i--){
			//Find the most recent length of the lexical chains
			calculateChainLen();
			
			//Get the chain with maximum number of words
			Synset maxKey = getMaxChain();
			//System.out.println("maxKey: "+maxKey);
			
			//Ignoring chains with words already considered
			if (lCount.get(maxKey)==0) 
				break;
			
			//To create the non-redundant chosen lexical chains
			addChain(maxKey);
			
			// Ignoring chains of length zero
			if (lChain.size()==0) 
				break;
		}
    		
		//System.out.println("LexChain :");
		//printHashStringList(LexChain);
	}
	
	/**
	 * To extract the strong chains and removing redundant occurrences
	 *  of the words in other chains
	 *  
	 * @param mKey Chain with maximum number of words
	 */
	public void addChain(Synset mKey){
		
		ArrayList<String>  mChain  = lChain.get(mKey);
		Integer chainCount = lCount.get(mKey);
		
		for(int j = 0; j < mChain.size(); j++) {			
			String word = mChain.get(j).toLowerCase();
			//System.out.println("Removing word: "+word);
			
			ArrayList<Synset> voidChain = new ArrayList<Synset>();
			for(Iterator<Synset> i = lChain.keySet().iterator(); i.hasNext();) {
	    		Synset key = i.next();
	    		ArrayList<String> chain  = lChain.get(key);
	    		if (chain.contains(word) && key!=mKey){
	    			chain.remove(word);
	    			//System.out.println(word+" removed from the chain "+key.getOffset());
	    			
	    			Integer value = lCount.get(key)-1;
	    			//System.out.println("Update count for chain "+key.getOffset()+" is "+value);
	    			lCount.put(key, value);
	    			
	    			//Book-keeping the updates to remove empty chains 
	    			if(value==0||chain.size()==0){
	    				//System.out.println("Chain "+key.getOffset()+" added to Void");
	    				voidChain.add(key);
	    			}
	    		}
			}
			
			//Removing empty chains
			for(int k = voidChain.size()-1; k>=0; k--){
				lChain.remove(voidChain.get(k));
				lCount.remove(voidChain.get(k));
				chainLength.remove(voidChain.get(k));
				//System.out.println("Empty chain removal for "+voidChain.get(k).getOffset());
			}
		}
		//Removing the processed chains from the main list from consideration of next iteration
	    lChain.remove(mKey);
		lCount.remove(mKey);
		chainLength.remove(mKey);
		
		//if(mChain.size()>1)
		LexChain.put(mKey,mChain);
		//System.out.println("Traces of processed chain removal for "+mKey.getOffset());
	}
	
	/**
	 * To calculate the updated chain lengths after redundant removal
	 */
	protected void calculateChainLen(){
		for(Iterator<Synset> i = lChain.keySet().iterator(); i.hasNext();) {
    		Synset key = i.next();
    		int wordCount = 0;
    		ArrayList<String>  chain  = lChain.get(key);
    		Integer chainCount = lCount.get(key);
    		
    		for(int j = 0; j < chain.size(); j++) {
    			String word = chain.get(j).toLowerCase();
    			Integer c =  wCount.get(word);
    			wordCount+=c-1;
		    }
    		Integer value = chainCount+wordCount;
    		chainLength.put(key, value);
	    }
	}
	
	/**
	 * To find the chain of maximum word occurrences.
	 * 
	 * @return returns the Synset with maximum word occurrences
	 */
	protected Synset getMaxChain(){
		
        Map.Entry<Synset, Integer> maxEntry = null;

        for (Map.Entry<Synset, Integer> entry : chainLength.entrySet()) {
            if (maxEntry == null || entry.getValue()> (maxEntry.getValue()) ) {
                maxEntry = entry;
            }
        }

        //System.out.println("Key:"+maxEntry.getKey()+" Value: "+ maxEntry.getValue());
        
		return maxEntry.getKey();
	}
	

	/**
	 * To summarize the given text
	 */
	public void summarize(){
		System.out.println("Identifying the strong lexical chains...");
		//Scoring the short-listed lexical chains and finding the mean
		float mean = scoreChains();
		
		HashMap<Synset, ArrayList<String>> strongChains = getStrongChains(mean);
		System.out.println("\nLexical Chains :");
		printHashStringList(strongChains);
		
		System.out.println("\nSummarizing the text...");
		HashMap<Integer, List<HasWord>> summarizedText = new HashMap<Integer, List<HasWord>>();		 
		 
	    for (Map.Entry<Synset, ArrayList<String>> entry : strongChains.entrySet()) {
	        String maxEntry = null;
	        Integer sentenceOrder = new Integer(0);
	        
	        ArrayList<String> word = entry.getValue();
	        
	        //Finding the representative word for the chain with maximum contribution
	        for(int i = 0; i < word.size(); i++){
	        	if (maxEntry == null || wCount.get(word.get(i))> wCount.get(maxEntry)) {
	        		maxEntry = word.get(i);
	            }
	        }
	        //System.out.println("Representative Word found! " + maxEntry);
	        
	        List<HasWord> maxSentence = null;
	        boolean maxExists = false;
	        
	        //Identifying first sentence with the representative word in the given text
	        for (int i = 0; i<sentences.size(); i++) {		
	        	List<HasWord> sentence = sentences.get(i);	
	    		for(int j = 0; j<sentence.size(); j++){
	    			if(maxEntry.equals(sentence.get(j).toString().toLowerCase())){
	    				maxSentence = sentence;
	    				maxExists = true;
	    				sentenceOrder=i;
	    				break;
	    			}
	    		}
	    		
	    		if(maxExists)
	    			break;
	    	}
	        
	        summarizedText.put(sentenceOrder, maxSentence);
	    }
	        
	    System.out.println("Done.\n");

        for(int i = 0; i < sentences.size() ; i++) {
    	    if (summarizedText.get(i)!= null){
    	    	List<HasWord> s = summarizedText.get(i);
    	    	for (int j =0; j < s.size();j++){
					if (j!= s.size()-1) 
						System.out.print(" " + s.get(j));
					else
						System.out.print(s.get(j));
				}
    	    }
    	}       
	}
	
	/**
	 * To identify the strong chains that are above the mean score
	 * 
	 * @param mean - mean value of scores of all lexical chains
	 * @return the strong chains that satisfy the minimum mean score
	 */
	public HashMap<Synset, ArrayList<String>> getStrongChains(float mean){
		HashMap<Synset, ArrayList<String>> strongChains = new HashMap<Synset, ArrayList<String>>();
		for(Iterator i = Scores.keySet().iterator(); i.hasNext();) {
    		Synset key = (Synset)i.next();
    		Float  score  = (Float) Scores.get(key);
    		 if(score.floatValue() > mean){
    			 strongChains.put(key, LexChain.get(key));
    		 }
	    }
		return strongChains;
	}
	
	/**
	 * To score each lexical chain and find the mean value
	 * 
	 * @return the mean of the scores of all the chains
	 */
	public float scoreChains(){
		float sum = 0, count= 0;
    	for(Iterator i = LexChain.keySet().iterator(); i.hasNext();) {
    		Synset key = (Synset)i.next();
    		int wordCount = 0;
    		ArrayList  chain  = (ArrayList) LexChain.get(key);
    		
    		//Unique word count in a chain
    		Integer distinctWords = chain.size();

    		//Total occurrences of the words in the chain
    		for(int j = 0; j < chain.size(); j++) {
    			String word = chain.get(j).toString().toLowerCase();
    			Integer c = (Integer) wCount.get(word);
    			wordCount+=c-1;
		    }

    		//The length of each chain
		    Integer length = distinctWords + wordCount;
		    
		    //Score = Length * Homogeneity Index of the chain
		    //Homogeneity Index = 1 - (Unique word count / Length of the chain)
		    Float value = new Float(length.floatValue() * (1.00-(distinctWords.floatValue()/length.floatValue())));
		    Scores.put(key, value);
		    
		    sum+=value;
		    count++;
	    }
    	
    	//System.out.println("Scores :");
		//debugPrintHashScores(Scores);
		
    	return (sum/count);
	}
   
    /**
     * Increment the count in a HashMap for t.
     *
     * @param h1 The HashMap to be modified
     * @param t  The key of the field to increment
     */
    protected boolean addWordCount(HashMap h1, String t)
    {
	if(h1.containsKey(t)) {
		Integer ip = (Integer) h1.get(t);
		ip++;
		h1.put(t, ip);
		return true;
	}
	else {
		Integer ip = new Integer(1);		
		h1.put(t, ip);
		return false;
	}
    }
    
    /**
     * Print out a HashMap<String, Integer>.
     *
     * @param h The HashMap to be printed.
     */
    protected void debugPrintHashStringInt(HashMap h) {
    	for(Iterator i = h.keySet().iterator(); i.hasNext();) {
    		String key = (String)i.next();
    		Integer  ip  = (Integer)h.get(key);	    
    		System.out.println(", " + key + ": " + ip);
    	}
    }
    
    /**
     * Print out a HashMap<Synset,Integer]>.
     *
     * @param h The HashMap to be printed.
     */
    protected void debugPrintHashLongInt(HashMap h) {
    	for(Iterator i = h.keySet().iterator(); i.hasNext();) {
    		Synset key = (Synset)i.next();
    		Integer  ip  = (Integer)h.get(key);
    		System.out.println(", " + key.getOffset()+ ": " + ip);
    	}
    }
    
    /**
     * Print out a HashMap<Synset,Float>.
     *
     * @param h The HashMap to be printed.
     */
    protected void debugPrintHashScores(HashMap h) {
    	for(Iterator i = h.keySet().iterator(); i.hasNext();) {
    		Synset key = (Synset)i.next();
    		Float  ip  = (Float)h.get(key);
    		System.out.println(", " + key.getOffset()+": " + ip);
    	}
    }
    

    /**
     * Print out a HashMap<Synset,ArrayList<String>>.
     *
     * @param h The HashMap to be printed.
     */
    protected void printHashStringList(HashMap h) {
    	int count = 1;
    	for(Iterator i = h.keySet().iterator(); i.hasNext();) {
    		Synset key = (Synset)i.next();
    		int wordCount = 0;
    		ArrayList  chain  = (ArrayList) h.get(key);
       		Integer chainCount = chain.size();
    		System.out.print("Chain " + /*" { " +key.getOffset()+ " }" +*/  count /*+ "(" + chainCount + ")"*/ +": ");
    		
    		for(int j = 0; j < chain.size(); j++) {
    			String word = chain.get(j).toString().toLowerCase();
    			Integer c = (Integer) wCount.get(word);
    			System.out.print(word + "(" + c +")");
    			if (j!= chain.size()-1) System.out.print(", ");
    			wordCount+=c-1;
		    }
    		System.out.println(/*"--> Count = "+(chainCount+wordCount)*/);
		count++;
	    }
    }
    
    /**
     * Increment the count in a HashMap for s with t.
     *
     * @param h1 The HashMap to be modified
     * @param s  The key of the field to increment
     * @param t The word to be added to the chain
     */
    protected boolean addWordChain(HashMap h1, Synset s, String t)
    {
    	if(h1.containsKey(s)){
    		ArrayList chains = (ArrayList) h1.get(s);  //Used as int *
    		if (!chains.contains(t)){
    			chains.add(t);
    			h1.put(s, chains);
    			addChainCount(lCount, s);
    		}
		return true;
    	}
    	else {
    		ArrayList chains = new ArrayList();;
    		chains.add(t);
    		h1.put(s, chains);
    		addChainCount(lCount, s);
    		return false;
	    }
    }
    
    
    /**
     * Increment the count in a HashMap for s.
     *
     * @param h1 The HashMap to be modified
     * @param s  The key of the field to increment
     */
    protected boolean addChainCount(HashMap h1, Synset s)
    {
    	if(h1.containsKey(s)){		
    		Integer ip = (Integer)h1.get(s);
    		ip++;
    		h1.put(s, ip);
    		return true;
	    }
    	else{
    		Integer ip = new Integer(1);;
    		h1.put(s, ip);
    		return false;
    	}
    }
    
    /**
     * The Main Function
     * @param args - CommandLine Arguments
     * @throws JWNLException
     * @throws FileNotFoundException
     */
	public static void main(String[] args) throws JWNLException, FileNotFoundException 
	{	
		LexicalChains lc;
	
		lc = new LexicalChains("properties.xml");
		
		Scanner sc = new Scanner(System.in);
		System.out.print("Enter the input filename with complete path: ");
		String fileName = sc.nextLine();
		
		//lc.build("input.txt");
		lc.build(fileName); 
		
		lc.getLexicalChains();
		lc.summarize(); 
		
    }

}
