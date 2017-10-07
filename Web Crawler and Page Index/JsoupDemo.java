// Copy these imports to enable JSoup to work
import org.jsoup.nodes.*;
import org.jsoup.*;
import org.jsoup.select.*;
import java.io.*;
import java.util.*;

public class JsoupDemo{
  public static void main(String args[]) {
    try{
      // Select a file to parse, create a File from it
      File input = new File("crawls/small-site/start.html");
      // File input = new File(args[0]);

      // Parse the HTML file using Jsoup, a Document results
      Document doc = Jsoup.parse(input, "UTF-8"); 

      // Use the select(..) method to select all links (<a href=.. >
      // tags) in the HTML file.  It returns an ArrayList of Elements.
      List<Element> links = doc.select("a[href]");
      System.out.println("LINKS");
      for(Element element : links){
        String linkText = element.text();         // Extract the text in the link
        String linkedPage = element.attr("href"); // Extract the linked page
        System.out.printf("'%s'\n-->%s\n\n",linkText,linkedPage);
      }

      // Extract the text of the HTML page which removes all
      // formatting, links, images, etc. and returns as a string.
      String text = doc.body().text();

      // Open up a scanner and scan through the body text using a
      // facncy delimeter to ignore punctuation and spaces.
      Scanner scan = new Scanner(text); //, "UTF-8");
      scan.useDelimiter("(\\p{Space}|\\p{Punct}|\\xA0)+");
      System.out.println("PAGE WORDS");
      while(scan.hasNext()){
        System.out.println(scan.next());
      }
    }
    catch(Exception e){
      System.out.printf("Could not parse %s\n",args[0]);
      return;
    }

  }
}
