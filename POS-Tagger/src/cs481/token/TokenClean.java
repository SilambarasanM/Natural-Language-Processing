package cs481.token;

import cs481.util.*;

import java.io.*;
import java.util.*;

import org.w3c.dom.*;

/**
 * Clean up a token XML file by removing extraneous whitespace.
 * Run from the commandline.
 *
 * @author Sterling Stuart Stein
 */
public class TokenClean
{
   /**
    * The commandline frontend.
    *
    * @param argv Unused.
    */
   public static void main(String[] argv) throws Exception
   {
      Vector doc = Token.readXML(System.in);
      Token.writeXML(doc, System.out);
   }
}