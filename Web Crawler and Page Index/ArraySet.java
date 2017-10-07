import java.util.*;

public class ArraySet<T extends Comparable<T>>{
// A simple implementation of a Set backed by an array.  As a Set,
// instances track *unique* items so that no duplicates occur.  This
// implementation should keep the underlying array sorted and use
// binary search to quickly identify if items are present or absent to
// maintain uniqueness.  To maintain this, items that go into the set
// must implement the Comparable interface so that they have a
// compareTo(..) method and are compatible with library search and
// sort methods.

  private List<T> set; //AL that manages the underlying array size auto
  
  // creates an empty arrayset
  public ArraySet(){
    set = new ArrayList<T>();
  }
  
  //Return the size of the set
  public int size(){
    return set.size();
  }   

  //Return the contents of the set as a list.
  public List<T> asList(){
    return set;
  }

  //Check if the set contains this query item.
  public boolean contains(T query){
    int some = Collections.binarySearch(set, query); //holds integer indicating position
    // if it's positive that means it's in the set
    if (some >= 0){
      return true;
    }
    // if it's negative that means it's not in the set
    else{
      return false;
    }
  }
  
  //Adds the item to the set if it's unique and if it's not null.
  public boolean add(T item){
    // checks if item is null, will throw runtimeexception
    if (item == null){
      throw new RuntimeException("It's a null");
    }
     
    //assumes that method gives insertion point so I change it to normal index of where the item would go
    int some = (Collections.binarySearch(set, item) * -1) -1; 
    // if the set contains the item then don't add it, return false
    if (contains(item) == true){
      return false;
    }
    //otherwise, add the item at that index and return true
    else{
      set.add(some, item);
      return true;
    }
  }
  
  //Retrieve an item in the set that is equal to the query item.
  public T get(T query){
    int some = Collections.binarySearch(set, query);
    // if some is positive integer that means it is inside set
    if (some >= 0){
      return set.get(some);
    }
    //if item isn't found, return null
    else{
      return null;
    }
  }
  
  // Return a string representation of the set and its contents
  public String toString(){
    return set.toString();
  }

}