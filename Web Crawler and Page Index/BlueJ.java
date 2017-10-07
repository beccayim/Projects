import org.jsoup.nodes.*;
import org.jsoup.*;
import org.jsoup.select.*;
import java.io.*;
import java.util.*;
public class PageIndex{
// Index of terms found on pages and the associated pages on which
// they were found.  An index is created empty but can use Crawler
// that has discoverd pages to add terms and pages to the index.
// PageIndex provides basic query facilities to look up which pages
// contain one or more words.
     
     // The set of IndexEntries which track search terms found on pages
     // along with the pages on which they are found.  Entries are kept
     // in a set as they should be unique.
     protected ArraySet<IndexEntry> entries;
     
     // Create an empty PageIndex        
     public PageIndex(){
          entries = new ArraySet<IndexEntry>();
     }
     
     // Return the number of entries in the index which is the number of
     // terms that have been added with at least one page associated with
     // them.        
     public int size(){
          return entries.size();
     }
     
     // Return a string representation of the indexed terms and
     // pages. The format of the string is
     //
     // INDEX: #### entries
     // --------------------
     // @ entry1
     // entry1-file1
     // entry1-file2
     // entry1-file3
     // ...
     // @ entry2
     // entry2-file1
     // entry2-file2
     // entry2-file3
     // ...
     //
     // Which creates a header and then iterates through all IndexEntries
     // appending their toString().        
     public String toString(){        
          String a = "INDEX: " + entries .size() +" entries" + "\n" + "--------------------\n";
          for(int i =0; i <entries .size(); i++){
               a = a +  entries .fetch(i).toString();
          }
          
          return a;        
     }
     
     // Determine if the term given is valid. Valid terms do not appear
     // in the sorted array Util.STOP_WORDS which are uninformative
     // words.  Use binary search to efficiently determine if the given
     // term is in STOP_WORDS; return false if it is and true otherwise.
     //
     // Addition: Valid terms should also be longer than 1 character.        
     public boolean validTerm(String term){   
          if(term.length()<=1){
               return false;
          }        
          
          int s = Arrays.binarySearch(Util.STOP_WORDS, term.toLowerCase());
          if(s >= 0){
               return false;
          }
          return true;
     }
     
     // Return true if the IndexContains the given term and some pages
     // associated with it and false otherwise.        
     public boolean containsTerm(String term){                
          for(int i =0; i <entries.size(); i++){
               if(entries.fetch(i).getTerm().equals(term)){                        
                    if(entries.fetch(i).getPages().size() !=0){
                         return true;
                    }                                
               }               
          }
          return false;        
     }
     
     // Return a list of the pages associated with the given term. If
     // there are no pages associated with the given term, return and
     // empty list.        
     public List<String> getPagesWithTerm(String term){
          List<String> list = new ArrayList<String>();
          for(int i =0; i <entries.size(); i++){
               if(entries.fetch(i).getTerm().equals(term)){                        
                    return entries.fetch(i).getPages();
               }               
          }
          return list;
     }
     
     
     // Add the given term found in the given page to the index.  If the
     // term is not valid as per the validTerm() method, do not add it
     // and return false.  Valid terms should be added along with the
     // page on which they occurred to an IndexEntry.  Return true if the
     // term is new to the index or if the term exists but the page is
     // new for that term.  Otherwise return false.
     public boolean addTermAndPage(String term, String page){
          if(!validTerm(term)){
               return false;
          }
          IndexEntry ie = new IndexEntry(term);
          if(!containsTerm(term)){
               
               ie.addPage(page);
               entries.add(ie);
               return true;                        
          }else{
               if(getPagesWithTerm(term).isEmpty() || !getPagesWithTerm(term).contains(page)){                                      
                    
                    System.out.println( "entries = " + entries);
                    entries.get(ie).addPage(page);
                    System.out.println( "entries.get(ie) = " + entries.get(ie));
                    return true;                 
               }else{                        
                    return false;
               }                                        
          }
     }
     // Optional main method for your own testing        
     public static void main(String args[]){
          PageIndex index = new PageIndex();
          //System.out.println(index.containsTerm("java"));
          index.addTermAndPage("java","A.html");
          //System.out.println(index.containsTerm("java"));                
          //System.out.println("getPagesWithTerm(term).isEmpty() " + index.getPagesWithTerm("java").isEmpty());
          //System.out.println("getPagesWithTerm(term).contains(page) " + index.getPagesWithTerm("java").contains("A.html"));
          
          index.addTermAndPage("java","http://docs.oracle.com/javase/8/docs/technotes/guides/language/index.html");
          System.out.println(index);
          
          //index.addTermAndPage("c++","C.html");
          //index.addTermAndPage("rlang","R.html");
          //index.addTermAndPage("dlang","d.html");
          //index.addTermAndPage("dlang","https://dlang.org");
          //index.addTermAndPage("rlang","https://www.r-project.org");
          //index.addTermAndPage("java","https://en.wikipedia.org/wiki/Java_(programming_language)");
     }
     
     // For each page in the given crawler's foundPageList(), open and
     // parse the page using the JSoup library.  Examine the body text of
     // the page which is a string.  Use a Scanner on this string and set
     // the delimiter to
     // 
     //    scan.useDelimiter("(\\p{Space}|\\p{Punct}|\\xA0)+");
     // 
     // which will parse through words on the page skipping most
     // punctuation. Add this page to the entries in the index associated
     // with each term in the body. Individual pages may generate
     // exceptions while reading which the this method should ignore and
     // continue to the next page. This may happen for the pages which
     // use HTML frames.        
     public void addCrawledPages(Crawler crawler){               
          for(String s :  crawler.foundPagesList()){
               try{                        
                    File input = new File(s);
                    Document doc = Jsoup.parse(input, "UTF-8"); 
                    String text = doc.body().text();
                    Scanner scan = new Scanner(text);
                    scan.useDelimiter("(\\p{Space}|\\p{Punct}|\\xA0)+");
                    while(scan.hasNext()){
                         String a = scan.next();
                         if(validTerm(a)){
                              addTermAndPage(a,s);                                        
                         }                              
                    }            
               }catch(Exception e){
                    continue;
               }               
          }        
     }
     
     // Find the intersection (common elements) of x and y. Assume that x
     // and y are sorted.  Use this fact to efficiently build up a list
     // of the common elements with a single loop through the lists x and
     // y.        
     public static List<String> intersectionOfSorted(List<String> x, List<String> y){              
          int i = 0;
          int j = 0;
          List<String> l = new ArrayList<String>();
          while(i<x.size() && j<y.size()){
               if(x.get(i).compareTo(y.get(j)) == 0){      
                    l.add(x.get(i));
                    i++;
                    j++;
               }else if(x.get(i).compareTo(y.get(j)) < 0 ){
                    i++;                        
               }else{
                    j++;
               }                      
          }
          return l;
     }
     
     // Return a list of pages in the index which match the given
     // query. The query may be several space-separated words such as
     // "robotics artificial intelligence".  These should be separated
     // and used to retrieve lists of pages matching the words. Make use
     // of the insertionOfSorted(x,y) method to efficiently combine lists
     // of pages repeatedly in a loop to produce the end results.        
     /**
      public List<String> query(String queryString){
      
      String[] query = queryString.split(" ");
      List<String> result = getPagesWithTerm(query[0]);
      for(String s : query){
      result = PageIndex.intersectionOfSorted(result, getPagesWithTerm(s));                        
      }
      return result;               
      }**/
     
     
     
     
}