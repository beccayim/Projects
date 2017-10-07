import java.util.*;
import java.io.*;
import org.jsoup.nodes.*;
import org.jsoup.*;
import org.jsoup.select.*;

public class PageIndex{
// Index of terms found on pages and the associated pages on which
// they were found.  An index is created empty but can use Crawler
// that has discoverd pages to add terms and pages to the index.
// PageIndex provides basic query facilities to look up which pages
// contain one or more words.

  protected ArraySet<IndexEntry> entries;
  // The set of IndexEntries which track search terms found on pages
  // along with the pages on which they are found.  Entries are kept
  // in a set as they should be unique.

  // Create an empty PageIndex
  public PageIndex(){
    entries = new ArraySet<IndexEntry>();
  }
  
  // Return the number of entries in the index 
  public int size(){
    return entries.size();
  }

  // Return a string representation of the indexed terms and
  // pages.
  public String toString(){
    String s = ""; //start off with empty string
    s += "INDEX: " + entries.size() + " entries\n"; //first line of string
    s += "--------------------\n"; //second line of string
    List<IndexEntry> indexEntryList = entries.asList(); // turn the arrayset into a list so it can be iterated
    //for each index entry, add the term and all its pages
    for (IndexEntry entry: indexEntryList){
      s += "@ " + entry.getTerm() + "\n";
      List<String> pagesList = entry.getPages();
      for (String page: pagesList){
        s += page + "\n";
      }
    }
    return s;
  }

  // Determine if the term given is valid.
  public boolean validTerm(String term){
    String word = term.toLowerCase(); //change word to all lower case
    int some = Arrays.binarySearch(Util.STOP_WORDS, word); 
    //if int is negative (not in the list of stop words) and not a single char then it's valid
    if (some < 0 && word.length() > 1){
      return true;
    }
    //otherwise it's invalid
    else{
      return false;
    }
  }

  // Return true if the IndexContains the given term and some pages
  // associated with it and false otherwise.
  public boolean containsTerm(String term){
    //make a list of entries so it can be iterated
    List<IndexEntry> indexEntryList = entries.asList();
    for (IndexEntry entry: indexEntryList){
      // checks if term matches up with any of the terms in the entries and has associated pages
      if (entry.getTerm().equals(term) && entry.getPages().size() != 0){
        return true;
      }
    }
    //if no match term is found then return false
    return false;    
  }
  
  // Return a list of the pages associated with the given term.
  public List<String> getPagesWithTerm(String term){
    //make an empty list of pages
    List<String> pagesWithTerm = new ArrayList<String>();
    //turn the entries into a list
    List<IndexEntry> indexEntryList = entries.asList();
    //find the entry in entries with the term and get its pages
    for (IndexEntry entry: indexEntryList){
      if (entry.getTerm().equals(term)){
        pagesWithTerm = entry.getPages();
      }
    }
    //return pages
    return pagesWithTerm;
  }
  
  // Add the given term found in the given page to the index.
  public boolean addTermAndPage(String term, String page){
    term = term.toLowerCase(); //make sure word is lower cased
    // returns false if term isn't valid
    if (validTerm(term) == false){
      return false;
    }
    else{
      // if page doesn't contain the term
      if (containsTerm(term) == false){
        // need to make a new indexentry with that term 
        IndexEntry temp = new IndexEntry(term);
        // add pages assoicated to that term
        temp.addPage(page);
        // add new indexentry to pageindex
        entries.add(temp);
        return true;
      }
      // if page already has the term
      else {
        List<IndexEntry> indexEntryList = entries.asList();
        // find the indexentry of given term
        for (IndexEntry entry: indexEntryList){
          if (entry.getTerm().equals(term)){
            // if the indexentry doesn't already have the page, then add the page
            if(entry.containsPage(page) == false){
              entry.addPage(page);
              return true;
            }
          }   
        }
        return false;
      } 
    }
  }
  
  // For each page in the given crawler's foundPageList(), find all the valid terms
  // and pages and add to pageindex
  public void addCrawledPages(Crawler crawler){
    try{
      // get the list of foundpages
      List<String> foundPages = crawler.foundPagesList();
      // for every page in the list, parse through it, scan through just the words
      for (String page : foundPages){
        File input = new File(page);
        Document doc = Jsoup.parse(input, "UTF-8");
        String text = doc.body().text();
        Scanner scan = new Scanner(text);
        scan.useDelimiter("(\\p{Space}|\\p{Punct}|\\xA0)+");
        // while there's a next word, check if the word is valid and if it is, add it and the page to pageindex
        while (scan.hasNext()){
          String s = scan.next();
          if (validTerm(s) == true){
            addTermAndPage(s, page);
          }
        }   
      }
    }
    //catch null pointer exception
    catch (NullPointerException e){
      System.out.print("Nullpointerexception");
    }
    //catch ioexception
    catch (IOException e){
      System.out.println("bad");
    }
  }

  // Find the intersection (common elements) of x and y.
  public static List<String> intersectionOfSorted(List<String> x, List<String> y){
    List<String> common = new ArrayList<String>(); //list that will hold the common elements
    int i = 0; // index iterating through x
    int j = 0; // index iterating through y
    // while there are still elements left in both lsits
    while (i < x.size() && j < y.size()){
      // check if the elements are the same starting at the beginning
      // if they are then add that element to common and increment both indexes
      if (x.get(i).equals(y.get(j))){ // or compareTo =0 
        common.add(x.get(i));
        i++;
        j++;
      }
      //otherwise check which one is bigger, is x is bigger then move further in y
      else if ((x.get(i).compareTo(y.get(j))) > 0){
        j++; 
      }
      else{ // otherwise move further in x
        i++;
      }
    }
    return common;
  }

  // Return a list of pages in the index which match the given
  // query.
  public List<String> query(String queryString){
    // create a list that will hold pages of the first term in string
    List<String> temp1 = new ArrayList<String>();
    String[] terms = queryString.split(" "); //split the string into an array of terms
    temp1 = getPagesWithTerm(terms[0]); //this is now the pages of the first term
    //for loop first checks the commonality of the first terms pages with first term pages which gives first term pages
    // then for each next term, it'll check the intersection of the previous new list and compare with
    // page of next term.
    for (String term: terms){
      List<String> temp2 = getPagesWithTerm(term);
      List<String> common = intersectionOfSorted(temp1, temp2);
      temp1 = common;
    }
    return temp1;  
  }

}