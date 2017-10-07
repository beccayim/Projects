// Simple class to encapsulate a triple row,col,element in boards.
// The class is immutable
public class RowColElem<T> implements Comparable<RowColElem<T>>{
  private int row;
  private int col;
  private T elem;

  // Create a RowColElem with the parameter parts
  public RowColElem(int r, int c, T e){
    this.row = r;
    this.col = c;
    this.elem = e;
  }

  // Return the row
  public int getRow(){
    return this.row;
  }

  // Return the column
  public int getCol(){
    return this.col;
  }

  // Return the element
  public T getElem(){
    return this.elem;
  }

  // Compare two elems in Row Major order: first on row, then on
  // column to break ties; fulfills Comparable interface
  public int compareTo(RowColElem<T> that){
    int diff = this.getRow()-that.getRow();
    if(diff != 0){ return diff; }
    return this.getCol()-that.getCol();
  }

  // Return a pretty string version of the triple formated as
  // (row,col,elem)
  public String toString(){
    return String.format("(%d,%d,%s)",row,col,elem);
  }
}
