import java.util.*;
public class DenseBoard<T> implements ExpandableBoard<T>{
// Implementation of ExpandableBoard which uses internal 2D
// arrays/ArrayLists to store elements. Provides undo/redo
// capabilities and tracks the longest sequence as elements are set.
// 
// Space Complexity: O(R*C + H*L)
//   R: number of rows in the board
//   C: number of cols in the board
//   H: combined size of the undo/redo history
//   L: length of the longest sequence
  private ArrayList<ArrayList<T>> board;
  private List<RowColElem<T>> lgstSeq;
  private int minRow, maxRow, minCol, maxCol;
  private T fillElem;
  private ArrayList<myStack> undos,redos;
  
  //private inner class to record the history of elements and the longest sequence at that time
  private class myStack{
    //internal fields include the element and the longest seq. when element in placed
    private RowColElem<T> elem;
    private List<RowColElem<T>> seq;
     
    //private constructor
    private myStack(RowColElem<T> elem, List<RowColElem<T>> seq){
      this.elem = elem;
      this.seq = seq;
    }

    //getter method for element
    private RowColElem<T> getElem(){
      return this.elem;
    }
    //getter method for longest seq associated with element
    private List<RowColElem<T>> getLS(){
      return this.seq;
    }
    
    //string rep for me to see progress when testing
    public String toString(){
      String s = "[";
      for (int i = 0; i < seq.size(); i++){
        s += seq.get(i) + ",";
      }
      s += "]";
      return s;
    }
  }

    // Workhorse constructor, create initial space indicated by min/max
  // row/col. 
  // Runtime: O(R * C)
  //   R; number of rows which is maxRow-minRow+1
  //   C; number of cols whcih is maxCol-minCol+1
  public DenseBoard(int minRow, int maxRow, int minCol, int maxCol, T fillElem){
    //instantiate internals fields
    this.minRow = minRow;
    this.maxRow = maxRow;
    this.minCol = minCol;
    this.maxCol = maxCol;
    this.lgstSeq = new ArrayList<RowColElem<T>>();
    this.undos = new ArrayList<myStack>();
    this.redos = new ArrayList<myStack>();
    // throw runtime exception when fillElem is null
    if (fillElem == null){
      throw new RuntimeException("Cannot set fill element to null");
    }
    this.fillElem = fillElem;
    // make board an arraylist of arraylists with given boundaries and fill it with null
    board = new ArrayList<ArrayList<T>>();
    for (int i = minRow; i <= maxRow; i++){
      ArrayList<T> row = new ArrayList<T>();
      for (int j = minCol; j <= maxCol; j++){
        row.add(null);
      }
      board.add(row);
    }
           
     
  }
  
// Convenience 1-arg constructor, creates a single cell board with
  // given fill element. The initial extent of the board is a single
  // element at 0,0.  May wish to call the first constructor in this
  // one to minimize code duplication.
  public DenseBoard(T fillElem){
    this(0,0,0,0,fillElem); //use first constructor
  }
  
  // Convenience 2-arg constructor, creates a board with given fill
  // element and copies elements from T 2-D array. Assumes upper left
  // is coordinate 0,0 and lower right is size of 2-D array
  public DenseBoard(T[][] x, T fillElem){
    this(0,x.length - 1,0, x[0].length - 1, fillElem); // use first constructor
    // then iterate through the board again and set anything that should have a value from t[][] to be that value
    for (int i = 0; i < x.length; i++){
      for(int j = 0; j < x[i].length; j++){
        if (x[i][j] != fillElem){
          set(i,j, x[i][j]);
        }
      }
    }
    //reset undos to be empty otherwise the stuff that was "set" (even though they weren't really) will be in there
    undos = new ArrayList<myStack>();
  }


  // Access the extent of the board: all explicitly set elements are
  // within the bounds established by these four methods.
  //
  // Runtime: O(1)
  //getter method of minRow
  public int getMinRow(){
    return minRow;
  }

  //getter method of maxRow
  public int getMaxRow(){
    return maxRow;
  }

  //getter method of minCol
  public int getMinCol(){
    return minCol;
  }

  //getter method of maxCol
  public int getMaxCol(){
    return maxCol;
  }
 
  // Retrieve the fill element for the board.
  public T getFillElem(){
    return fillElem;
  }
  

  // Change the fill element for the board. 
  //Runtime O(1) (worst case) b/c we're just reassigning a field.
  public void setFillElem(T f){
    //can't set fill to null
    if (f == null){ 
      throw new RuntimeException("Cannot set fill element to null");
    }
    // if new fill isn't same as old fill the make the change
    if (fillElem != f){
      this.fillElem = f;
    }
  }
  
  
  // Retrieve the longest sequence present on the board. 
  // Runtime: O(L) (worst case)
  // L: length of the longest sequence
  public List< RowColElem<T> > getLongestSequence(){ 
    ArrayList<RowColElem<T>> ans = new ArrayList<RowColElem<T>>();
    //makes a copy of lgstSeq
    ans.addAll(lgstSeq);
    return ans;
   
  }
  
  // Retrieve an element at virtual row/col specified.
  public T get(int row, int col){ 
    // if it's out of bounds then return fillElem
    if (row > maxRow || row < minRow || col > maxCol || col < minCol){
      return fillElem; 
    }
    //variables to calculate where in the array the row col would be
    int rowDiff = row - minRow;
    int colDiff = col - minCol;
    //if element is null then just return fillElem
    if (board.get(rowDiff).get(colDiff) == null){
      return fillElem;
    }
    //otherwise return the value of the element
    else{
      return board.get(rowDiff).get(colDiff);
    }
  }

  
  // Append a row to the bottom of the board increasing the maximum
  // row by one
  //Runtime is O(C) C = # of cols because the for loop iterates from minCol to maxCol aka # of cols
  public void addRowBottom(){
    //make a new empty row
    ArrayList<T> row = new ArrayList<T>();
    // fill in new row with fillElem
    for (int j = minCol; j <= maxCol; j++){
      row.add(null);
    }
    board.add(row); //add the row to the board
    maxRow++; // increment maxRow
  }
 
  // Append a column to the right edge of the board increasing the
  // maximum column by one
  //Runtime is O(R) R= # of rows because the for loop interates through board.size() aka # of rows
  public void addColRight(){
    int rowLength = board.size();
    // for each row, add an element fillElem to make a new col
    for (int j = 0; j < rowLength; j++){
      board.get(j).add(null);
    }
    maxCol++; //increment maxCol
  }
  
  //helper method to check North-South direction for longest sequence
  public void checkNS(int row, int col, T x){
    //variables to calculate where in the array row and col would be
    int rowDiff = row - minRow; 
    int colDiff = col - minCol;
    int count = 0; // count to keep track of how far from set elem we are 
    List<RowColElem<T>> seq = new ArrayList<RowColElem<T>>(); //new seq of possible longest
    RowColElem<T> temp = new RowColElem<T>(row,col,x); //RowColElem of element that is being set
    
    //check top half before set element
    // starting from row one above element to 0, increment count to keep track of how far you are
    for (int newRow = rowDiff - 1; newRow >= 0; newRow--){
      count++;
      //check if element has same value as the set element value
      // if it does then make a new RowColElem of that element and add it to seq
      if (board.get(newRow).get(colDiff) == x || x.equals(board.get(newRow).get(colDiff))){
        RowColElem<T> e = new RowColElem<T>(row - count, col, x);
        seq.add(e);
      }
      //if same element isn't found then break
      else{
        break;
      }
    }
    // add the element that is being set to the seq.
    seq.add(temp);
    count = 0; //reset count for next iteration
    
    //check bottom half after set element
    //starting from row one below element to board.size(), increment count 
    for (int newRow = rowDiff + 1; newRow < board.size(); newRow++){
      count++;
      //if same element found then make it to a RCE and add to seq
      if (board.get(newRow).get(colDiff) == x || x.equals(board.get(newRow).get(colDiff))){
        temp = new RowColElem<T>(row + count, col, x);
        seq.add(temp);
      }
      //if not found then breal
      else{
        break;
      }
    }
    //check if new sequence is the new longest seq
    if (seq.size() > this.lgstSeq.size()){
      this.lgstSeq = seq;
    }   
  }

  //helper method to check Diagonal direction for longest sequence
  public void checkD(int row, int col, T x){
    //variables to calculate where in the array row and col would be
    int rowDiff = row - minRow; 
    int colDiff = col - minCol; 
    int count = 0; // count to keep track of how far from elem we are
    List<RowColElem<T>> seq = new ArrayList<RowColElem<T>>();//new seq of possible longest
    RowColElem<T> temp = new RowColElem<T>(row,col,x); //RowColElem of element that is being set

    //go down left
    // start at one column less than set element and continue till minCol
    // increment count to keep trakc of how far from elem we are and rowDiff b/c row is also going down
    for (int newCol = colDiff - 1; newCol >= 0; newCol--){
      count++;
      rowDiff++;
      //make sure stay in row bounds
      if (rowDiff < board.size()){
        // check if element is same as set element, if it is then make RCE and add to seq
        if (board.get(rowDiff).get(newCol) == x || x.equals(board.get(rowDiff).get(newCol))){
          RowColElem<T> e = new RowColElem<T>(row + count,col - count,x);
          seq.add(e);
        }
        //if not found then break
        else{
          break;
        }
      }
    }

    seq.add(temp); // add the element that is being set to the seq.
    count = 0; //reset count for next iteration
    rowDiff = row - minRow; // reset rowDiff
    
    //check up right    
    // start at one col right of set element and continue till maxCols
    // increment count and decrement rowDiff bc we are going up the board
    for (int newCol = colDiff +1; newCol < board.get(0).size(); newCol++){
      count++;
      rowDiff--;
      // make sure we stay within row bounds
      if (rowDiff >= 0){
        // if element is same as row element, add RCE to seq
        if (board.get(rowDiff).get(newCol) == x || x.equals(board.get(rowDiff).get(newCol))){
          temp = new RowColElem<T>(row - count,col + count,x);
          seq.add(temp);
        }
        // otherwise break
        else{
          break;
        }
      }
    }
    //check if new sequence is the new longest seq
    if (seq.size() > this.lgstSeq.size()){
      this.lgstSeq = seq;
    }
  }

  //helper method to check anti-diagonal direction for longest seq.
  public void checkAD(int row, int col, T x){
    //variables to calculate where in the array row and col would be
    int rowDiff = row - minRow; 
    int colDiff = col - minCol;
    int count = 0; // count to keep track of how far from elem we are
    List<RowColElem<T>> seq = new ArrayList<RowColElem<T>>();//new seq of possible longest
    RowColElem<T> temp = new RowColElem<T>(row,col,x);// RowColElem of element that is being set
    
    //check up left 
    //start at one col to the left of set element until minCol
    // increment count and decrement rowDiff bc we're going up the board
    for (int newCol = colDiff - 1; newCol >= 0; newCol--){
      count++;
      rowDiff--;
      //make sure within row bounds
      if (rowDiff >= 0){
        // if element is same as set element then make RCE and add to seq
        if (board.get(rowDiff).get(newCol) == x || x.equals(board.get(rowDiff).get(newCol))){
          RowColElem<T> e = new RowColElem<T>(row - count,col - count,x);
          seq.add(e);
        }
        //otherwise break
        else{
          break;
        }
      }
    }
    seq.add(temp); // add the element that is being set to the seq.
    count = 0; //reset count for next iteration
    rowDiff = row - minRow; //reset rowDiff
    
    //go down right
    // start at one col to the right of set element and increment count and rowDiff cus going down board
    for (int newCol = colDiff +1; newCol < board.get(0).size(); newCol++){
      count++;
      rowDiff++;
      // stay within row bounds 
      if (rowDiff < board.size()){
        // if element is same as set element then make RCE and add to seq.
        if (board.get(rowDiff).get(newCol) == x || x.equals(board.get(rowDiff).get(newCol))){
          temp = new RowColElem<T>(row + count,col + count,x);
          seq.add(temp);
        }
        //otherwise break
        else{
          break;
        }
      }
    }
    //check if new sequence is the new longest seq
    if (seq.size() > this.lgstSeq.size()){
      this.lgstSeq = seq;
    }
  }
  
  // Helper method to look for longest sequence in West-East direction
  public void checkWE(int row, int col, T x){ 
    //variables to calculate where in the array row and col would be
    int rowDiff = row - minRow;
    int colDiff = col - minCol;
    int count = 0; // count to keep track of how far from set elem we are 
    List<RowColElem<T>> seq = new ArrayList<RowColElem<T>>(); //new seq of possible longest
    RowColElem<T> temp = new RowColElem<T>(row,col,x); // RowColElem of element that is being set
    
    // loop that checks in the west direction
    for (int newCol = colDiff - 1; newCol >= 0; newCol--){
      count++; // keep track of how far from elem we are
      // if one slot to the left is the same element then add the new RowColElem to seq.
      if (board.get(rowDiff).get(newCol) == x){
        RowColElem<T> e = new RowColElem<T>(row,col - count,x);
        seq.add(e);
      }
      // otherwise nothing here
      else{
        break;
      }
    }
    seq.add(temp); // add the element that is being set to the seq.
    count = 0; //reset count for next iteration
    
    //check in the east direction
    for (int newCol = colDiff +1; newCol < board.get(0).size(); newCol++){
      count++;
      // if slot to the right is the same element the add the new RCE to end of seq.
      if (board.get(rowDiff).get(newCol) == x){
        temp = new RowColElem<T>(row,col + count,x);
        seq.add(temp);
      }
      // otherwise nothing here
      else{
        break;
      }
    }
    // keep checking if this new sequence is the longest
    if (seq.size() > this.lgstSeq.size()){
      this.lgstSeq = seq;
    }
  }
        
      
  
  // Set give element at row/col position to be x. 
  // Runtime:
  //   If expansion is required, same complexity as expandToInclude()
  //   If expansion is not required, O(L)
  //     L: the length of the longest sequence on the board
  public void set(int row, int col, T x){ //still gotta check longest sequence and do expandable
    // if set element is null, throw runtimeexception
    if (x == null){
      throw new RuntimeException("Cannot set elements to null");
    // if the new fillElem is same as fillElem then ignore
    }
    else if (x == fillElem || x.equals(fillElem)){ 
      return;
    }
    
    expandToInclude(row,col);//expand the board to fit new row and col
    //variables to calculate where in the array row and col would be
    int rowDiff = row - minRow;
    int colDiff = col - minCol;
    //if element is already set, throw runtimeexception
    if (board.get(rowDiff).get(colDiff) != null){// @@@@@@@@@@@@@@@@@@@@@@@@@@ strings and integers no difference?
      throw new RuntimeException("Element " + row + " " + col + " already set to " + board.get(rowDiff).get(colDiff));
    }
    //Set the element
    else{
      board.get(rowDiff).set(colDiff, x);
      //check for new longest seq
      checkWE(row,col,x);
      checkNS(row,col,x);
      checkD(row,col,x);
      checkAD(row,col,x);
      //reset redo when you set an element
      redos = new ArrayList<myStack>();
      //make a RCE and list (longest seq) and make it a stack and add it to undos to keep track of board history
      RowColElem<T> temp = new RowColElem<T>(row, col, x);
      List<RowColElem<T>> ls = getLongestSequence();
      myStack s = new myStack(temp, ls);
      undos.add(s);
    }
  }
  
  // Return how many rows the board has in memory which should
  // correspond to the difference between maxRow and minRow. 
  public int getPhysicalRows(){
    return board.size();
  }

  // Return how many columns the board has in memory which should
  // correspond to the difference between maxCol and minCol.
  public int getPhysicalCols(){
    return board.get(0).size();
  } 
  
  //Helper method that adds row to the top of the board
  // Runtime (N + R*C), n = new elems add, R = # rows, C = # cols, bc there's one for loop 
  // to make the new elements, and 2 for loops that iterate through original board size.
  public void addRowTop(int rows){
    // first makes a row of nulls to the correct size (# of cols) and adds to board at bottom
    for (int i = 0; i < rows; i++){
      ArrayList<T> row = new ArrayList<T>();
      for(int j = minCol; j <= maxCol; j++){
        row.add(null);
      }
      board.add(row); //add the row to the board
      minRow--; //decrement minRow
    }
    
    //iterate through "original" size board backwards in case of overlapping when shifting
    for (int i = board.size()-1 - rows; i >= 0; i--){
      for (int j = board.get(0).size() - 1; j >= 0; j--){
        // basically copy whatever was in the row of the "original" board and shift it by # of rows added
        // so rows is added to row index. 
        // then set the row of "original" board to null;
        board.get(i+rows).set(j, board.get(i).get(j));
        board.get(i).set(j,null);
      }
    }
  }
  
  //Helper method that adds a column to left of the board
  public void addColLeft(int cols){
    // adds a column on each row with null, and does this cols many times (cols = # of new cols);  
    for (int i = 1; i <= cols; i++){
      for (int j = 0; j < board.size(); j++){
        board.get(j).add(null);
      }
      minCol--; //decrement maxCol
    } 
    // iterates through "original" size of board backwards in case of overlapping
    for (int i = board.size()-1; i >= 0; i--){
      for (int j = board.get(0).size() - 1 -cols; j >= 0; j--){
        // basically copy whatever was in the col of the original board and shift it by # of cols added
        // so cols is added to col (j) index.
        // then set the col of "original" board to null
        board.get(i).set(j + cols, board.get(i).get(j));
        board.get(i).set(j, null);
      }
    }
  }
  
  // Ensure that there is enough internal storage allocated so that no
  // expansion will occur if set(row,col,x) is called. Expand internal
  // space for the board if needed.
  // Runtime: 
  //   Expansion right/down: O(N)       (amortized)
  //   Expansion left/up:    O(N + R*C) (amortized)
  //     N: new elements created which is the return value of the function
  //     R: current number of rows
  //     C: current number of columns
  public int expandToInclude(int row, int col){
    //Variables that represent how many more rows or cols will be needed
    int rowDiff = 0;
    int colDiff = 0;
    // represent new elems that will be added
    int newElem = 0;
    // find the # of rows needed in either direction
    if (row > maxRow){
      rowDiff = row - maxRow; 
    }
    else if (row < minRow){
      rowDiff = row - minRow;
    }
    // find the # of cols needed in either direction
    if (col > maxCol){
      colDiff = col - maxCol;
    }
    else if (col < minCol){
      colDiff = col - minCol;
    }
    
    // if no rows of cols needed then return 0
    if (rowDiff == 0 && colDiff == 0){
      return 0;
    }
    // For however many extra rows needed, depending on the direction
    // either add row at the bottom or top
    // if rowDiff is positive then you're adding more rows out
    if (rowDiff > 0){
      // for each new row needed just call addRowBottom
      for (int i = 0; i < rowDiff; i++){
        addRowBottom();
        newElem += board.get(0).size(); // increment newElem by # of elems in rows being added
      }
    }
    else if (rowDiff <0) {
      addRowTop(Math.abs(rowDiff)); // call addRowTop method to do work
      newElem += board.get(0).size() * Math.abs(rowDiff); // incremenet newElem by # of elems in rows being added
    }

    
    // For however many extra cols needed, depending on the direction
    // either add col to the left or right
    if (colDiff > 0){
      for (int j = 0; j < colDiff; j++){
        addColRight();
        newElem += board.size();
      }
    }
    else if (colDiff < 0){
      addColLeft(Math.abs(colDiff));
      newElem += board.size() * Math.abs(colDiff);
    }
    return newElem;
  }
 
  // Undo an explicit set(row,col,x) operation by changing an element
  // to its previous state.
  // Runtime: O(1) (worst case)
  public void undoSet(){
    // if undos is empty then throw exception
    if (undos.size() == 0){
      throw new RuntimeException("Undo history is empty");
    }
    // get last element that will be undone
    myStack undo = undos.get(undos.size()-1);
    // take it out of the list
    undos.remove(undos.size()-1);
    //find where this element would be in arraylist indexes
    int rowDiff = undo.getElem().getRow() - minRow;
    int colDiff = undo.getElem().getCol() - minCol;
    // set it to null now
    board.get(rowDiff).set(colDiff, null);
    // reassign lgstSeq by grabbing the last longest seq in the undo list
    if (undos.size() > 0){
      lgstSeq = undos.get(undos.size()-1).getLS();
    }
    // if undos is empty then lgst seq is empty
    else{
      lgstSeq = new ArrayList<RowColElem<T>>();
    }
    // anything undone can be redone
    redos.add(undo);
  }

  // Redo a set that was undone via undoSet().
  // Runtime: O(1)
  public void redoSet(){
    // if redos is empty then throw exception
    if (redos.size() == 0){
      throw new RuntimeException("Redo history is empty");
    }
    // get last element that will be redone
    myStack doAgain = redos.get(redos.size()-1);
    // calculate where this element is according to board indexes
    int rowDiff = doAgain.getElem().getRow() - minRow;
    int colDiff = doAgain.getElem().getCol() - minCol;
    //set those indexes to be the element that it was once assigned again
    board.get(rowDiff).set(colDiff,doAgain.getElem().getElem()); 
    // this can now be undone
    undos.add(doAgain);
    // can take it out of redo list
    redos.remove(redos.size()-1);
    // longest seq is now whatever was recently added longest seq
    lgstSeq = doAgain.getLS(); 
  }

  //String representation of board
  // Runtime: O(R*C)
  //   R: number of rows
  //   C: number of columns
  public String toString(){
    //Variables that represents the numbers of rows and cols
    int rowLength = board.size();
    int colLength = board.get(0).size();
  
    StringBuilder s = new StringBuilder("    "); //will be the board rep / answer
    StringBuilder barString= new StringBuilder("    "); // represents the bar border
    StringBuilder topBar = new StringBuilder(""); // represents the top bar border
    // for each column, make the barString that much longer, and add column # to border
    for(int i = 0; i < colLength; i++){
      barString.append("+---");
      topBar.append(String.format("|%3s", minCol + i));
     }
    // append the ends of the strings
    barString.append("+\n");
    topBar.append("|\n");
    // append the borders to the answer string at the beginning
    s.append(topBar);
    s.append(barString);
  
    // iterate through the number of rows and cols
    for (int i = 0; i < rowLength; i++){
      for (int j = 0; j < colLength; j++){
        // check if col is at 0, b/c then you need the row number at the beginning
        if (j == 0){
          if (board.get(i).get(j) == null){
            s.append(String.format(" %2s |%3s", minRow + (i), fillElem));
          }
          else{
            s.append(String.format(" %2s |%3s", minRow + (i), board.get(i).get(j)));
          }
        }
        //otherwise just add the elements and bars
        else{
          if(board.get(i).get(j) == null){
            s.append(String.format("|%3s", fillElem)); // @@@@@@@@@@ later switch that to the correct values
          }
          else{
            s.append(String.format("|%3s", board.get(i).get(j)));
          }
        }  
      }
      //finish off the row and add the bar border
      s.append("|\n" + barString);
    }
  //turn string builder into a string
  String ans = s.toString();
  return ans;  
  }
}