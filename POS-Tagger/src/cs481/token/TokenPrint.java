package cs481.token;

import cs481.util.*;

import java.io.*;
import java.util.*;

import org.w3c.dom.*;

/**
 * Print out Token XML as lines suitable for diff.
 * Run from the commandline.
 *
 * @author Sterling Stuart Stein
 */
public class TokenPrint
{
   /**
    * Print out Token XML as lines suitable for diff.
    *
    * @param argv The name of the XML file.
    */
   public static void main(String[] argv) throws Exception
   {
      if(argv.length != 1)
      {
         System.err.println("Error: wrong number of arguments");
         System.err.println("Format:  java cs481.token.TokenPrint <XML file>");
         System.err.println("Example: java cs481.token.TokenPrint tokens.xml");
         System.exit(1);
      }

      Object[] t = TokenDiff.tokenArray(Token.readXML(
               new BufferedInputStream(new FileInputStream(argv[0]))));

      for(int i = 0; i < t.length; i++)
      {
         System.out.println(t[i]);
      }
   }
}