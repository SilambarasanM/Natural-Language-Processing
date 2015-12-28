////////////////////////////////////////////////////////////////////////////////
//
//      MP 4 - cs585
//
//      Paul Chase
//
//      This implements a cfg style grammar
////////////////////////////////////////////////////////////////////////////////

import java.io.*;
import java.text.*;
import java.util.*;
import java.util.regex.*;

//this class implements the third assignment for CS585
public class Grammar
{
	private Vector productions;
	private Vector nonTerminals;
	
	//this reads the grammar in from a file
	public Grammar(String f) throws Exception
	{
		productions = new Vector();
		productions.clear();
		nonTerminals = new Vector();
		nonTerminals.clear();
		//load the file
		BufferedReader br = new BufferedReader(new FileReader(f));
		Production p;
		String str = br.readLine();
		String rule[];
		while(str!=null)
		{
			rule = str.split("\t");
			p = new Production();
			p.probability = (new Float(rule[0])).floatValue();
			p.left = rule[1];
			p.right = rule[2].split(" ");
			p.dot = 0;
			p.start = 0;
			productions.add(p);
			addNonTerminal(rule[1]);
			str = br.readLine();
		}
	}

	//checks if we've seen this nonterminal; if we haven't, add it
	private void addNonTerminal(String s)
	{
		for(int i=0;i<nonTerminals.size();i++)
		{
			if(((String)nonTerminals.get(i)).compareTo(s)==0){
				return;
			}
		}
		nonTerminals.add(s);
	}

	private final boolean isNonTerminal(String s)
	{
		//return true if it's a non-terminal
		for(int i=0;i<nonTerminals.size();i++)
		{
			if(((String)nonTerminals.get(i)).compareTo(s)==0){
				return true;
			}
		}
		return false;
	}

	//this function predicts possible completions of p, and adds them to v
	private final void predict(Vector v, Production p,int pos)
	{
		Vector prods = getProds(p.right[p.dot]);
		Production q,r;
		for(int j=0;j<prods.size();j++)
		{
			r = (Production)prods.get(j);
			q = new Production(r);
			q.dot = 0;
			q.start = pos;
			addProd(v,q);
		}
	}

	//this checks if we can scan s on the current production p
	private final boolean scan(Vector v, Production p, String s)
	{
		Production q;
		if(p.right[p.dot].compareTo(s)==0)
		{
			//match - add it to the next vector
			q = new Production(p);
			q.dot = q.dot + 1;
			//Debug
			//System.out.println("Invoking add prod from scan for next");
			addProd(v,q);
			return true;
		}
		return false;
	}

	//this takes a completed production and tries to attach it back in the
	//cols table, putting any attachments into cur.
	private final void attach(Vector cols, Vector cur, Production p, HashMap back)
	{
		//if the next thing in one rule is the first thing in this rule,
		//we attach.  otherwise ignore
		Vector col;
		Production q,r;
		String s = p.left;
		boolean match = false;
		
		col = (Vector)cols.get(p.start);
		for(int j=0;j<col.size();j++)
		{
			q = (Production)col.get(j);
			if(q.right.length > q.dot)
				if(q.right[q.dot].compareTo(s)==0)
				{	//Attach!
					r = new Production(q);
					r.dot = r.dot + 1;
					//Adding back pointer
					if(!r.childExists()){
						r.spawn(q.right.length);
					}
					r.spawn(r.dot-1, p);
					addPointer(back, p, r);
					addProd(cur,r);
				}
		}
	}
	
	//this adds back pointers to the matched production rules
	private final void addPointer(HashMap back, Production p, Production r)
	{
		Vector ptr = new Vector(); ptr.clear();
		if(r.dot-1==0){
			ptr.add(p);
		}
		else{
			Production q = new Production(r);
		    	q.dot = q.dot-1;
		    	for(Iterator i = back.keySet().iterator(); i.hasNext();) {
				    Production key = (Production)i.next();
				    if(key.equals(q)){
				    	Vector bp = (Vector) back.get(key);
				    	ptr.addAll(bp);
				    	ptr.add(p);
				    	break;
				    }
		    	}
		}
		back.put(r, ptr);
	}
	
	//this parses the sentence
	public final Production parse(String sent[])
	{
		//this is a vector of vectors, storing the columns of the table
		Vector cols = new Vector();	cols.clear();
		//this is the current column; a vector of production indices
		Vector cur = new Vector();	cur.clear();
		//this is the next column; a vector of production indices
		Vector next = new Vector();	next.clear();	
		//Adding back pointer
		//this is the back pointer; a vector of production indices
		HashMap back = new HashMap();	back.clear();
		//add the first symbol
		cur.add((Production)getProds("ROOT").get(0));
		Production p;
		for(int pos=0;pos<=sent.length;pos++)
		{
			int i=0;
			boolean match = false;
			//check through the whole vector, even as it gets bigger
			while(i!=cur.size())
			{
				p = (Production)cur.get(i);
				if(p.right.length > p.dot)
				{	//predict and scan
					if(sent.length == pos)
					{
						match = true;
						
					} else{
						if(isNonTerminal(p.right[p.dot]))
						{
							//predict adds productions to cur
							predict(cur,p,pos);
						}else{
							//scan adds productions to next
						    //System.out.println("scan (next): " + p.toString() + " ("+pos+")= " + sent[pos]);
						    if(scan(next,p,sent[pos])) {
							match = true;
						    }
						}
					}
				} else {	//attach
					//Adding back pointer
				    attach(cols,cur,p,back);
					//attach(cols,cur,p);
				    if(sent.length == pos)
					{
					    match = true;
					}
				}
				i++;
				//Debug
				/*System.out.println("Cur");
				print(cur);
				System.out.println("Next");
				print(next);
				System.out.println("Back");
				print(back);
				//System.out.println("Cols");
				//print(cols);
				System.out.println("Incrementing i");*/
				//when using a gargantuan grammar
				//this spits out stuff if it's taking a long time.
				if(i%100 == 0)
					System.out.print(".");
			}
			cols.add(cur);
			if(!match)
			{
			    printTable(cols,sent);
			    System.out.println("Failed on: "+ cur);
			    return null;
			}
			
			//Debug
			/*System.out.println("Cur");
			print(cur);
			System.out.println("Next");
			print(next);
			//System.out.println("Cols");
			//print(cols);
			System.out.println("Back");
			print(back);*/
			cur = next;
			next = new Vector();	next.clear();
			//System.out.println();
		}
		
		
		//print the Earley table
		//Comment this out once you've got parses printing; it's
		//only here for your evaluation.
		printTable(cols,sent);

		
		System.out.println("\n\nBack Links\n------------------");
		print(back);
		System.out.println();
		
		//Right now we simply check to see if a parse exists;
		//in other words, we see if there's a "ROOT -> x x x ."
		//production in the last column.  If there is, it's returned; otherwise
		//return null.
		//TODO: Return a full parse.
		cur = (Vector)cols.get(cols.size()-1);
		Production finished = new Production((Production)getProds("ROOT").get(0));
		finished.dot = finished.right.length;
		boolean parsed = false;
		for(int i=0;i<cur.size();i++)
		{
			p = (Production)cur.get(i); 
			if(p.equals(finished))
			{
				parsed = true;
				//return p;
			}
		}
		if(parsed){
			//System.out.println("Finished right: "+finished.right[0]);
			Vector finalProds = getFinalProds(cols);
			//getFinalParse(cols, back, finalProds);
			Production pFinal = new Production();
			for(int i = 0; i<finalProds.size(); i++){
				Production s = (Production) finalProds.get(i);
				//System.out.println("s left: "+s.left);
				if(s.left.equals("s")){
					pFinal = s;
				}
				
			}
			return pFinal;
		}
		else{
			return null;
		}
	}
	
	/*private void getFinalParse(Vector cols, HashMap back, Vector finalProds){
		Vector tree = new Vector();
		ArrayList trees = new ArrayList();
		ArrayList<String> cur = new ArrayList<String>();
		cur.add("s");
		int cptr = 0, curcount = 0;
		int iteration = 1000;
		int i = cols.size()-1;
		do{ 
			System.out.println("Current Column = "+i);
			Vector col = (Vector) cols.get(i); 
			for(int j = 0; j <col.size(); j++){
				Production p = (Production) col.get(j);
				System.out.println("Current Vector = "+j+"\t Prod = "+p);
				System.out.println("Checking "+cur.get(cptr)+" with "+p.left);
				if(cur.get(cptr).equals(p.left)){
					if(p.dot==p.right.length){
						System.out.println("Adding "+p);
						trees.add(p);
						for(int k=p.right.length-1; k>=0; k--){
							cur.add(p.right[k]);
						}
						cptr++;	//for tracking rule one by one
						curcount++;	//tracking to reiterate if required
					}
				}
			}
			iteration--;
			if(iteration == 0)
				break;
			if(curcount==col.size()){
				i--;
				col.clear();
				curcount=0;
			}
		}while(i>=0);
		System.out.println("Final Parse ready!");
		return;
	}*/
	
	private boolean scanAdd(Vector col, Production b, Vector backLinks){
		for (int j=0; j<col.size(); j++){
			Production p = (Production) col.get(j);
			if(p.left == b.right[b.dot-1])
			{
				if(p.right.length==p.dot){
					addProd(backLinks, p);
					return true;
				}
			}
		}
		return false;
		
	}

	/**this prints the table in a human-readable fashion.
	 * format is one column at a time, lists the word in the sentence
	 * and then the productions for that column.
	 * @param cols The columns of the table
	 * @param sent the sentence
	 */
	private final void printTable(Vector cols,String sent[])
	{
		Vector col;
		//print one column at a time
		for(int i=0;i<cols.size();i++)
		{
			col = (Vector)cols.get(i);
			//sort the columns by 
			if(i>0)
			{
				System.out.println("\nColumn "+i+": "+sent[i-1]+"\n------------------------");
			}else{
				System.out.println("\nColumn "+i+": ROOT\n------------------------");
			}
			
			for(int j=0;j<col.size();j++)
			{
				System.out.println(((Production)col.get(j)).toString());
			}
		}
	}

	//this adds a production p to the vector v of production indices
	//it also checks for duplicate indices, and skips those
	private final void addProd(Vector v, Production p)
	{
		//check for duplicates
		for(int i=0;i<v.size();i++)
			if(((Production)v.get(i)).equals(p)){
				//Debug
				Production q = (Production)v.get(i);
				return;
			}
		v.add(p);
		//Debug
		//System.out.println("addProd: Added to v |"+p);
	}

	//This runs through the columns and returns all the fully parsed productions
	//i.e. those with little dots at the very end.
	private final Vector getFinalProds(Vector cols)
	{
		Vector cur;
		Vector prods = new Vector();	prods.clear();
		Production p;
		for(int i=0; i<cols.size(); i++)
		{
			cur = (Vector)cols.get(i);
			for(int j=0;j<cur.size();j++)
			{
				p = (Production)cur.get(j);
				if(p.right.length == p.dot)
				{
					if(p.left.compareTo("ROOT")!=0)
					{
						p.childExists();
						//System.out.println();
						//System.out.println("Children?"+p.childExists());
						prods.add(p);
					}
					/*else{
						if(i == cols.size()-1)
							System.out.println("ROOT");
					}*/
				}
			}
		}
		//convert it to an array for returning
		return prods;
	}

	//this returns true if a string is in the grammar, false otherwise
	//it's not exactly "comprehensive"... mostly it'll just see if all
	//the tokens in the sentence are terminals.
	private final boolean inGrammar(String s)
	{
		boolean found=false;
		Production p;
		//Debug
		//System.out.println("inGrammar invoked for "+s+"!");
		for(int i=0;i<productions.size();i++)
		{
			p = (Production)productions.get(i);
			for(int j=0;j<p.right.length;j++)
				if(p.right[j].indexOf(s)!=-1)
					found = true;
			//we can't have a string equal to a non-terminal
			if(p.left.compareTo(s)==0)
			{
				System.out.println("String contains a non-terminal - cannot parse");
				return false;
			}
		}
		return found;
	}

	//this returns a vector of productions with a left side matching the
	//argument; happy string comparing.
	private final Vector getProds(String left)
	{
		//we store it in a vector for safekeeping
		Vector prods = new Vector();	prods.clear();
		Production p;
		for(int i=0;i<productions.size();i++)
		{
			p = (Production)productions.get(i);
			if(p.left.compareTo(left)==0){
				//Debug
				//System.out.println("\t"+left+": Adding "+p);
				prods.add(p);
			}
		}
		//convert it to an array for returning
		return prods;
	}

	//this checks if the given string[] has a parse tree with this grammar
	//
	public final boolean canParse(String sent[])
	{
		//check if all symbols are in the grammar
		for(int i=0;i<sent.length;i++)
			if(!inGrammar(sent[i]))
				return false;
		return true;
	}

	//this prints out the grammar
	public void print()
	{
		System.out.println(this.toString());
	}

	//what does every toString function do?
	public String toString()
	{
		String ret = "";
		for(int i=0;i<productions.size();i++)
			ret = ret + ((Production)productions.get(i)).toString() + "\n";
		return ret;
	}
	
	//Debug
	public void print(Vector v){
		System.out.println("Printing Vector...");
		for (int i =0; i< v.size();i++){
			Production p = (Production) v.get(i);
			System.out.print(p+" ==>\t");
			p.childExists();
			System.out.println();
			//System.out.println(v.get(i));
		}
	}
	//Debug
		public void print(HashMap h){
			for(Iterator i = h.keySet().iterator(); i.hasNext();) {
			    Production key = (Production)i.next();
			    if(key.dot==key.right.length){
			    System.out.print(key+"\t\t=>\t{\t");
			    Vector  bp  = (Vector) h.get(key);
			    for (int j =0; j< bp.size();j++){
			    	Production p = (Production) bp.get(j);
		    		System.out.print(p.toString());
		    		if(j!=bp.size()-1) System.out.print(" ,\t");
			    }
			    System.out.println("}");
			    }
			}
		}
}
