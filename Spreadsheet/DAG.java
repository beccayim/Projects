import java.util.*;
public class DAG{
// Model a Directed Acyclic Graph (DAG) which allows nodes (vertices)
// to be specified by name as strings and added to the DAG by
// specifiying their upstream dependencies as a set of string IDs.
// Attempting to introduce a cycle causes an exception to be thrown.

  protected Map<String, Set<String>> downstreamLinks;

  protected Map<String, Set<String>> upstreamLinks;
  // Maps which allow the downstream and upstream links for a given
  // node to be retrieved.

  // Construct an empty DAG
  public DAG(){
    this.downstreamLinks = new HashMap<String, Set<String>>();
    this.upstreamLinks = new HashMap<String, Set<String>>();
  }
  
  // Produce a string representaton of the DAG which shows the
  // upstream and downstream links in the graph.
  public String toString(){ 
    // use a string builder and add first line
    StringBuilder s = new StringBuilder();
    s.append("Upstream Links:\n");
    // iterate through all the ids in upstream
    for (String cell: upstreamLinks.keySet()){
      Set<String> temp = upstreamLinks.get(cell);
      // make sure it's not empty set
      if (!temp.isEmpty()){
        // add in certain format
        s.append(String.format("%4s : [", cell));
        int count = temp.size();
        int i = 1; // helps keep track of last elem
        // for each id upstream of that cell id
        for (String id : temp){
          if (i < count){ // to add the last elem without the comma
            s.append(id + ", ");
            i++;
          }
          else{
            s.append(id + "]\n");
          }
        }
      }
    }
    // same thing but for downstreamLinks
    s.append("Downstream Links:\n");
    for (String cell: downstreamLinks.keySet()){
      Set<String> temp = downstreamLinks.get(cell);
      if (!temp.isEmpty()){
        s.append(String.format("%4s : [", cell));
        int count = temp.size();
        int i = 1;
        for (String id : temp){
          if (i < count){ // to add the last elem without the comma
            s.append(id + ", ");
            i++;
          }
          else{
            s.append(id + "]\n");
          }
        }
      }
    }
    // return as a string
    return s.toString(); 
  }

  // Return the upstream links associated with the given ID.  If there
  // are no links associated with ID, return the empty set.
  //
  // TARGET COMPLEXITY: O(1)
  public Set<String> getUpstreamLinks(String id){
    Set<String> stream = new HashSet<String>();
    // if upstreamLinks has the upstreamlinks of this id then update stream
    if (upstreamLinks.containsKey(id)){
      stream = upstreamLinks.get(id);
    }
    // otherwise it has no stream return empty set
    return stream;
  }
  
  // Return the downstream links associated with the given ID.  If
  // there are no links associated with ID, return the empty set.
  //
  // TARGET COMPLEXITY: O(1)
  public Set<String> getDownstreamLinks(String id){
    // same but for downstream
    Set<String> stream = new HashSet<String>();
    if (downstreamLinks.containsKey(id)){
      stream = downstreamLinks.get(id);
    }
    return stream;
  }

  public static class CycleException extends RuntimeException{
  // Class representing a cycle that is detected on adding to the
  // DAG. Raised in checkForCycles(..) and add(..).

    public CycleException(String msg){
      super(msg);
    }
    // Construct an exception with the given error message

  }

  // Add a node to the DAG with the provided set of upstream links.
  // TARGET RUNTIME COMPLEXITY: O(N + L)
  // MEMORY OVERHEAD: O(P)
  //   N : number of nodes in the DAG
  //   L : number of upstream links in the DAG
  //   P : longest path in the DAG starting from node id
  public void add(String id, Set<String> upstreamIDs){
    // Retrieve the current upstream links associated with id and save them in a local variable
    Set<String> currUpStream = new HashSet<String>();
    if (upstreamLinks.containsKey(id)){
      Set<String> temp = upstreamLinks.get(id);
      // copies it into local var
      for(String p : temp){
        currUpStream.add(p);
      }
    }
    remove(id); //Remove id from the DAG
    upstreamLinks.put(id, upstreamIDs); //change upstreamlink associated with id 
    
    //For each of the nodes in newUpstreamLinks, add id to its downstream links
    for(String s : upstreamIDs){
      // if downstream already has the key then just add the id to the already existing set
      if(downstreamLinks.containsKey(s)){
        downstreamLinks.get(s).add(id);
      }     
      // if downstream doesn't contain the key then add a new set with the just the id
      else{
        Set<String> temp = new HashSet<String>();
        temp.add(id);
        downstreamLinks.put(s, temp);
       }
    } 
    
    // Make path that holds the new added id that will be passed into cycles method
    List<String> path = new ArrayList<String>();
    path.add(id);
    // if there is a cycle then remove the id (undo it) and add the id again with the old up stream
    if (checkForCycles(upstreamLinks, path) == true){
      remove(id);
      add(id, currUpStream);
      // Make error message for cycle exception
      StringBuilder s = new StringBuilder();
      s.append("[");
      int i = 1; //help keep track of where last elem is
      for (String elem : path){
        if (i < path.size()){
          s.append(elem + ", ");
          i++;
        }
        else{
          s.append(elem + "]");
        }
      }
      throw new CycleException(s.toString());
    }
  }
  
  // Determine if there is a cycle in the graph represented in the
  // links map.
  public static boolean checkForCycles(Map<String, Set<String>> links, List<String> curPath){
    String lastNode = curPath.get(curPath.size()-1); 
    Set<String> neighbors = links.get(lastNode);
    // if there are no neighbors then no cycle
    if(neighbors == null || neighbors.size() == 0){
      return false;
    }
    else{
      // for each neighbor, add it to the path and check if it's already in the path
      // also check if the neighbor has neighbors that are in its path
      for(String s : neighbors){
        curPath.add(curPath.size(), s);
        if(curPath.get(0).equals(s)){
          return true;
        }
        boolean result = checkForCycles(links, curPath);
        if(result == true){
          return true;
        }
        // remove neighbor from path once you've checked it for cycles
        curPath.remove(curPath.size()-1);
      }
    }
    // no cycles were found
    return false;
  }
  
  // Remove the given id by eliminating it from the downstream links
  // of other ids and eliminating its upstream links. 
  // TARGET COMPLEXITY: O(L_i)
  //   L_i : number of upstream links node id has
  public void remove(String id){
    // make sure id is in upstream
    if(upstreamLinks.containsKey(id)){
      Set<String> stream = upstreamLinks.get(id);
      // eliminates id from all the downstreams it was connected to
      for(String s : stream){
        downstreamLinks.get(s).remove(id);
      }
    }
    // remove from the upstream
    upstreamLinks.remove(id);
  }
}
