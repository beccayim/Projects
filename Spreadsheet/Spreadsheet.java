import java.util.*;
public class Spreadsheet{
// Basic model for a spreadsheet. Allows cells to be set by specifying their ID
  protected Map<String, Cell> sheet;
  protected DAG dag;

  // Construct a new empty spreadsheet
  public Spreadsheet(){
    this.sheet = new HashMap<String, Cell>();
    this.dag = new DAG();
  }
  
  // Return a string representation of the spreadsheet.
  public String toString(){
    // make string builder
    StringBuilder s = new StringBuilder();
    // add the top line in suggested format and border
    s.append(String.format("%6s | %6s | %s\n", "ID", "Value", "Contents"));
    s.append("-------+--------+---------------\n");
    // for each id of the spreadsheet, append it to string in suggest format with value and contents
    for (String id : sheet.keySet()){
      s.append(String.format("%6s | %6s | '%s'\n", id, sheet.get(id).displayString(), sheet.get(id).contents()));
    }
    // use dag's tostring
    s.append("\nCell Dependencies\n");
    s.append(dag.toString());
    return s.toString();           
  }
   
  // Check if a cell ID is well formatted.
  public static void verifyIDFormat(String id){
    // check if string matches this regular expression
    if (!id.matches("^[A-Z]+[1-9][0-9]*$")){
      throw new RuntimeException("Not well formatted");
    }
  }

  // Retrieve a string which should be displayed for the value of the
  // cell with the given ID. Return "" if the specified cell is empty.
  public String getCellDisplayString(String id){
    return sheet.get(id).displayString();
  }
  
  // Retrieve a string which is the actual contents of the cell with
  // the given ID. Return "" if the specified cell is empty.
  public String getCellContents(String id){
    return sheet.get(id).contents();
  }
  
  // Delete the contents of the cell with the given ID. 
  public void deleteCell(String id){
    // remove id from the sheet
    sheet.remove(id);
    Set<String> set = new HashSet<String>();
    // add it back to the dag with empty set to make contents empty
    dag.add(id,set);
    // notify downstream cells of the chage
    notifyDownstreamOfChange(id);
  }
  
  // Set the given cell with the given contents. 
  public void setCell(String id, String contents){
    //If contents is "" or null, delete the cell indicated.
    if (contents == null || contents.equals("")){
      deleteCell(id);
      return;
    }
    Cell cell = Cell.make(contents); // make new cell with new contents
    cell.updateValue(sheet); // update the cells values 
    dag.add(id, cell.getUpstreamIDs()); // add cell to dag
    sheet.put(id, cell); // put id into sheet with updated cell
    notifyDownstreamOfChange(id);
  }

  // Notify all downstream cells of a change in the given cell.
  public void notifyDownstreamOfChange(String id){
    Set<String> downStream = dag.getDownstreamLinks(id);
    // go through all the downstream ids and update the value
    // recursively do it for each cell's downstreams to make sure 
    // everything updates in order
    for (String cell : downStream){
      sheet.get(cell).updateValue(sheet);
      notifyDownstreamOfChange(cell);
    }
  }
}