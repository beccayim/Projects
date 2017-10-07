import java.util.*;

public class IndexEntry implements Comparable<IndexEntry>{
// Entries in the PageIndex: Represents a single term (word) found and
// tracks the pages that have that word associated with them.
// IndexEntries are created by specificying a term in the constructor
// and then adding pages to the entry.  The class implements
// Comparable so it can be stored in a set such as ArraySet.

  protected String term;
  // A search term which is found on some pages. Examples: "computer"
  // "acrobat" or "electronica".  Terms are kepty in lower case only.

  protected ArraySet<String> pages;
  // The set of pages which contain the given term

  // Create an empty IndexEntry associated with the given term
  public IndexEntry(String term){
    this.term = term.toLowerCase(); //make term lower cased
    this.pages = new ArraySet<String>(); // make pages empty
  }

  // Return the term associated with this entry
  public String getTerm(){
    return term;
  }
  
  //retuurns list of pages
  public List<String> getPages(){
    List<String> pagesList = pages.asList();
    return pagesList;
  }

  // Return true if the the given page is already present in this
  // entry and false otherwise.
  public boolean containsPage(String pageFileName){
    // if contains is true then page is present
    if (pages.contains(pageFileName) == true){
      return true;
    }
    else{
      return false;
    }
  }

  // Add the given page to this entry returning true if it was not
  // present and is therefore a new addition; return false if it is
  // already present
  public boolean addPage(String filename){
    if (containsPage(filename) == true){
      return false;
    }
    //if entry doesn't have the page then add it to list pages
    else{
      pages.add(filename);
      return true;
    }
  }
  
  // Compare the entry to that entry. The comparison is based entirely
  // on the term field which should use the built-in String comparison
  // methods to generate the difference.
  public int compareTo(IndexEntry that){
    return this.getTerm().compareTo(that.getTerm());
  }
  
  // Return whether the other object is equal to this IndexEntry.
  public boolean equals(Object other){
    //check if object is even an IndexEntry
    if (!(other instanceof IndexEntry)){
      return false;
    }
    // if it is then cast it to be one
    IndexEntry temp = (IndexEntry) other;
    // check if terms are the same
    return this.getTerm().equals(temp.getTerm());
  }
  
  // Return a string representation of this entry. 
  public String toString(){
    List<String> pagesList = getPages(); //List rep of pages
    String s = "";//start off with empty string
    s += "@ " + term + "\n"; //add heading
    //for each page, add it and new line
    for (String page : pagesList){
      s += page + "\n";
    }
    return s;
  }
}