/**This implements a production in a pcfg grammar
 *
 * @author Paul Chase: chaspau@iit.edu
 * @version 1.0
 * 
 */

import java.io.*;
import java.text.*;
import java.util.*;
import java.util.regex.*;

public class Production
{
	float probability;
	String left;
	String right[];
	int dot;
	int start;

	//Production does double duty as a parse tree; this is for that.
	//therefore, has same number of children as right[], one for
	//each; if there is no child there, null is stored instead.
	Production children[];
	//parent is for the linking as well.
	Production parent;

	/**Simple constructor, assumes no children, initializes everybody.*/
	Production()
	{
		probability=0.0f;
		left = "";
		right = null;
		dot = 0;
		start = 0;
		children = null;
		parent = null;
	}

	/**Constructs a production with n right productions.*/
	Production(int n)
	{
		this();
		right = new String[n];
		children = new Production[n];
		for(int i=0;i<n;i++)
		{
			right[i] = null;
			children[i] = null;
		}
	}

	/**Constructs a production with the given right hand side.*/
	Production(String[] rhs)
	{
		this(rhs.length);
		right = rhs;
	}

	/**Copy constructor.*/
	Production(Production p)
	{
		probability = p.probability;
		left = p.left;
		right = p.right;
		dot = p.dot;
		start = p.start;
		children = p.children;
	}

	/**This creates a child of the production given its index.
	 * This adds the child to the production and sets the parent for
	 * the newly created child production.
	 * 
	 * @param n the index on the right hand side where the child attaches
	 * @return The newly created child
	 */
	public final Production spawn(int n)
	{
		//Debug
		//System.out.println("Spawning Begins");
		Production p = new Production();
		p.parent = this;
		children = new Production[n];
		for(int i =0; i<n; i++){
			//System.out.println("i="+i);
			children[i] = p;
		}
		return p;
	}

	/**This creates a child of the production given its index.
         * This adds the child to the production and sets the parent for
         * the newly created child production.  The new child production
	 * will be a copy of the production input as a parameter.
         *
         * @param n the index on the right hand side where the child attaches
	 * @param prod the production to copy the child from
         * @return The newly created child
         */
        public final Production spawn(int n, Production prod)
        {
        	/*Production p;
        	if(children[n].right==null){
        		System.out.println("New Prod");
        		p = new Production(prod);
                p.parent = this;
                children[n] = p;
                return this;
        	}
        		else{
        			p = new Production(this);
        			p.children[n] = new Production(prod);
        			System.out.println("Prod already exists, so creating new prod with different child for alternate parse");
        			return p;
        		}*/
        	Production p = new Production(prod);
            p.parent = this;
            children[n] = p;
            
            return p;
        }
	
	/**This returns true if the given production matches this one.
	 *
	 * The comparison checks for identical productions only, down to the
	 * placement of the dot.
	 * 
	 * @param p The production to compare to.
	 */
	public final boolean equals(Production p)
	{
		if(left != p.left || right.length != p.right.length || dot != p.dot || start != p.start)
			return false;
		for(int i=0;i<right.length;i++)
			if(right[i] != p.right[i])
				return false;
		return true;
	}

	/**Easy print.
	 */
	public void print()
	{
		System.out.println(this.toString());
	}

	/**Standard toString human-readable output.
	 * Format:
	 * startpos  left-- right1 . right2
	 * with the dot moving about accordingly.
	 */
	public String toString()
	{
		String ret = start+"\t"+left+"->";
                for(int i=0;i<right.length;i++)
                {
                        if(i==dot)
                                ret = ret + "\t.";
                        ret = ret + "\t" + right[i];
                }
                if(dot == right.length)
                        ret = ret + "\t.";
                return ret;
	}

	//this to check if children exists
	public boolean childExists(){
		
		//System.out.println("ChildExists begins!");
		if (children!=null){
			//System.out.println("Child Size:"+ children.length);
			/*for(int i = 0; i< children.length; i++){
				if(children[i]==null)
					/*try{
					System.out.print("\t"+i+" = "+children[i]+", ");
				}
				catch(NullPointerException e){
					System.out.print("\t"+i+" = Null, ");
				}
			}*/
			return true;
		}
		else{
			return false;
		}
	}
	
	public int getChildSize(){
		System.out.println("Childsize begins for "+this.toString());
		int count = 0;
				for(int i =0; i<right.length; i++){
					if(this.children!=null){
						//Debug
						//
						if(this.children[i].right!=null){
							int temp = this.children[i].getChildSize();
							System.out.println("\tIncrementing count("+count+") for "+this.toString()+" by "+temp);
							count += temp;
						}
					}
					else{
						for(int j=0; j< right.length; j++){
							//Debug

							//System.out.print(right[j]);
							count += 1;
						}
					}
				}
				System.out.println("Returning "+count+" for "+this.toString());
				return count;
	}
	
	/**This prints a parse, a chain of productions.
	 * TODO: Write this function!
	 */
	public void recursivePrint()
	{
		System.out.print(left.toUpperCase()+" [ ");
		for(int i =0; i<right.length; i++){
			if(this.children!=null){
				this.children[i].recursivePrint();
			}
			else{
				for(int j=0; j< right.length; j++){
					System.out.print(right[j]);
				}
			}
		}
		System.out.print(" ] ");
	}
}
