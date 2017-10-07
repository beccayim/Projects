import java.util.*;

public abstract class Crawler {
// Abstract class to crawl linked pages. Descendent crawlers should
// override the crawl(page) method to initiate a crawllestof linked
// pages. The class is intended only to work for locally stored files
// for whic validPageLink(page) should return true while non-local and
// web links will return false. This class makes use of the ArraySet
// class.

  protected ArraySet<String> foundPages;
  // Sets of pages that have been found or have been skipped due to
  // their being invalid or non-existent.

  protected ArraySet<String> skippedPages;

  // Public constructor that creates an empty crawler.
  public Crawler(){
    foundPages = new ArraySet<String>();
    skippedPages = new ArraySet<String>();
  }

  // Initiate a crawl on the given page.  Child classes should
  // override this.
  public abstract void crawl(String pageFileName);
  
  // Return the unique pages that have been found so far and are
  // valid.
  public List<String> foundPagesList(){
    return foundPages.asList();
  }

  // Return the unique pages that have been skipped so far. 
  public List<String> skippedPagesList(){
    return skippedPages.asList();
  }

  // Return a string of pages that have been found so far.
  public String foundPagesString(){
    String s = "";
    //turn the arrayset into a list so that it can be iterated
    List<String> pages = foundPages.asList();
    for (String page: pages){
      s += page + "\n";
    }
    return s;
  }

  // Return a string of pages that have been found so far. 
  public String skippedPagesString(){
    String s = "";
    //turn the arrayset into a list so that it can be iterated
    List<String> pages = skippedPages.asList();
    for (String page: pages){
      s += page + "\n";
    }
    return s;
  }

  // Return true if the given pageFileName is valid and false
  // otherwise. Valid pages
  public static boolean validPageLink(String pageFileName){
    //a bunch of strings that are invalid
    String s1 = "http://";
    String s2 = "https://";
    String s3 = "file://";
    String s4 = "https:/";
    //strings that are valid
    String s5 = ".html";
    String s6 = ".HTML";
   
    //checks if the name has the invalid strings
    if (pageFileName.contains(s1) || pageFileName.contains(s2) || pageFileName.contains(s3) || pageFileName.contains(s4)){
      return false;
    }
    //checks if the name has the valid strings
    if (pageFileName.contains(s5) || pageFileName.contains(s6)){
      return true;
    }
    //otherwise it's probably wrong
    else{
      return false;
    }
  }
}