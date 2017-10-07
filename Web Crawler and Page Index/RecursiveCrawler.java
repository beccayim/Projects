import org.jsoup.nodes.*;
import org.jsoup.*;
import org.jsoup.select.*;
import java.util.*;
import java.io.*;

public class RecursiveCrawler extends Crawler{
  //Create an empty crawler
  public RecursiveCrawler(){
    foundPages = new ArraySet<String>();
    skippedPages = new ArraySet<String>();
  }
 
  //Find all the pages linked in a collection and put it in either foundPages or skippedPages
  public void crawl(String pageFileName){
    try{
      /* if this filename is valid then add it to foundPages.
      * then make it a file, create a doc from the page, and extract a list of links from the doc
      */
      if (validPageLink(pageFileName) == true){
        foundPages.add(pageFileName);
        File input = new File(pageFileName);
        Document doc = Jsoup.parse(input, "UTF-8");
        List<Element> links = doc.select("a[href]");
        //for every item in the links, get the LINKHREF and check if it's a valid page
        for (Element element: links){
          String linkedPage = element.attr("href");
          // if it's not a valid page then add to skippedPages and continue
          if (validPageLink(linkedPage) == false){
            skippedPages.add(linkedPage);
            continue;
          }
          //change name of file 
          String file = Util.relativeFileName(pageFileName, linkedPage);
          File temp = new File(file); //turn that filename into a file
    
          // if file is not readable then add to skippedPages
          if (temp.canRead() == false){
            skippedPages.add(file);
          }
          //Otherwise, if it is a valid page
          else {
            //Check and ignore files that have already been read
            if (foundPages.contains(file) == true || skippedPages.contains(linkedPage) == true){
              continue;
            }
            //crawl into that new page 
            else{
              crawl(file);
            }
          }
        }
      }
      //if filename isn't valid then add to skippedPages
      else{
        skippedPages.add(pageFileName);
      }
    }
    //catch exception, probably if filename can't be turned into a file
    catch(Exception e){
      System.out.printf("Could not parse %s\n", pageFileName);
      return;
    }
  }
}