// Tests for Cell class: Identical to MilestoneTests
import org.junit.*;
import static org.junit.Assert.*;
import java.util.*;
import org.junit.Test; // fixes some compile problems with annotations
import org.junit.Rule;
import org.junit.rules.Timeout;

public class CellTests {
  /*Main method runs tests in this file*/ 
  public static void main(String args[])
  {
    org.junit.runner.JUnitCore.main("CellTests");
  } 

  // Global timeout for all tests: use argument to Timeout.millis( __ );
  //@Rule public Timeout globalTimeout = Timeout.millis(1000); 

  // Utility to creat a set based on arguments. Invoke as:
  //
  //   Set<String> set = toSet("B1","C1","D1");
  // 
  public static Set<String> toSet(String... args){
    Set<String> set = new HashSet<String>();
    for(String s : args){
      set.add(s);
    }
    return set;
  }


  // Utility to create a cell map based on arguments. Invoke as
  // 
  //   Map<String.Cell> cellMap = cellMap("A1","2.0","CX5","5.22");
  // 
  // Does not update any values in the cell so it
  public static Map<String,Cell> cellMap(String... args){
    Map<String,Cell> cellMap = new HashMap<String,Cell>();
    for(int i=0; i<args.length; i+=2){
      String id = args[i];
      Cell cell = Cell.make(args[i+1]);
      cellMap.put(id,cell);
    }
    return cellMap;
  }

  @Test public void cell_string1(){
    Cell cell = Cell.make("hello"); 
    assertEquals("string",cell.kind());
    assertEquals("hello", cell.displayString());
    assertEquals("hello", cell.contents());
    assertEquals(null,    cell.numberValue());
    assertEquals(false,   cell.isError());

    cell.updateValue(cellMap()); // should do nothing for kind=string

    assertEquals("string",cell.kind());
    assertEquals("hello", cell.displayString());
    assertEquals("hello", cell.contents());
    assertEquals(null,    cell.numberValue());
    assertEquals(false,   cell.isError());
  }
  @Test public void cell_string2(){
    Cell cell = Cell.make("-hi"); 
    assertEquals("string",cell.kind());
    assertEquals("-hi",   cell.displayString());
    assertEquals("-hi",   cell.contents());
    assertEquals(null,    cell.numberValue());
    assertEquals(false,   cell.isError());
    cell.updateValue(cellMap()); // should do nothing for kind=string
    assertEquals("string",cell.kind());
    assertEquals("-hi",   cell.displayString());
    assertEquals("-hi",   cell.contents());
    assertEquals(null,    cell.numberValue());
    assertEquals(false,   cell.isError());
  }
  @Test public void cell_string3(){
    Cell cell = Cell.make("A5+B2"); 
    assertEquals("string",cell.kind());
    assertEquals("A5+B2", cell.displayString());
    assertEquals("A5+B2", cell.contents());
    assertEquals(null,    cell.numberValue());
    assertEquals(false,   cell.isError());
    cell.updateValue(cellMap()); // should do nothing for kind=string
    assertEquals("string",cell.kind());
    assertEquals("A5+B2", cell.displayString());
    assertEquals("A5+B2", cell.contents());
    assertEquals(null,    cell.numberValue());
    assertEquals(false,   cell.isError());
  }

  @Test public void cell_string_trim1(){
    Cell cell = Cell.make(" Hello"); 
    assertEquals("string",cell.kind());
    assertEquals("Hello", cell.displayString());
    assertEquals("Hello", cell.contents());
    assertEquals(null,    cell.numberValue());
    assertEquals(false,   cell.isError());
    cell.updateValue(cellMap()); // should do nothing for kind=string
    assertEquals("string",cell.kind());
    assertEquals("Hello", cell.displayString());
    assertEquals("Hello", cell.contents());
    assertEquals(null,    cell.numberValue());
    assertEquals(false,   cell.isError());
  }
  @Test public void cell_string_trim2(){
    Cell cell = Cell.make("  a  b c  "); 
    assertEquals("string",cell.kind());
    assertEquals("a  b c",cell.displayString());
    assertEquals("a  b c",cell.contents());
    assertEquals(null,    cell.numberValue());
    assertEquals(false,   cell.isError());
    cell.updateValue(cellMap()); // should do nothing for kind=string
    assertEquals("string",cell.kind());
    assertEquals("a  b c",cell.displayString());
    assertEquals("a  b c",cell.contents());
    assertEquals(null,    cell.numberValue());
    assertEquals(false,   cell.isError());
  }

  @Test public void cell_number1(){
    Cell cell = Cell.make("1"); 
    assertEquals("number"    ,cell.kind());
    assertEquals("1.0"       ,cell.displayString());
    assertEquals("1"         ,cell.contents());
    assertEquals((Double)1.0 ,cell.numberValue());
    assertEquals(false       ,cell.isError());
    cell.updateValue(cellMap()); // should do nothing for kind=number
    assertEquals("number"    ,cell.kind());
    assertEquals("1.0"       ,cell.displayString());
    assertEquals("1"         ,cell.contents());
    assertEquals((Double)1.0 ,cell.numberValue());
    assertEquals(false       ,cell.isError());
  }
  @Test public void cell_number2(){
    Cell cell = Cell.make("10.472"); 
    assertEquals("number"    ,cell.kind());
    assertEquals("10.5"      ,cell.displayString());
    assertEquals("10.472"    ,cell.contents());
    assertEquals((Double)10.472, cell.numberValue());
    assertEquals(false       ,cell.isError());
    cell.updateValue(cellMap()); // should do nothing for kind=number
    assertEquals("number"    ,cell.kind());
    assertEquals("10.5"      ,cell.displayString());
    assertEquals("10.472"    ,cell.contents());
    assertEquals((Double)10.472, cell.numberValue());
    assertEquals(false       ,cell.isError());
  }
  @Test public void cell_number3(){
    Cell cell = Cell.make("-5.23"); 
    assertEquals("number"    ,cell.kind());
    assertEquals("-5.2"      ,cell.displayString());
    assertEquals("-5.23"     ,cell.contents());
    assertEquals((Double)(-5.23), cell.numberValue());
    assertEquals(false       ,cell.isError());
    cell.updateValue(cellMap()); // should do nothing for kind=number
    assertEquals("number"    ,cell.kind());
    assertEquals("-5.2"      ,cell.displayString());
    assertEquals("-5.23"     ,cell.contents());
    assertEquals((Double)(-5.23), cell.numberValue());
    assertEquals(false       ,cell.isError());
  }

  @Test public void cell_number_trim1(){
    Cell cell = Cell.make("1  "); 
    assertEquals("number"    ,cell.kind());
    assertEquals("1.0"       ,cell.displayString());
    assertEquals("1"         ,cell.contents());
    assertEquals((Double)1.0 ,cell.numberValue());
    assertEquals(false       ,cell.isError());
    cell.updateValue(cellMap()); // should do nothing for kind=number
    assertEquals("number"    ,cell.kind());
    assertEquals("1.0"       ,cell.displayString());
    assertEquals("1"         ,cell.contents());
    assertEquals((Double)1.0 ,cell.numberValue());
    assertEquals(false       ,cell.isError());
  }
  @Test public void cell_number_trim2(){
    Cell cell = Cell.make("   10.472     "); 
    assertEquals("number"    ,cell.kind());
    assertEquals("10.5"      ,cell.displayString());
    assertEquals("10.472"    ,cell.contents());
    assertEquals((Double)10.472, cell.numberValue());
    assertEquals(false       ,cell.isError());
    cell.updateValue(cellMap()); // should do nothing for kind=number
    assertEquals("number"    ,cell.kind());
    assertEquals("10.5"      ,cell.displayString());
    assertEquals("10.472"    ,cell.contents());
    assertEquals((Double)10.472, cell.numberValue());
    assertEquals(false       ,cell.isError());
  }

  // Evaluate a formula containing only arithmetic
  @Test public void evalFormulaTree_arithmetic1(){
    FNode root = FNode.parseFormulaString("=8+1");
    Double result = Cell.evalFormulaTree(root,cellMap());
    assertEquals((Double) 9.0, result);
  }
  @Test public void evalFormulaTree_arithmetic2(){
    FNode root = FNode.parseFormulaString("=8.5 +  1.75");
    Double result = Cell.evalFormulaTree(root,cellMap());
    assertEquals((Double) 10.25, result);
  }
  @Test public void evalFormulaTree_arithmetic3(){
    FNode root = FNode.parseFormulaString("=8.5 -  1.75");
    Double result = Cell.evalFormulaTree(root,cellMap());
    assertEquals((Double) 6.75, result);
  }
  @Test public void evalFormulaTree_arithmetic4(){
    FNode root = FNode.parseFormulaString("=21.0 / (8.5 -  2.5)");
    Double result = Cell.evalFormulaTree(root,cellMap());
    assertEquals((Double) 3.5, result);
  }
  @Test public void evalFormulaTree_arithmetic5(){
    FNode root = FNode.parseFormulaString("=17 / 8.5 -  2.5");
    Double result = Cell.evalFormulaTree(root,cellMap());
    assertEquals((Double) (-0.5), result);
  }
  @Test public void evalFormulaTree_arithmetic6(){
    FNode root = FNode.parseFormulaString("=17 / 8.5 + 2.5 * (2+1)");
    Double result = Cell.evalFormulaTree(root,cellMap());
    assertEquals((Double) (9.5), result);
  }
  @Test public void evalFormulaTree_arithmetic7(){
    FNode root = FNode.parseFormulaString("=-10 * -1.5");
    Double result = Cell.evalFormulaTree(root,cellMap());
    assertEquals((Double) (15.0), result);
  }
  @Test public void evalFormulaTree_arithmetic8(){
    FNode root = FNode.parseFormulaString("= -( 5 + 8 / 4 * (7+1) - 1) + 5");
    Double result = Cell.evalFormulaTree(root,cellMap());
    assertEquals((Double) (-15.0), result);
  }

  // Evaluate formulas with missing references which should raise a
  // Cell.EvalFormulaException
  @Test(timeout=1000,expected=Cell.EvalFormulaException.class)
  public void evalFormulaTree_exception1(){
    FNode root = FNode.parseFormulaString("=A1");
    Double result = Cell.evalFormulaTree(root,cellMap());
    throw new RuntimeException("Should have thrown a EvalFormulaException");
  }
  @Test(timeout=1000,expected=Cell.EvalFormulaException.class)
  public void evalFormulaTree_exception2(){
    FNode root = FNode.parseFormulaString("=2 * 20 + CX5");
    Double result = Cell.evalFormulaTree(root,cellMap());
    throw new RuntimeException("Should have thrown a EvalFormulaException");
  }
  @Test(timeout=1000,expected=Cell.EvalFormulaException.class)
  public void evalFormulaTree_exception3(){
    FNode root = FNode.parseFormulaString("=2 * (20 + CX5)");
    Double result = Cell.evalFormulaTree(root,cellMap());
    throw new RuntimeException("Should have thrown a EvalFormulaException");
  }
  @Test(timeout=1000,expected=Cell.EvalFormulaException.class)
  public void evalFormulaTree_exception4(){
    FNode root = FNode.parseFormulaString("=-BB8");
    Double result = Cell.evalFormulaTree(root,cellMap());
    throw new RuntimeException("Should have thrown a EvalFormulaException");
  }
  @Test(timeout=1000,expected=Cell.EvalFormulaException.class)
  public void evalFormulaTree_exception5(){
    FNode root = FNode.parseFormulaString("=(100 + A2) - 10 / (CX5 * BB8)");
    Double result = Cell.evalFormulaTree(root,cellMap());
    throw new RuntimeException("Should have thrown a EvalFormulaException");
  }
  @Test(timeout=1000,expected=Cell.EvalFormulaException.class)
  public void evalFormulaTree_exception6(){
    FNode root = FNode.parseFormulaString("=(100 + A2) - 10 / (CX5 * BB8)");
    Double actual = Cell.evalFormulaTree(root,cellMap("BB8","-0.5","A1","64.64","A2","200.0")); //,"CX5","10"));
    throw new RuntimeException("Should have thrown a EvalFormulaException");
  }
  @Test(timeout=1000,expected=Cell.EvalFormulaException.class)
  public void evalFormulaTree_exception7(){
    FNode root = FNode.parseFormulaString("=(100 + A2) - 10 / (CX5 * BB8)");
    Double actual = Cell.evalFormulaTree(root,cellMap("BB8","-0.5","A1","64.64","CX5","10")); // "A2","200.0")); 
    throw new RuntimeException("Should have thrown a EvalFormulaException");
  }

  // Eval a formula with references to other cells
  @Test public void evalFormulaTree_refs1(){
    FNode root = FNode.parseFormulaString("=A1");
    Double actual = Cell.evalFormulaTree(root,cellMap("A1","2.0"));
    assertEquals((Double) (2.0), actual);
  }
  @Test public void evalFormulaTree_refs2(){
    FNode root = FNode.parseFormulaString("=2 * 20 + CX5");
    Double actual = Cell.evalFormulaTree(root,cellMap("A1","2.0","CX5","5.22"));
    assertEquals((Double) (45.22), actual);
  }
  @Test public void evalFormulaTree_refs3(){
    FNode root = FNode.parseFormulaString("=2 * (20 + CX5)");
    Double actual = Cell.evalFormulaTree(root,cellMap("A1","2.0","CX5","5.22"));
    assertEquals((Double) (50.44), actual);
  }
  @Test public void evalFormulaTree_refs4(){
    FNode root = FNode.parseFormulaString("=-BB8");
    Double actual = Cell.evalFormulaTree(root,cellMap("BB8","-7.6","A1","2.0","CX5","5.22"));
    assertEquals((Double) (7.6), actual);
  }
  @Test public void evalFormulaTree_refs5(){
    FNode root = FNode.parseFormulaString("=(100 + A2) - 10 / (CX5 * BB8)");
    Double actual = Cell.evalFormulaTree(root,cellMap("BB8","-0.5","A1","64.64","A2","200.0","CX5","10"));
    assertEquals((Double) (302.0), actual);
  }
  @Test public void getUpstreamIDs_empty1(){
    Cell cell = Cell.make("= -( 5 + 8 / 4 * (7+1) - 1) + 5");
    Set<String> expect = toSet();
    Set<String> actual = cell.getUpstreamIDs();
    assertEquals(expect,actual);
  }
  
  // Test the capability of the formula cells to retrieve their
  // upstream id references (cells on which it depends for value)
  @Test public void getUpstreamIDs_refs1(){
    Cell cell = Cell.make("=A1");
    Set<String> expect = toSet("A1");
    Set<String> actual = cell.getUpstreamIDs();
    assertEquals(expect,actual);
  }
  @Test public void getUpstreamIDs_refs2(){
    Cell cell = Cell.make("=2 * 20 + CX5");
    Set<String> expect = toSet("CX5");
    Set<String> actual = cell.getUpstreamIDs();
    assertEquals(expect,actual);
  }
  @Test public void getUpstreamIDs_refs3(){
    Cell cell = Cell.make("=-BB8");
    Set<String> expect = toSet("BB8");
    Set<String> actual = cell.getUpstreamIDs();
    assertEquals(expect,actual);
  }
  @Test public void getUpstreamIDs_refs4(){
    Cell cell = Cell.make("=(100 + A2) - 10 / (CX5 * BB8)");
    Set<String> expect = toSet("BB8","A2","CX5");
    Set<String> actual = cell.getUpstreamIDs();
    assertEquals(expect,actual);
  }
  @Test public void getUpstreamIDs_refs5(){
    Cell cell = Cell.make("=A1*A1");
    Set<String> expect = toSet("A1");
    Set<String> actual = cell.getUpstreamIDs();
    assertEquals(expect,actual);
  }
  @Test public void getUpstreamIDs_refs6(){
    Cell cell = Cell.make("=(100 + A2 - (B7*(1+6)) - 10) / (CX5 * BB8 / Z8) + 22 - 0.5*GG67*GG67/BB8+Z2");
    Set<String> expect = toSet("GG67", "A2", "CX5", "B7", "BB8", "Z2", "Z8");
    Set<String> actual = cell.getUpstreamIDs();
    assertEquals(expect,actual);
  }


  // The specified behavior of formula cells is that they are in ERROR
  // immediately after creation and before a call to c.updateValue(cellMap)
  // The following tests assume check for the ERROR state after creation
  @Test public void cell_formula_error_after_creation1(){
    Cell cell = Cell.make("=A5");
    assertEquals("formula"   ,cell.kind());
    assertEquals("ERROR"     ,cell.displayString());
    assertEquals("=A5"       ,cell.contents());
    assertEquals((Double)null,cell.numberValue());
    assertEquals(true        ,cell.isError());
  }
  @Test public void cell_formula_error_after_creation2(){
    Cell cell = Cell.make("=(100 + A2) - 10 / (CX5 * BB8)");
    assertEquals("formula"   ,cell.kind());
    assertEquals("ERROR"     ,cell.displayString());
    assertEquals("=(100 + A2) - 10 / (CX5 * BB8)",cell.contents());
    assertEquals((Double)null,cell.numberValue());
    assertEquals(true        ,cell.isError());
  }

  // These formulats could potentially be evaluated without the need
  // for a cellMap so they are not used. Technically the cell should
  // be in the ERROR state after creation even for such formulas but
  // this is not enforced.
  // 
  // @Test public void cell_formula_error_after_creation3(){
  //   Cell cell = Cell.make("=5.89"); 
  //   assertEquals("formula"   ,cell.kind());
  //   assertEquals("ERROR"     ,cell.displayString());
  //   assertEquals("=5.89"     ,cell.contents());
  //   assertEquals((Double)null,cell.numberValue());
  //   assertEquals(true        ,cell.isError());
  // }
  // @Test public void cell_formula_error_after_creation4(){
  //   Cell cell = Cell.make("=  8   "); 
  //   assertEquals("formula"   ,cell.kind());
  //   assertEquals("ERROR"     ,cell.displayString());
  //   assertEquals("=  8"      ,cell.contents());
  //   assertEquals((Double)null,cell.numberValue());
  //   assertEquals(true        ,cell.isError());
  // }


  // The following tests create a formula cell the call
  // updateValue(cellMap) so that the cell should no longer be in the
  // ERROR state unless there are missing references in the cellMap
  @Test public void cell_number_formula1(){
    Cell cell = Cell.make("=5.89"); 
    cell.updateValue(cellMap()); 
    assertEquals("formula"   ,cell.kind());
    assertEquals("5.9"       ,cell.displayString());
    assertEquals("=5.89"     ,cell.contents());
    assertEquals((Double)5.89,cell.numberValue());
    assertEquals(false       ,cell.isError());
  }
  @Test public void cell_number_formula2(){
    Cell cell = Cell.make("=  8   "); 
    cell.updateValue(cellMap()); 
    assertEquals("formula"   ,cell.kind());
    assertEquals("8.0"       ,cell.displayString());
    assertEquals("=  8"      ,cell.contents());
    assertEquals((Double)8.0 ,cell.numberValue());
    assertEquals(false       ,cell.isError());
  }

  // Check that trim() is called to remove whitespace
  @Test public void cell_number_formula_trim1(){
    Cell cell = Cell.make("  =  8   "); 
    cell.updateValue(cellMap()); 
    assertEquals("formula"   ,cell.kind());
    assertEquals("8.0"       ,cell.displayString());
    assertEquals("=  8"      ,cell.contents());
    assertEquals((Double)8.0 ,cell.numberValue());
    assertEquals(false       ,cell.isError());
  }

  // Update formulas with maps that do not contain the references they
  // require which should leave the cells in ERROR
  @Test public void cell_ref_error1(){
    Cell cell = Cell.make("=A5");
    cell.updateValue(cellMap()); 
    assertEquals("formula"   ,cell.kind());
    assertEquals("ERROR"     ,cell.displayString());
    assertEquals("=A5"       ,cell.contents());
    assertEquals(null        ,cell.numberValue());
    assertEquals(true        ,cell.isError());
  }
  @Test public void cell_ref_error2(){
    Cell cell = Cell.make("=2 * 20 + CX5");
    cell.updateValue(cellMap()); 
    assertEquals("formula"   ,cell.kind());
    assertEquals("ERROR"     ,cell.displayString());
    assertEquals("=2 * 20 + CX5",cell.contents());
    assertEquals(null        ,cell.numberValue());
    assertEquals(true        ,cell.isError());
  }
  @Test public void cell_ref_error3(){
    Cell cell = Cell.make("=2 * (20 + CX5)");
    cell.updateValue(cellMap()); 
    assertEquals("formula"   ,cell.kind());
    assertEquals("ERROR"     ,cell.displayString());
    assertEquals("=2 * (20 + CX5)",cell.contents());
    assertEquals(null        ,cell.numberValue());
    assertEquals(true        ,cell.isError());
  }
  @Test public void cell_ref_error4(){
    Cell cell = Cell.make("=-BB8");
    cell.updateValue(cellMap()); 
    assertEquals("formula"   ,cell.kind());
    assertEquals("ERROR"     ,cell.displayString());
    assertEquals("=-BB8"     ,cell.contents());
    assertEquals(null        ,cell.numberValue());
    assertEquals(true        ,cell.isError());
  }
  @Test public void cell_ref_error5(){
    Cell cell = Cell.make("=(100 + A2) - 10 / (CX5 * BB8)");
    cell.updateValue(cellMap()); 
    assertEquals("formula"   ,cell.kind());
    assertEquals("ERROR"     ,cell.displayString());
    assertEquals("=(100 + A2) - 10 / (CX5 * BB8)",cell.contents());
    assertEquals(null        ,cell.numberValue());
    assertEquals(true        ,cell.isError());
  }
  @Test public void cell_ref_error6(){
    Cell cell = Cell.make("=(100 + A2) - 10 / (CX5 * BB8)");
    cell.updateValue(cellMap("BB8","-0.5","A1","64.64","A2","200.0")); //,"CX5","10"));
    assertEquals("formula"   ,cell.kind());
    assertEquals("ERROR"     ,cell.displayString());
    assertEquals("=(100 + A2) - 10 / (CX5 * BB8)",cell.contents());
    assertEquals(null        ,cell.numberValue());
    assertEquals(true        ,cell.isError());
  }
  @Test public void cell_ref_error7(){
    Cell cell = Cell.make("=(100 + A2) - 10 / (CX5 * BB8)");
    cell.updateValue(cellMap("BB8","-0.5","A1","64.64","CX5","10")); // "A2","200.0")); 
    assertEquals("formula"   ,cell.kind());
    assertEquals("ERROR"     ,cell.displayString());
    assertEquals("=(100 + A2) - 10 / (CX5 * BB8)",cell.contents());
    assertEquals(null        ,cell.numberValue());
    assertEquals(true        ,cell.isError());
  }
  @Test public void cell_ref_error8(){
    Cell cell = Cell.make("=(100 + A2) - 10 / (CX5 * BB8)");
    cell.updateValue(cellMap("BB8","-0.5","A1","64.64","CX5","10", "A2","hi")); 
    assertEquals("formula"   ,cell.kind());
    assertEquals("ERROR"     ,cell.displayString());
    assertEquals("=(100 + A2) - 10 / (CX5 * BB8)",cell.contents());
    assertEquals(null        ,cell.numberValue());
    assertEquals(true        ,cell.isError());
  }
  @Test public void cell_ref_error9(){
    Cell cell = Cell.make("=(100 + A2) - 10 / (CX5 * BB8)");
    cell.updateValue(cellMap("BB8","-0.5","A1","SWEET!","CX5","10", "A2","bogus")); 
    assertEquals("formula"   ,cell.kind());
    assertEquals("ERROR"     ,cell.displayString());
    assertEquals("=(100 + A2) - 10 / (CX5 * BB8)",cell.contents());
    assertEquals(null        ,cell.numberValue());
    assertEquals(true        ,cell.isError());
  }

  // Tests which use formulas with cell references;
  // updateValue(cellMap) is called to give the cells a valid number
  // value
  @Test public void cell_ref_formula1(){
    Cell cell = Cell.make("=A5");
    cell.updateValue(cellMap("A5","2.0")); 
    assertEquals("formula"   ,cell.kind());
    assertEquals("2.0"       ,cell.displayString());
    assertEquals("=A5"       ,cell.contents());
    assertEquals((Double)2.0 ,cell.numberValue());
    assertEquals(false       ,cell.isError());
  }
  @Test public void cell_ref_formula2(){
    Cell cell = Cell.make("=2 * 20 + CX5");
    cell.updateValue(cellMap("A1","2.0","CX5","5.22"));
    assertEquals("formula"    ,cell.kind());
    assertEquals("45.2"       ,cell.displayString());
    assertEquals("=2 * 20 + CX5",cell.contents());
    assertEquals((Double)45.22,cell.numberValue());
    assertEquals(false        ,cell.isError());
  }
  @Test public void cell_ref_formula3(){
    Cell cell = Cell.make("=2 * (20 + CX5)");
    cell.updateValue(cellMap("A1","2.0","CX5","5.22"));
    assertEquals("formula"    ,cell.kind());
    assertEquals("50.4"       ,cell.displayString());
    assertEquals("=2 * (20 + CX5)",cell.contents());
    assertEquals((Double)50.44,cell.numberValue());
    assertEquals(false        ,cell.isError());
  }
  @Test public void cell_ref_formula4(){
    Cell cell = Cell.make("=-BB8");
    cell.updateValue(cellMap("BB8","-7.6","A1","2.0","CX5","5.22"));
    assertEquals("formula"   ,cell.kind());
    assertEquals("7.6"       ,cell.displayString());
    assertEquals("=-BB8",cell.contents());
    assertEquals((Double)7.6 ,cell.numberValue());
    assertEquals(false       ,cell.isError());
  }
  @Test public void cell_ref_formula5(){
    Cell cell = Cell.make("=(100 + A2) - 10 / (CX5 * BB8)");
    cell.updateValue(cellMap("BB8","-0.5","A1","64.64","A2","200.0","CX5","10"));
    assertEquals("formula"    ,cell.kind());
    assertEquals("302.0"      ,cell.displayString());
    assertEquals("=(100 + A2) - 10 / (CX5 * BB8)",cell.contents());
    assertEquals((Double)302.0,cell.numberValue());
    assertEquals(false        ,cell.isError());
  }
  @Test public void cell_ref_formula6(){
    Cell cell = Cell.make("=(100 + A2 - (B7*(1+6)) - 10) / (CX5 * BB8 / Z8) + 22 - 0.5*GG67*GG67/BB8+Z2");
    cell.updateValue(cellMap("BB8","-0.5","B7","128.0","A1","64.64","A2","200.0","CX5","10","Z8","1.0","Z2","27.0","GG67","5.0","Z8","0.2"));
    assertEquals("formula"    ,cell.kind());
    assertEquals("98.2"       ,cell.displayString());
    assertEquals("=(100 + A2 - (B7*(1+6)) - 10) / (CX5 * BB8 / Z8) + 22 - 0.5*GG67*GG67/BB8+Z2",cell.contents());
    assertEquals((Double)98.24,cell.numberValue());
    assertEquals(false        ,cell.isError());
  }

  // Check that multiple cells can co-exist which precludes the use of
  // static fields for the cell data
  @Test public void multiple_cells1(){
    Cell c1 = Cell.make("1"); 
    Cell c2 = Cell.make("10.472"); 
    // c1
    assertEquals("number"    ,c1.kind());
    assertEquals("1.0"       ,c1.displayString());
    assertEquals("1"         ,c1.contents());
    assertEquals((Double)1.0 ,c1.numberValue());
    assertEquals(false       ,c1.isError());
    // c2
    assertEquals("number"    ,c2.kind());
    assertEquals("10.5"      ,c2.displayString());
    assertEquals("10.472"    ,c2.contents());
    assertEquals((Double)10.472, c2.numberValue());
    assertEquals(false       ,c2.isError());
  }
  @Test public void multiple_cells2(){
    Cell c1 = Cell.make("1"); 
    Cell c2 = Cell.make("=A5");
    // c1
    assertEquals("number"    ,c1.kind());
    assertEquals("1.0"       ,c1.displayString());
    assertEquals("1"         ,c1.contents());
    assertEquals((Double)1.0 ,c1.numberValue());
    assertEquals(false       ,c1.isError());
    // c2
    assertEquals("formula"   ,c2.kind());
    assertEquals("ERROR"     ,c2.displayString());
    assertEquals("=A5"       ,c2.contents());
    assertEquals(null        ,c2.numberValue());
    assertEquals(true        ,c2.isError());
  }
  @Test public void multiple_cells3(){
    Cell c1 = Cell.make("=A1");
    Cell c2 = Cell.make("=2 * 20 + CX5");
    Cell c3 = Cell.make("=(100 + A2) - 10 / (CX5 * BB8)");
    // Check all cells in error
    assertEquals(true,c1.isError());
    assertEquals(true,c2.isError());
    assertEquals(true,c3.isError());
    // update cells to establish numberValue()
    c1.updateValue(cellMap("BB8","-0.5","A1","64.64","A2","200.0","CX5","10"));
    c2.updateValue(cellMap("BB8","-0.5","A1","64.64","A2","200.0","CX5","10"));
    c3.updateValue(cellMap("BB8","-0.5","A1","64.64","A2","200.0","CX5","10"));
    // c1
    assertEquals("formula"   ,c1.kind());
    assertEquals("64.6"      ,c1.displayString());
    assertEquals("=A1"       ,c1.contents());
    assertEquals((Double)64.64,c1.numberValue());
    assertEquals(false       ,c1.isError());
    // c2
    assertEquals("formula"    ,c2.kind());
    assertEquals("50.0"       ,c2.displayString());
    assertEquals("=2 * 20 + CX5",c2.contents());
    assertEquals((Double)50.0 ,c2.numberValue());
    assertEquals(false        ,c2.isError());
    // c3
    assertEquals("formula"    ,c3.kind());
    assertEquals("302.0"      ,c3.displayString());
    assertEquals("=(100 + A2) - 10 / (CX5 * BB8)",c3.contents());
    assertEquals((Double)302.0,c3.numberValue());
    assertEquals(false        ,c3.isError());
  }
  @Test public void multiple_cells4(){
    Cell c1 = Cell.make("=A1");
    Cell c2 = Cell.make("=2 * 20 + CX5");
    Cell c3 = Cell.make("=(100 + A2) - 10 / (CX5 * BB8)");
    // Check upstream IDs
    assertEquals(toSet("A1")            ,c1.getUpstreamIDs());
    assertEquals(toSet("CX5")           ,c2.getUpstreamIDs());
    assertEquals(toSet("A2","CX5","BB8"),c3.getUpstreamIDs());
    // call update
    c1.updateValue(cellMap("BB8","-0.5","A1","64.64","A2","200.0","CX5","10"));
    c2.updateValue(cellMap("BB8","-0.5","A1","64.64","A2","200.0","CX5","10"));
    c3.updateValue(cellMap("BB8","-0.5","A1","64.64","A2","200.0","CX5","10"));
    // Update should not change upstream IDs
    assertEquals(toSet("A1")            ,c1.getUpstreamIDs());
    assertEquals(toSet("CX5")           ,c2.getUpstreamIDs());
    assertEquals(toSet("A2","CX5","BB8"),c3.getUpstreamIDs());
  }
  @Test public void cell_formula_stress1(){
    Cell cell = Cell.make("=(100 + A2 - (B7*(1+6)) - 10) / (CX5 * BB8 / Z8) + 22 - 0.5*GG67*GG67/BB8+Z2");
    assertEquals("formula"    ,cell.kind());
    assertEquals("=(100 + A2 - (B7*(1+6)) - 10) / (CX5 * BB8 / Z8) + 22 - 0.5*GG67*GG67/BB8+Z2",cell.contents());

    cell.updateValue(cellMap("BB8","-0.5","B7","128.0","A1","64.64","A2","200.0","CX5","10","Z8","1.0","Z2","27.0","GG67","5.0","Z8","0.2"));
    assertEquals("98.2"       ,cell.displayString());
    assertEquals((Double)98.24,cell.numberValue());
    assertEquals(false        ,cell.isError());

    cell.updateValue(cellMap());
    assertEquals("ERROR"     ,cell.displayString());
    assertEquals(null        ,cell.numberValue());
    assertEquals(true        ,cell.isError());

    cell.updateValue(cellMap("BB8","-0.5","B7","128.0","A1","64.64","A2","200.0","CX5","10","Z8","1.0","Z2","27.0","GG67","5.0","Z8","0.2"));
    assertEquals("98.2"       ,cell.displayString());
    assertEquals((Double)98.24,cell.numberValue());
    assertEquals(false        ,cell.isError());

    cell.updateValue(cellMap("BB8","-0.5","B7","ACK!","A1","64.64","A2","200.0","CX5","10","Z8","1.0","Z2","27.0","GG67","5.0","Z8","0.2"));
    assertEquals("ERROR"     ,cell.displayString());
    assertEquals(null        ,cell.numberValue());
    assertEquals(true        ,cell.isError());

    cell.updateValue(cellMap("BB8","-0.5","B7","128.0","A1","64.64","A2","200.0","CX5","10","Z8","1.0","Z2","27.0","GG67","bogus","Z8","0.2"));
    assertEquals("ERROR"     ,cell.displayString());
    assertEquals(null        ,cell.numberValue());
    assertEquals(true        ,cell.isError());

    cell.updateValue(cellMap("BB8","-0.5","B7","128.0","A1","64.64","A2","200.0","CX5","10","Z8","1.0","Z2","27.0","GG67","5.0","Z8","0.2"));
    assertEquals("98.2"       ,cell.displayString());
    assertEquals((Double)98.24,cell.numberValue());
    assertEquals(false        ,cell.isError());

    cell.updateValue(cellMap("BB8","-0.5","B7","64.0","A1","64.64","A2","200.0","CX5","10","Z8","1.0","Z2","27.0","GG67","-3.6","Z8","0.2"));
    assertEquals("68.3"       ,cell.displayString());
    assertEquals((Double)68.28,cell.numberValue());
    assertEquals(false        ,cell.isError());
  }

}
