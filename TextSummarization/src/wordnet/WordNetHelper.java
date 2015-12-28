// Class with some static WordNet helper functions
package wordnet;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;


import net.didion.jwnl.*;
import net.didion.jwnl.data.*;
import net.didion.jwnl.data.list.*;
import net.didion.jwnl.data.relationship.*;
import net.didion.jwnl.dictionary.*;

public class WordNetHelper {

    // Dictionary object
    public static Dictionary wordnet;

    // Morphological processor object
    private static MorphologicalProcessor morph;
    
    // Initialize the database!
    public static void initialize(String propsFile) {

    	//String propsFile = "file_properties.xml";
        try {
            JWNL.initialize(new FileInputStream(propsFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (JWNLException e) {
            e.printStackTrace();
        }
        
        // Create dictionary object
        wordnet = Dictionary.getInstance();
        
        //Create morphological processor object
        morph = wordnet.getMorphologicalProcessor();
    }


    // Return array of POS objects for a given String
    public static POS[] getPOS(String s) throws JWNLException {
        // Look up all IndexWords (an IndexWord can only be one POS)
        IndexWordSet set = wordnet.lookupAllIndexWords(s);
        
        // Turn it into an array of IndexWords
        IndexWord[] words = set.getIndexWordArray();
        
        // Make the array of POS
        POS[] pos = new POS[words.length];
        for (int i = 0; i < words.length; i++) {
            pos[i] = words[i].getPOS();
        }
        return pos;
    }

    /**
     * To get the related words for a word
     * @param word Given word for which related words need to be found
     * @param type Type of relation to be found
     * @return related words for given word
     * @throws JWNLException
     */
    public static ArrayList getRelated(IndexWord word, PointerType type) throws JWNLException {
        try {
            Synset[] senses = word.getSenses();
            // Look for the related words for all Senses
            for (int i = 0; i < senses.length; i++) {
                ArrayList a = getRelated(senses[i],type);
                // If we find some, return them
                if (a != null && !a.isEmpty()) {
                    return a;
                }
            }
        } catch (NullPointerException e) {
            // System.out.println("Oops, NULL problem: " + e);
        }
        return null;
    }

    /**
     * To get related words for a given sense
     * @param sense The given sense
     * @param type The type of the relation
     * @return The list of related words for the given sense
     * @throws JWNLException
     * @throws NullPointerException
     */
    public static ArrayList getRelated (Synset sense, PointerType type) throws JWNLException, NullPointerException {
        PointerTargetNodeList relatedList;

        // Call a different function based on the type of relationship we are looking for
        if (type == PointerType.HYPERNYM) {
            relatedList = PointerUtils.getInstance().getDirectHypernyms(sense);
        } else if (type == PointerType.HYPONYM){
            relatedList = PointerUtils.getInstance().getDirectHyponyms(sense);
        } else if (type == PointerType.ANTONYM){
            relatedList = PointerUtils.getInstance().getAntonyms(sense);
        }else if (type == PointerType.PART_MERONYM){
            relatedList = PointerUtils.getInstance().getPartMeronyms(sense);
        }else if (type == PointerType.MEMBER_MERONYM){
            relatedList = PointerUtils.getInstance().getMemberMeronyms(sense);
        }else if (type == PointerType.SUBSTANCE_MERONYM){
            relatedList = PointerUtils.getInstance().getSubstanceMeronyms(sense);
        }else if (type == PointerType.SIMILAR_TO){
            relatedList = PointerUtils.getInstance().getCoordinateTerms(sense);
        }else if (type == PointerType.PART_HOLONYM){
            relatedList = PointerUtils.getInstance().getPartHolonyms(sense);
        }else if (type == PointerType.MEMBER_HOLONYM){
            relatedList = PointerUtils.getInstance().getMemberHolonyms(sense);
        }else if (type == PointerType.SUBSTANCE_HOLONYM){
            relatedList = PointerUtils.getInstance().getSubstanceHolonyms(sense);
        }else if (type == PointerType.DERIVED){
            relatedList = PointerUtils.getInstance().getDerived(sense);
        }else if (type == PointerType.SEE_ALSO){
            relatedList = PointerUtils.getInstance().getHolonyms(sense);
        } else {
            relatedList = PointerUtils.getInstance().getHolonyms(sense);
        } 
        
        // Iterate through the related list and make an ArrayList of Synsets to send back
        Iterator i = relatedList.iterator();
        ArrayList a = new ArrayList();
        while (i.hasNext()) {
            PointerTargetNode related = (PointerTargetNode) i.next();
            Synset s = related.getSynset();
            a.add(s);
        }
        return a;
    }

    
    // Get the IndexWord object for a String and POS
    public static IndexWord getWord(POS pos, String s) throws JWNLException {
        IndexWord word = wordnet.getIndexWord(pos,s);
        return word;
    }
    
    /* To get the stem word in WordNet for a given word
	 * @param word Word to be stemmed
	 * @return the stemmed word or null if it was not found in WordNet
	 */
	public static IndexWord getBaseWord ( String word ) throws JWNLException, NullPointerException
	{
		if ( word == null ) return null;
		
		IndexWord w;
		
			w = morph.lookupBaseForm( POS.NOUN, word );
			if ( w != null )
				return w;
				//return w.getLemma().toString ();
			w = morph.lookupBaseForm( POS.VERB, word );
			if ( w != null )
				return w;
				//return w.getLemma().toString();
			w = morph.lookupBaseForm( POS.ADJECTIVE, word );
			if ( w != null )
				return w;
				//return w.getLemma().toString();
			w = morph.lookupBaseForm( POS.ADVERB, word );
			if ( w != null )
				return w;
				//return w.getLemma().toString();
		
		return null;
	}
}
