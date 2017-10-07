import java.util.*;
public class Cell {
// Spreadsheet Cells can be one of three different kinds:

  protected String contents;   
  // The actual contents of the cell.

  protected Double numberValue;  
  // Value if cell contains a number or value of evaluated formula. 
  
  protected FNode formulaTree;    
  // Root of the parsed formula tree if kind() is "formula". Null o/w.
  private String kind; // either string, formula, or number
  private boolean error; // indicates if formula is error
 
  // Factory method to create cells with the given contents linked to
  // the given spreadsheet.
  public static Cell make(String contents){
    //instantiate internal fields
    Cell c =  new Cell();
    c.error = false;
    c.contents = contents.trim();
    c.numberValue = 0.0;
    
    // if contents represents a formula, change the fields accordingly
    if (contents.contains("=")){
      c.kind = "formula";
      c.error = true;
      c.numberValue = null;
      c.formulaTree = FNode.parseFormulaString(contents);
    }
    else{
      // if there isn't an exception then it's a number, otherwise it's a string
      // changes kind and numberValue accordingly
      try{
        c.numberValue = Double.parseDouble(contents);
        c.kind = "number";
      }catch(Exception e){
        c.kind = "string";
        c.numberValue = null;
      }
    }
    return c;
  }
  
  // Return the kind of the cell which is one of "string", "number",
  // or "formula".
  public String kind(){
    return kind;
  }

  // Return the raw contents of the cell.
  // Runtime Complexity: O(1)
  public String contents(){
    return contents;
  }

  // Returns whether the cell is ansently in an error state.
  public boolean isError(){ //change
    return error;
  }

  // Produce a string to display the contents of the cell.
  // Runtime Complexity: O(1)
  public String displayString(){
    // returns original content for string
    if(kind.equals("string")){
      return contents;
    }
    // if it's in error state then return error
    if(isError() == true){
      return "ERROR";
    }
    // whether or not it's formula or number return numeric value
    else{
      return String.format("%.1f", numberValue);
    }
  }

  // Return the numeric value of this cell.
  // Runtime Complexity: O(1)
  public Double numberValue(){
    // if formula has error then return null
    if(error == true){
      return null;
    }
    // will return null even for strings, or the numberic value otherwise
    return numberValue;
  }

  // Update the value of the cell value. 
  // Runtime Complexity: 
  //   O(1) for "number" and "string" cells
  //   O(T) for "formula" nodes where T is the number of nodes in the
  //        formula tree
  public void updateValue(Map<String,Cell> cellMap){
    // change error to false bc we're calculating the value
    error = false;
    // if it's a formula then call evalFormulaTree to get value
    if(kind.equals("formula")){ 
      try{
        numberValue = evalFormulaTree(formulaTree, cellMap);
      }
      // if value can't be calculated then it's still in error state
      catch(EvalFormulaException e){
        error = true;
      }
    }
  }

  public static class EvalFormulaException extends RuntimeException{
  // A simple class to reflect problems evaluating a formula tree.
    public EvalFormulaException(String msg){
      super(msg);
    }
    // Construct an exception with the specified error message
  }

  // Recursively evaluate the formula tree formulaTreeed at the given
  // node.
  // Runtime Complexity: O(T) 
  //   T: the number of nodes in the formula tree
  public static Double evalFormulaTree(FNode node, Map<String,Cell> cellMap){
    // declare variable that will be answer
    double ans = 0.0;  
    // if it's a plus then add left and right
    if(node.type == TokenType.Plus){
      ans = evalFormulaTree(node.left, cellMap);
      ans += evalFormulaTree(node.right, cellMap);
    }
    // if minus then subtract right from left
    else if(node.type == TokenType.Minus){
      ans = evalFormulaTree(node.left, cellMap);
      ans -= evalFormulaTree(node.right, cellMap);
    }
    // if multiply then multiply left and right
    else if(node.type == TokenType.Multiply){
      ans = evalFormulaTree(node.right, cellMap);
      ans *= evalFormulaTree(node.left, cellMap);
    }
    // if divide then divide left by right
    else if(node.type == TokenType.Divide){
      ans = evalFormulaTree(node.left, cellMap);
      ans /= evalFormulaTree(node.right, cellMap);
    }
    // if negate then make negative
    else if(node.type == TokenType.Negate){
      ans -= evalFormulaTree(node.left, cellMap);
    }
    // if number then make it the number
    else if(node.type == TokenType.Number){
      ans = Double.parseDouble(node.data);
    }
    // if cell ID then try to get number val of it
    else if(node.type == TokenType.CellID){
      if(cellMap.containsKey(node.data)){
        // check if the cell ID is usable 
        if(cellMap.get(node.data).numberValue() == null){
          throw new EvalFormulaException("Invalid");      
        }
        ans = cellMap.get(node.data).numberValue();      
      }
      // if id isn't in cellMap then throw exception
      else{
        throw new EvalFormulaException("Can't find");
      }
    // tokentype doesn't exist
    }
    else{
      throw new RuntimeException("Error with TokenType '" + node.type + "'");
    } 
    return ans;
  }

  // Return a set of upstream cells from this cell.
  public Set<String> getUpstreamIDs(){
    Set<String> set = new HashSet<String>();
    // call helper method, throw in empty set
    getAllCellIDsRecur(formulaTree, set);
    return set;
  }

  // Recursive helper method to calculate all IDs in the given formula
  // tree.
  protected static void getAllCellIDsRecur(FNode node, Set<String> set){
    // if node is null then stop
    if(node == null){
      return;
    }
    // if children are null then only the node itself is added
    else if (node.left == null && node.right == null && node.type ==TokenType.CellID){
      set.add(node.data);
    }
    // otherwise keep checking if left or right is a cell ID and add to set
    else{
      if(node.left != null && node.left.type == TokenType.CellID){
        set.add(node.left.data);
      }
      if(node.right != null && node.right.type == TokenType.CellID){
        set.add(node.right.data);
      }
      //recursively call method for left and right
      getAllCellIDsRecur(node.left, set);
      getAllCellIDsRecur(node.right, set);
    }
  }
}
