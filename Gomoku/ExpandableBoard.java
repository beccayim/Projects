// DO NOT MODIFY
import java.util.*;

// Methods of an expandable game board for Gomoku
public interface ExpandableBoard<T> {

  // Fill Element Methods
  // 
  // Retrieve the fill element which is returned for empty cells.
  public T getFillElem();	      

  // Change the fill element returned for empty cells. The fill
  // element cannot be null and attempting to set it to null will
  // generate a RuntimException with the message
  // "Cannot set elements to null"
  public void setFillElem(T f);

  public int getMaxRow();		      
  public int getMaxCol();
  public int getMinRow();
  // Board Extent
  // 
  // Retrieves highest and lowest indices of row/col relevant to the
  // board. All explicit elements are within the extent determined by
  // these 4 indices.
  public int getMinCol();
  
  // get(row,col)
  // 
  // Retrive element at the given position. row and col may be any
  // value including negative numbers or values outside of the extent
  // of the matrix defined by min/max Row/Col methods.  Any access
  // beyond the extent of the board (beyond min/max row/col) will
  // return the fill element of the board. Elements that have not been
  // set will result in the fill element being returned. Otherwise an
  // explicitly set elemnt will be returned.
  public T get(int row, int col); 

  // set(row,col,x)
  // 
  // Set the element at position row,col to be x.  Perform any
  // internal expansion of the board that is required to facilitate
  // setting the element. Update the longest sequence if needed. Once
  // an element is set, it cannot be changed with any further calls to
  // set(). Attempting to do so will raise a RuntimeException with the
  // message "Element 4 -2 already set to XX" where the row/col
  // indices and string representation of the element are adjusted to
  // match the call made.  Elements cannot be set to null; attempting
  // to do so will throw a RuntimeException with the message
  // "Cannot set elements to null"
  public void set(int row, int col, T x); 

  // list = getLongestSequence()
  // 
  // Returns a List of triples of the longest sequence of equal
  // elements. Sequences may be horizontal, vertical, or diagonal the
  // returned list will be triples of (row,col,element). This function
  // may be used to determine a winner in a game by checking after a
  // player move if the longest sequence is 5 long (or however long is
  // required to win). If there is a tie for the longest sequence,,
  // the earliest longest sequence to appear on the board should be
  // returned.
  public List< RowColElem<T> > getLongestSequence();

  // Undo and Redo functionality
  // 
  // Undo the last explicit set move which was made. The board should
  // track the history of all explicit sets and allow all of them to
  // be undone with repeated calls to this method up to the point
  // where no element of the board has been explicitly set.  The board
  // does not need to change its internal size as sets are undone.
  public void undoSet();

  // If a set has been undone with undoLastSet(), redo it. This
  // repeated calls to this method should allowed. However, any
  // explicit call to set() which changes an element will empty cancel
  // the ability to redo sets.
  public void redoSet();

  // toString() - String representation of board
  // 
  // Boards should override toString to produce a nice looking string
  // separation with rows and columns dividided and labelled. 
  //
  // Examples:
  //   |  1|  2|  3|
  //   +---+---+---+
  // 1 |   |   |   |
  //   +---+---+---+
  // 2 |   |   |   |
  //   +---+---+---+
  // 3 |   |   |   |
  //   +---+---+---+
  //
  //    | -4| -3| -2| -1|  0|  1|  2|  3|
  //    +---+---+---+---+---+---+---+---+
  // -2 |  A|   |   |   |   |   |   |   |
  //    +---+---+---+---+---+---+---+---+
  // -1 |   |   |  B|   |   |   |   |   |
  //    +---+---+---+---+---+---+---+---+
  //  0 |   |   |   |   |   |   |   |   |
  //    +---+---+---+---+---+---+---+---+
  //  1 |   |   |   |   |   |  A|   |   |
  //    +---+---+---+---+---+---+---+---+
  //  2 |   |   |   |   |   |   |   |   |
  //    +---+---+---+---+---+---+---+---+
  //  3 |   |   |   |   |   |   |   |   |
  //    +---+---+---+---+---+---+---+---+
  public String toString();
}
