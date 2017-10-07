import org.jsoup.nodes.*;
import org.jsoup.*;
import org.jsoup.select.*;
import java.util.*;
import java.io.*;

public class IterativeCrawler extends Crawler {
// An implementation of a crawler which does not use recursion and
// instead uses internal storage to track the pages that have yet to
// be visited.

  protected ArraySet<String> pendingPages;
  // A list of pages that remain to be visited. As a single page is
  // visited, any valid links that are found are added to this list to
  // be visited later.  This list should only contain valid, existing
  // pages which can be visited and have not yet been visited.

  // Create an empty crawler.
  public IterativeCrawler(){
    foundPages = new ArraySet<String>();
    skippedPages = new ArraySet<String>();
    pendingPages = new ArraySet<String>();
  }

  // Master crawl method which will start at the given page and visit
  // all reachable pages.
  public void crawl(String pageFileName){
    //add to pendingPages and then crawlingRemaining.
    pendingPages.add(pageFileName);
    crawlRemaining();    
  }
  
  // Enter a loop that crawls individual pages until there are no
  // pending pages remaining.  
  public void crawlRemaining(){
    //while there are still pages in pendingPages, keep crawling.
    while (pendingPages.size() > 0){
      crawlNextPage();
    }
  }
  
  // Add the given page to the list of of pending pages to be
  // visited.
  public void addPendingPage(String pageFileName){
    pendingPages.add(pageFileName); 
  }

  // Return the number of pages remaining to visit.
  public int pendingPagesSize(){
    return pendingPages.size();
  }

  // Return a string with each pending page to visit on its own line.
  public String pendingPagesString(){
    String s = "";
    //turn the arrayset into a list so that it can be iterated
    List<String> pages = pendingPages.asList();
    for (String page: pages){
      s += page + "\n";
    }
    return s;
  }

  // Crawl a single page which is retrieved and removed from the list
  // of pending pages.
  public void crawlNextPage(){
    List<String> pageList = pendingPages.asList(); //turn the arrayset into a list
    String pageFileName = pageList.remove(pendingPagesSize() - 1);//get the last filename in pendingPages
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
        // each one is either added to pendingPages or skippedPages
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
            //else add that page to pendingPages
            else{
              pendingPages.add(file);
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