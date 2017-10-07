// Tests of DAG
import org.junit.*;
import static org.junit.Assert.*;
import java.util.*;
import org.junit.Test; // fixes some compile problems with annotations
import org.junit.Rule;
import org.junit.rules.Timeout;

public class DAGTests {
  /*Main method runs tests in this file*/ 
  public static void main(String args[])
  {
    org.junit.runner.JUnitCore.main("DAGTests");
  } 

  // Global timeout for all tests: use argument to Timeout.millis( __ );
  @Rule public Timeout globalTimeout = Timeout.millis(1000); 

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

  // Sort the elements of string representation of a list/array and
  // return a string of the answer
  // 
  // sortArray("[C1, E1, B1, D1]")
  //       --> "[B1, C1, D1, E1]"
  public static String sortList(String s){
    String [] list = s.replaceAll("[\\[\\],]","").split(" ");
    Arrays.sort(list);
    String listStr = Arrays.toString(list);
    return listStr;
  }

  // Utility to sort a line involving a DAG link to get the links in
  // sorted order for comparison
  //
  // sortLine("A1 : [C1, E1, B1, D1]")
  //      --> "A1 : [B1, C1, D1, E1]"
  public static String sortDAGLine(String s){
    String prefix = s.substring(0,7);
    String sorted = sortList(s.substring(7));
    return prefix + sorted;
  }

  // Utility to sort the lines of the DAG to produce a string which is
  // independent of the order the links may appear in the map.  Sorts
  // text that appears in a line and the overall order of the
  // lins. Uses sortedLine to order lines corresponding to DAG links.
  public static String sortedLines(String s){
    try{
      // Extract the lines corresponding to upstream/downstream links
      String ulinks = "Upstream Links:\n";
      int ulinksIdx = s.indexOf(ulinks);
      String dlinks = "Downstream Links:\n";
      int dlinksIdx = s.indexOf(dlinks);
      String ulinksSub = s.substring(ulinksIdx+ulinks.length(), dlinksIdx);
      String dlinksSub = s.substring(dlinksIdx+dlinks.length(), s.length());
      // Sort the lines
      String [] ulinksLines = ulinksSub.split("\n");
      Arrays.sort(ulinksLines);
      String [] dlinksLines = dlinksSub.split("\n");
      Arrays.sort(dlinksLines);
      // Rebuild a string based on sorted order of lines and sorting
      // of each line internally
      StringBuilder sorted = new StringBuilder();
      sorted.append(ulinks);
      for(String u : ulinksLines){
        sorted.append(sortDAGLine(u)); sorted.append('\n');
      }
      sorted.append(dlinks);
      for(String d : dlinksLines){
        sorted.append(sortDAGLine(d)); sorted.append('\n');
      }
      return sorted.toString();
    }
    catch(StringIndexOutOfBoundsException e){
      return s;
    }
  }

  @SuppressWarnings("unchecked")
  public static Set<String> EMPTY_SET = (Set<String>) Collections.EMPTY_SET;

  // Tests which create empty dags and check that methods work correctly
  @Test public void dag_empty1(){
    DAG dag = new DAG();
    String expect =
      "Upstream Links:\n"+
      "Downstream Links:\n"+
      "";
    String actual = dag.toString();
    assertEquals(expect,actual);
  }
  @Test public void dag_empty2(){
    DAG dag = new DAG();
    Set<String> expect,actual;
    expect = EMPTY_SET;
    actual = dag.getUpstreamLinks("A1");
    assertEquals(expect,actual);
    
    expect = EMPTY_SET;
    actual = dag.getUpstreamLinks("hello");
    assertEquals(expect,actual);
    
    expect = EMPTY_SET;
    actual = dag.getDownstreamLinks("BB8");
    assertEquals(expect,actual);

    expect = EMPTY_SET;
    actual = dag.getDownstreamLinks("Ack!");
    assertEquals(expect,actual);
  }

  // Inspect a dag to ensure that it matches an expected set of
  // upstream/downstream links. The expected string version is checked
  // as well as potential specific upstream and downstream links which
  // are passed in arrays with a pattern of alternating ID and string
  // version of the links, as in:
  //
  // expectUpstream = new String[]{
  //   "A1","[D1, C1, B1]",
  //   "B1","[D1, C1]",
  // };
  public static void checkDAG(DAG dag, String expectSunsorted,
                              String [] expectUpstream,
                              String [] expectDownstream)
  {
    String msg;
    String actualS = sortedLines(dag.toString());
    String expectS = sortedLines(expectSunsorted); // expectSunsorted; 
    msg = String.format("DAG toString is wrong:\nExpect:\n%s\nActual:\n%s\n",expectS,actualS);
    assertEquals(msg,expectS,actualS);

    for(int i=0; i<expectUpstream.length; i+=2){
      String id =  expectUpstream[i];
      String expect = sortList(expectUpstream[i+1]);
      String actual = null;
      if(dag.getDownstreamLinks(id) != null){
        actual = sortList(dag.getUpstreamLinks(id).toString());
      }
      msg =
        String.format("Upstream Links wrong\nID:     %s\n",id)+
        String.format("Sorted Expect: %s\n",expect)+
        String.format("Sorted Actual: %s\n",actual)+
        String.format("DAG:\n%s\n",dag.toString())+
        "";
      assertEquals(msg,expect,actual);
    }

    for(int i=0; i<expectDownstream.length; i+=2){
      String id =  expectDownstream[i];
      String expect = sortList(expectDownstream[i+1]);
      String actual = null;
      if(dag.getDownstreamLinks(id) != null){
        actual = sortList(dag.getDownstreamLinks(id).toString());
      }
      msg =
        String.format("Downstream Links wrong\nID:      %s\n",id)+
        String.format("Sorted Expect: %s\n",expect)+
        String.format("Sorted Actual: %s\n",actual)+
        String.format("DAG:\n%s\n",dag.toString())+
        "";
      assertEquals(msg,expect,actual);
    }
  }

  // Check basic add functionality of the DAG
  @Test public void dag_basic_add1(){
    String expectS, expectUpstream[], expectDownstream[];
    DAG dag = new DAG();
    dag.add("A1",toSet("B1"));
    expectS =
      "Upstream Links:\n"+
      "  A1 : [B1]\n"+
      "Downstream Links:\n"+
      "  B1 : [A1]\n"+
      "";
    expectUpstream = new String[]{
      "A1","[B1]",
      "B1","[]"
    };
    expectDownstream = new String[]{
      "B1","[A1]",
      "A1","[]",
      "C1","[]",
    };
    checkDAG(dag,expectS,expectUpstream,expectDownstream);
  } 
  @Test public void dag_basic_add2(){
    String expectS, expectUpstream[], expectDownstream[];
    DAG dag = new DAG();
    dag.add("A1",toSet("B1","C1","D1"));
    expectS =
      "Upstream Links:\n"+
      "  A1 : [B1, C1, D1]\n"+
      "Downstream Links:\n"+
      "  C1 : [A1]\n"+
      "  B1 : [A1]\n"+
      "  D1 : [A1]\n"+
      "";
    expectUpstream = new String[]{
      "A1","[B1, C1, D1]",
      "D1","[]",
      "X2","[]",
    };
    expectDownstream = new String[]{
      "D1","[A1]",
      "C1","[A1]",
      "B1","[A1]",
      "A1","[]",
      "X2","[]",
    };
    checkDAG(dag,expectS,expectUpstream,expectDownstream);
  }
  @Test public void dag_basic_add3(){
    String expectS, expectUpstream[], expectDownstream[];
    DAG dag = new DAG();
    dag.add("A1",toSet("B1","C1","D1"));
    dag.add("B1",toSet("C1","D1"));
    expectS =
      "Upstream Links:\n"+
      "  A1 : [B1, C1, D1]\n"+
      "  B1 : [C1, D1]\n"+
      "Downstream Links:\n"+
      "  D1 : [A1, B1]\n"+
      "  C1 : [A1, B1]\n"+
      "  B1 : [A1]\n"+
      "";
    expectUpstream = new String[]{
      "A1","[B1, C1, D1]",
      "B1","[C1, D1]",
    };
    expectDownstream = new String[]{
      "D1","[A1, B1]",
      "C1","[A1, B1]",
      "B1","[A1]",
    };
    checkDAG(dag,expectS,expectUpstream,expectDownstream);
  }
  @Test public void dag_basic_add4(){
    String expectS, expectUpstream[], expectDownstream[];
    DAG dag = new DAG();
    dag.add("A1",toSet("B1","C1","D1"));
    dag.add("B1",toSet("C1","D1"));
    dag.add("C1",toSet("D1"));
    dag.add("D1",toSet("XY11","X23"));
    dag.add("X23",toSet("W0"));
    expectS =
      "Upstream Links:\n"+
      "  A1 : [B1, C1, D1]\n"+
      " X23 : [W0]\n"+
      "  D1 : [XY11, X23]\n"+
      "  C1 : [D1]\n"+
      "  B1 : [D1, C1]\n"+
      "Downstream Links:\n"+
      "XY11 : [D1]\n"+
      " X23 : [D1]\n"+
      "  W0 : [X23]\n"+
      "  D1 : [A1, C1, B1]\n"+
      "  C1 : [A1, B1]\n"+
      "  B1 : [A1]\n"+
      "";
    expectUpstream = new String[]{
      "A1","[D1, B1, C1]",
      "X23","[W0]",
      "D1","[XY11, X23]",
      "C1","[D1]",
      "B1","[D1, C1]",
      "W0","[]",
    };
    expectDownstream = new String[]{
      "X23","[D1]",
      "XY11","[D1]",
      "W0","[X23]",
      "D1","[A1, C1, B1]",
      "C1","[A1, B1]",
      "B1","[A1]",
      "A1","[]",
    };
    checkDAG(dag,expectS,expectUpstream,expectDownstream);
  }
  @Test public void dag_basic_add5(){
    String expectS, expectUpstream[], expectDownstream[];
    DAG dag = new DAG();
    dag.add("WD40",toSet("CC61","OX12","ZD10","PH1"));
    dag.add("OX12",toSet("H2","N2","O2"));
    dag.add("ZD10",toSet("H2","N2","PH1"));
    dag.add("CC61",toSet("C1","CC1","C12","C4"));
    dag.add("PH1",toSet("HX9","AB2"));
    dag.add("H2",toSet());
    dag.add("N2",toSet("N1","PH1"));
    expectS =
      "Upstream Links:\n"+
      "ZD10 : [N2, PH1, H2]\n"+
      "WD40 : [ZD10, CC61, PH1, OX12]\n"+
      "CC61 : [CC1, C4, C12, C1]\n"+
      "  N2 : [N1, PH1]\n"+
      " PH1 : [AB2, HX9]\n"+
      "OX12 : [O2, N2, H2]\n"+
      "Downstream Links:\n"+
      "ZD10 : [WD40]\n"+
      "  O2 : [OX12]\n"+
      "  N1 : [N2]\n"+
      "CC61 : [WD40]\n"+
      "  N2 : [ZD10, OX12]\n"+
      " C12 : [CC61]\n"+
      " HX9 : [PH1]\n"+
      "  H2 : [ZD10, OX12]\n"+
      "  C1 : [CC61]\n"+
      "OX12 : [WD40]\n"+
      " CC1 : [CC61]\n"+
      "  C4 : [CC61]\n"+
      " AB2 : [PH1]\n"+
      " PH1 : [ZD10, WD40, N2]\n"+
      "";
    expectUpstream = new String[]{
      "ZD10" ,"[N2, PH1, H2]",
      "WD40" ,"[ZD10, CC61, PH1, OX12]",
      "CC61" ,"[CC1, C4, C12, C1]",
      "N2"   ,"[N1, PH1]",
      "PH1"  ,"[AB2, HX9]",
      "O2"   ,"[]",
      "C12"  ,"[]",
    };
    expectDownstream = new String[]{
      "PH1"  ,"[ZD10, WD40, N2]",
      "H2"   ,"[ZD10, OX12]",
      "N1"   ,"[N2]",
      "CC61" ,"[WD40]",
      "N2"   ,"[ZD10, OX12]",
      "WD40" ,"[]",
    };
    checkDAG(dag,expectS,expectUpstream,expectDownstream);
  }

  // Check that adding an ID with an empty set of upstream
  // dependencies should not affect the dag
  @Test public void dag_add_empty_no_change1(){
    String expectS, expectUpstream[], expectDownstream[];
    DAG dag = new DAG();
    dag.add("A1",toSet());
    dag.add("B1",toSet());
    dag.add("C1",toSet());
    expectS =
      "Upstream Links:\n"+
      "Downstream Links:\n"+
      "";
    expectUpstream = new String[]{
      "A1","[]",
      "B1","[]",
      "C1","[]",
    };
    expectDownstream = new String[]{
      "A1","[]",
      "B1","[]",
      "C1","[]",
    };
    checkDAG(dag,expectS,expectUpstream,expectDownstream);
  }
  @Test public void dag_add_empty_no_change2(){
    String expectS, expectUpstream[], expectDownstream[];
    DAG dag = new DAG();
    dag.add("A1",toSet("B1","C1"));
    dag.add("B1",toSet());
    dag.add("C1",toSet("B1","D2"));
    dag.add("D2",toSet());
    expectS =
      "Upstream Links:\n"+
      "  A1 : [C1, B1]\n"+
      "  C1 : [D2, B1]\n"+
      "Downstream Links:\n"+
      "  C1 : [A1]\n"+
      "  D2 : [C1]\n"+
      "  B1 : [A1, C1]\n"+
      "";
    expectUpstream = new String[]{
      "A1","[C1, B1]",
      "B1","[]",
      "C1","[D2, B1]",
    };
    expectDownstream = new String[]{
      "C1","[A1]",
      "B1","[A1, C1]",
      "D2","[C1]",
      "A1","[]",
    };
    checkDAG(dag,expectS,expectUpstream,expectDownstream);
  }
 
  // Check that adding an existing node should replace its current
  // upstream/downstream dependencies
  @Test public void dag_add_replace1(){
    String expectS, expectUpstream[], expectDownstream[];
    DAG dag = new DAG();
    dag.add("A1",toSet("B1"));
    expectS =
      "Upstream Links:\n"+
      "  A1 : [B1]\n"+
      "Downstream Links:\n"+
      "  B1 : [A1]\n"+
      "";
    expectUpstream = new String[]{
      "A1","[B1]",
      "B1","[]",
      "C1","[]",
    };
    expectDownstream = new String[]{
      "B1","[A1]",
      "A1","[]",
      "C1","[]",
    };
    checkDAG(dag,expectS,expectUpstream,expectDownstream);

    dag.add("A1",toSet("C1")); // replace existing
    expectS =
      "Upstream Links:\n"+
      "  A1 : [C1]\n"+
      "Downstream Links:\n"+
      "  C1 : [A1]\n"+
      "";
    expectUpstream = new String[]{
      "A1","[C1]",
      "B1","[]",
    };
    expectDownstream = new String[]{
      "B1","[]",
      "A1","[]",
      "C1","[A1]",
    };
    checkDAG(dag,expectS,expectUpstream,expectDownstream);
  } 
  @Test public void dag_add_replace2(){
    String expectS, expectUpstream[], expectDownstream[];
    DAG dag = new DAG();
    dag.add("A1",toSet("B1"));
    dag.add("B1",toSet("C1"));
    dag.add("C1",toSet("D1"));
    dag.add("A1",toSet("C1")); // replace existing
    expectS =
      "Upstream Links:\n"+
      "  A1 : [C1]\n"+
      "  C1 : [D1]\n"+
      "  B1 : [C1]\n"+
      "Downstream Links:\n"+
      "  D1 : [C1]\n"+
      "  C1 : [A1, B1]\n"+
      "";
    expectUpstream = new String[]{
      "A1","[C1]",
      "C1","[D1]",
      "B1","[C1]",
    };
    expectDownstream = new String[]{
      "D1","[C1]",
      "C1","[A1, B1]",
    };
    checkDAG(dag,expectS,expectUpstream,expectDownstream);
  } 
  @Test public void dag_add_replace3(){
    String expectS, expectUpstream[], expectDownstream[];
    DAG dag = new DAG();
    dag.add("A1",toSet("B1","C1","D1"));
    dag.add("B1",toSet("C1","D1"));
    dag.add("C1",toSet("D1"));
    dag.add("D1",toSet("E1","F1","G1","H1"));
    dag.add("C1",toSet("F1","H1","X1")); // replace existing
    dag.add("B1",toSet("D1","H1"));      // replace existing
    expectS =
      "Upstream Links:\n"+
      "  A1 : [D1, C1, B1]\n"+
      "  D1 : [H1, G1, F1, E1]\n"+
      "  C1 : [H1, X1, F1]\n"+
      "  B1 : [H1, D1]\n"+
      "Downstream Links:\n"+
      "  H1 : [D1, C1, B1]\n"+
      "  X1 : [C1]\n"+
      "  G1 : [D1]\n"+
      "  F1 : [D1, C1]\n"+
      "  E1 : [D1]\n"+
      "  D1 : [A1, B1]\n"+
      "  C1 : [A1]\n"+
      "  B1 : [A1]\n"+
      "";
    expectUpstream = new String[]{
      "A1","[D1, C1, B1]",
      "D1","[H1, G1, F1, E1]",
      "C1","[H1, X1, F1]",
      "B1","[H1, D1]",
    };
    expectDownstream = new String[]{
      "D1","[A1, B1]",
      "H1","[D1, C1, B1]",
      "A1","[]",
    };
    checkDAG(dag,expectS,expectUpstream,expectDownstream);
  } 
  @Test public void dag_add_replace4(){
    String expectS, expectUpstream[], expectDownstream[];
    DAG dag = new DAG();
    dag.add("A1",toSet("B1","C1","D1"));
    dag.add("B1",toSet("C1","D1"));
    dag.add("C1",toSet("D1"));
    dag.add("A1",toSet("D1"));  // replace existing
    dag.add("B1",toSet("D1"));  // replace existing
    dag.add("C1",toSet());      // replace existing
    expectS =
      "Upstream Links:\n"+
      "  A1 : [D1]\n"+
      "  B1 : [D1]\n"+
      "Downstream Links:\n"+
      "  D1 : [A1, B1]\n"+
      "";
    expectUpstream = new String[]{
      "A1","[D1]",
      "B1","[D1]",
    };
    expectDownstream = new String[]{
      "D1","[A1, B1]",
    };
    checkDAG(dag,expectS,expectUpstream,expectDownstream);
  } 

  // Ensure that removing non-existent IDs from the dag has no effect
  @Test public void dag_add_remove_nonexistent1(){
    String expectS, expectUpstream[], expectDownstream[];
    DAG dag = new DAG();
    dag.add("A1",toSet("B1"));
    dag.add("B1",toSet("C1"));
    dag.remove("C1");
    dag.remove("D1");
    expectS =
      "Upstream Links:\n"+
      "  A1 : [B1]\n"+
      "  B1 : [C1]\n"+
      "Downstream Links:\n"+
      "  C1 : [B1]\n"+
      "  B1 : [A1]\n"+
      "";
    expectUpstream = new String[]{
      "A1","[B1]",
      "B1","[C1]",
      "C1","[]",
    };
    expectDownstream = new String[]{
      "A1","[]",
      "B1","[A1]",
      "C1","[B1]",
    };
    checkDAG(dag,expectS,expectUpstream,expectDownstream);
  } 

  // Check add followed by remove works as expected
  @Test public void dag_add_remove1(){
    String expectS, expectUpstream[], expectDownstream[];
    DAG dag = new DAG();
    dag.add("A1",toSet("B1"));
    dag.remove("A1");
    expectS =
      "Upstream Links:\n"+
      "Downstream Links:\n"+
      "";
    expectUpstream = new String[]{
      "A1","[]",
      "B1","[]",
    };
    expectDownstream = new String[]{
      "A1","[]",
      "B1","[]",
    };
    checkDAG(dag,expectS,expectUpstream,expectDownstream);
  } 
  @Test public void dag_add_remove2(){
    String expectS, expectUpstream[], expectDownstream[];
    DAG dag = new DAG();
    dag.add("A1",toSet("B1"));
    dag.remove("B1");
    dag.remove("C1");
    expectS =
      "Upstream Links:\n"+
      "  A1 : [B1]\n"+
      "Downstream Links:\n"+
      "  B1 : [A1]\n"+
      "";
    expectUpstream = new String[]{
      "A1","[B1]",
      "B1","[]",
      "C1","[]",
    };
    expectDownstream = new String[]{
      "A1","[]",
      "B1","[A1]",
      "C1","[]",
    };
    checkDAG(dag,expectS,expectUpstream,expectDownstream);
  } 
  @Test public void dag_add_remove3(){
    String expectS, expectUpstream[], expectDownstream[];
    DAG dag = new DAG();
    dag.add("A1",toSet("B1"));
    dag.add("B1",toSet("C1","D1"));
    dag.remove("B1");
    expectS =
      "Upstream Links:\n"+
      "  A1 : [B1]\n"+
      "Downstream Links:\n"+
      "  B1 : [A1]\n"+
      "";
    expectUpstream = new String[]{
      "A1","[B1]",
      "B1","[]",
      "C1","[]",
    };
    expectDownstream = new String[]{
      "A1","[]",
      "B1","[A1]",
      "C1","[]",
    };
    checkDAG(dag,expectS,expectUpstream,expectDownstream);
  } 
  @Test public void dag_add_remove4(){
    String expectS, expectUpstream[], expectDownstream[];
    DAG dag = new DAG();
    dag.add("A1",toSet("B1"));
    dag.add("B1",toSet("C1","D1"));
    dag.remove("B1");
    dag.remove("A1");
    expectS =
      "Upstream Links:\n"+
      "Downstream Links:\n"+
      "";
    expectUpstream = new String[]{
      "A1","[]",
      "B1","[]",
      "C1","[]",
    };
    expectDownstream = new String[]{
      "A1","[]",
      "B1","[]",
      "C1","[]",
    };
    checkDAG(dag,expectS,expectUpstream,expectDownstream);
  } 
  @Test public void dag_add_remove5(){
    String expectS, expectUpstream[], expectDownstream[];
    DAG dag = new DAG();
    dag.add("A1",toSet("B1","C1","D1"));
    dag.add("B1",toSet("E1","C1","D1","F1"));
    dag.add("F1",toSet("E1","D1","G1"));
    dag.add("R2",toSet("A1"));
    dag.remove("G1");
    dag.remove("A1");
    expectS =
      "Upstream Links:\n"+
      "  R2 : [A1]\n"+
      "  F1 : [G1, E1, D1]\n"+
      "  B1 : [F1, E1, D1, C1]\n"+
      "Downstream Links:\n"+
      "  A1 : [R2]\n"+
      "  G1 : [F1]\n"+
      "  F1 : [B1]\n"+
      "  E1 : [F1, B1]\n"+
      "  D1 : [F1, B1]\n"+
      "  C1 : [B1]\n"+
      "";
    expectUpstream = new String[]{
      "A1","[]",
      "B1","[F1, E1, D1, C1]",
      "F1","[G1, E1, D1]",
      "R2","[A1]",
    };
    expectDownstream = new String[]{
      "A1","[R2]",
      "G1","[F1]",
      "F1","[B1]",
      "E1","[F1, B1]",
      "D1","[F1, B1]",
      "C1","[B1]",
    };
    checkDAG(dag,expectS,expectUpstream,expectDownstream);
  } 
  @Test public void dag_add_remove6(){
    String expectS, expectUpstream[], expectDownstream[];
    DAG dag = new DAG();
    dag.add("A1",toSet("B1","C1","D1"));
    dag.add("B1",toSet("E1","C1","D1","F1"));
    dag.add("F1",toSet("E1","D1","G1"));
    dag.add("R2",toSet("A1"));
    dag.remove("G1");
    dag.remove("A1");
    dag.remove("B1");
    expectS =
      "Upstream Links:\n"+
      "  R2 : [A1]\n"+
      "  F1 : [G1, E1, D1]\n"+
      "Downstream Links:\n"+
      "  A1 : [R2]\n"+
      "  G1 : [F1]\n"+
      "  E1 : [F1]\n"+
      "  D1 : [F1]\n"+
      "";
    expectUpstream = new String[]{
      "A1","[]",
      "B1","[]",
      "F1","[G1, E1, D1]",
      "R2","[A1]",
    };
    expectDownstream = new String[]{
      "A1","[R2]",
      "G1","[F1]",
      "E1","[F1]",
      "D1","[F1]",
    };
    checkDAG(dag,expectS,expectUpstream,expectDownstream);
  } 
  @Test public void dag_add_remove7(){
    String expectS, expectUpstream[], expectDownstream[];
    DAG dag = new DAG();
    dag.add("A1",toSet("B1","C1","D1"));
    dag.add("B1",toSet("E1","C1","D1","F1"));
    dag.add("F1",toSet("E1","D1","G1"));
    dag.add("R2",toSet("A1"));
    dag.remove("G1");
    dag.remove("A1");
    dag.remove("B1");
    dag.remove("F1");
    expectS =
      "Upstream Links:\n"+
      "  R2 : [A1]\n"+
      "Downstream Links:\n"+
      "  A1 : [R2]\n"+
      "";
    expectUpstream = new String[]{
      "A1","[]",
      "B1","[]",
      "F1","[]",
      "R2","[A1]",
    };
    expectDownstream = new String[]{
      "A1","[R2]",
      "G1","[]",
      "E1","[]",
      "D1","[]",
    };
    checkDAG(dag,expectS,expectUpstream,expectDownstream);
  } 

  // Remove all downstream nodes from a node 
  @Test public void dag_add_remove8(){
    String expectS, expectUpstream[], expectDownstream[];
    DAG dag = new DAG();
    dag.add("A1",toSet("B1","C1"));
    dag.add("B1",toSet("D1","C1"));
    dag.add("D1",toSet("E1"));
    dag.remove("B1");
    dag.remove("D1");
    expectS =
      "Upstream Links:\n"+
      "  A1 : [B1, C1]\n"+
      "Downstream Links:\n"+
      "  B1 : [A1]\n"+
      "  C1 : [A1]\n"+
      "";
    expectUpstream = new String[]{
      "A1","[B1, C1]",
    };
    expectDownstream = new String[]{
      "A1","[]",
      "B1","[A1]",
      "C1","[A1]",
    };
    checkDAG(dag,expectS,expectUpstream,expectDownstream);
  } 
  @Test public void dag_add_remove9(){
    String expectS, expectUpstream[], expectDownstream[];
    DAG dag = new DAG();
    dag.add("A1",toSet("B1","C1","D1"));
    dag.add("B1",toSet("E1","C1","D1","F1"));
    dag.add("F1",toSet("E1","D1","G1"));
    dag.add("R2",toSet("A1"));
    dag.remove("G1");
    dag.remove("F1");
    dag.remove("R2");
    expectS =
      "Upstream Links:\n"+
      "  A1 : [B1, C1, D1]\n"+
      "  B1 : [C1, D1, E1, F1]\n"+
      "Downstream Links:\n"+
      "  B1 : [A1]\n"+
      "  C1 : [A1, B1]\n"+
      "  D1 : [A1, B1]\n"+
      "  E1 : [B1]\n"+
      "  F1 : [B1]\n"+
      "";
    expectUpstream = new String[]{
      "A1","[B1, C1, D1]",
      "B1","[C1, D1, E1, F1]",
      "F1","[]",
      "R2","[]",
    };
    expectDownstream = new String[]{
      "A1","[]",
      "G1","[]",
      "F1","[B1]",
      "E1","[B1]",
      "D1","[A1, B1]",
      "C1","[A1, B1]",
    };
    checkDAG(dag,expectS,expectUpstream,expectDownstream);
  } 
  @Test public void dag_add_remove10(){
    String expectS, expectUpstream[], expectDownstream[];
    DAG dag = new DAG();
    dag.add("A1",toSet("B1","C1","D1"));
    dag.add("B1",toSet("E1","C1","D1","F1"));
    dag.add("F1",toSet("E1","D1","G1"));
    dag.add("R2",toSet("A1"));
    dag.remove("G1");
    dag.remove("F1");
    dag.remove("B1");
    dag.remove("A1");
    dag.remove("R2");
    expectS =
      "Upstream Links:\n"+
      "Downstream Links:\n"+
      "";
    expectUpstream = new String[]{
      "A1","[]",
      "B1","[]",
      "F1","[]",
      "R2","[]",
    };
    expectDownstream = new String[]{
      "A1","[]",
      "G1","[]",
      "F1","[]",
      "E1","[]",
      "D1","[]",
      "C1","[]",
    };
    checkDAG(dag,expectS,expectUpstream,expectDownstream);
  } 

  // Test that multiple DAGs can coexist
  @Test public void dag_multiple_add_remove1(){
    String expectS, expectUpstream[], expectDownstream[];
    DAG dag1 = new DAG();
    DAG dag2 = new DAG();
    // Adjust dag1
    dag1.add("A1",toSet("B1","C1","D1"));
    // Adjust dag2
    dag2.add("A1",toSet("B1","C1","D1"));
    dag2.add("B1",toSet("E1","C1","D1","F1"));
    dag2.add("F1",toSet("E1","D1","G1"));
    dag2.add("R2",toSet("A1"));
    dag2.remove("G1");
    dag2.remove("A1");
    // Check dag1
    expectS =
      "Upstream Links:\n"+
      "  A1 : [D1, C1, B1]\n"+
      "Downstream Links:\n"+
      "  C1 : [A1]\n"+
      "  B1 : [A1]\n"+
      "  D1 : [A1]\n"+
      "";
    expectUpstream = new String[]{
      "A1","[D1, C1, B1]",
      "D1","[]",
      "X2","[]",
    };
    expectDownstream = new String[]{
      "D1","[A1]",
      "C1","[A1]",
      "B1","[A1]",
      "A1","[]",
      "X2","[]",
    };
    checkDAG(dag1,expectS,expectUpstream,expectDownstream);
    // Check dag2
    expectS =
      "Upstream Links:\n"+
      "  R2 : [A1]\n"+
      "  F1 : [G1, E1, D1]\n"+
      "  B1 : [F1, E1, D1, C1]\n"+
      "Downstream Links:\n"+
      "  A1 : [R2]\n"+
      "  G1 : [F1]\n"+
      "  F1 : [B1]\n"+
      "  E1 : [F1, B1]\n"+
      "  D1 : [F1, B1]\n"+
      "  C1 : [B1]\n"+
      "";
    expectUpstream = new String[]{
      "A1","[]",
      "B1","[F1, E1, D1, C1]",
      "F1","[G1, E1, D1]",
      "R2","[A1]",
    };
    expectDownstream = new String[]{
      "A1","[R2]",
      "G1","[F1]",
      "F1","[B1]",
      "E1","[F1, B1]",
      "D1","[F1, B1]",
      "C1","[B1]",
    };
    checkDAG(dag2,expectS,expectUpstream,expectDownstream);
  }
  @Test public void dag_multiple_add_remove2(){
    String expectS, expectUpstream[], expectDownstream[];
    // Create and modify two differeng dags
    DAG dag1 = new DAG();
    DAG dag2 = new DAG();
    dag1.add("A1",toSet("B1","C1","D1"));
    dag1.add("B1",toSet("E1","C1","D1","F1"));
    dag1.add("F1",toSet("E1","D1","G1"));
    dag1.add("R2",toSet("A1"));
    dag2.add("A1",toSet("B1","C1","D1"));
    dag2.add("B1",toSet("E1","C1","D1","F1"));
    dag1.remove("G1");
    dag2.add("F1",toSet("E1","D1","G1"));
    dag2.add("R2",toSet("A1"));
    dag2.remove("G1");
    dag2.remove("A1");
    dag2.remove("B1");
    dag1.remove("A1");
    // check dag1
    expectS =
      "Upstream Links:\n"+
      "  R2 : [A1]\n"+
      "  F1 : [G1, E1, D1]\n"+
      "  B1 : [F1, E1, D1, C1]\n"+
      "Downstream Links:\n"+
      "  A1 : [R2]\n"+
      "  G1 : [F1]\n"+
      "  F1 : [B1]\n"+
      "  E1 : [F1, B1]\n"+
      "  D1 : [F1, B1]\n"+
      "  C1 : [B1]\n"+
      "";
    expectUpstream = new String[]{
      "A1","[]",
      "B1","[F1, E1, D1, C1]",
      "F1","[G1, E1, D1]",
      "R2","[A1]",
    };
    expectDownstream = new String[]{
      "A1","[R2]",
      "G1","[F1]",
      "F1","[B1]",
      "E1","[F1, B1]",
      "D1","[F1, B1]",
      "C1","[B1]",
    };
    checkDAG(dag1,expectS,expectUpstream,expectDownstream);
    // check dag2
    expectS =
      "Upstream Links:\n"+
      "  R2 : [A1]\n"+
      "  F1 : [G1, E1, D1]\n"+
      "Downstream Links:\n"+
      "  A1 : [R2]\n"+
      "  G1 : [F1]\n"+
      "  E1 : [F1]\n"+
      "  D1 : [F1]\n"+
      "";
    expectUpstream = new String[]{
      "A1","[]",
      "B1","[]",
      "F1","[G1, E1, D1]",
      "R2","[A1]",
    };
    expectDownstream = new String[]{
      "A1","[R2]",
      "G1","[F1]",
      "E1","[F1]",
      "D1","[F1]",
    };
    checkDAG(dag2,expectS,expectUpstream,expectDownstream);

  }

  ////////////////////////////////////////////////////////////////////////////////
  // Cycle Detection

  // Utility to create a map based on arguments. Invoke as
  // 
  //   Map<String,Set<String>> map = toMap("A1","B1 C1","B1","C1 D1 E1","D1","E1");
  // 
  @SuppressWarnings("unchecked")
  public static Map<String,Set<String>> toMap(String... args){
    Map<String,Set<String>> map = new HashMap<String,Set<String>>();
    for(int i=0; i<args.length; i+=2){
      String id = (String) args[i];
      Set<String> set = new HashSet<String>();
      Scanner in = new Scanner(args[i+1]);
      while(in.hasNext()){
        set.add(in.next());
      }
      map.put(id,set);
    }
    return map;
  }

  // Utility to easily create a list (not used)
  public static List<String> toList(String... args){
    LinkedList<String> list = new LinkedList<String>();
    for(String s : args){
      list.add(s);
    }
    return list;
  }


  // Utility to test checkForCycle(..) behavior; checks that a specific path is found in the 
  public static void test_checkForCycle(String links[], String start, boolean expectCycle, String expectPathS){
    Map<String,Set<String>> linkMap = toMap(links);
    List<String> actualPath = new LinkedList<String>();
    actualPath.add(start);
    boolean actualCycle = DAG.checkForCycles(linkMap, actualPath);
    String actualPathS = actualPath.toString();

    boolean pass = true;
    pass = pass && actualCycle==expectCycle;
    pass = pass && actualPathS.equals(expectPathS);

    if(pass) { return; }

    // Failed test
    StringBuilder linksS = new StringBuilder();
    for(int i=0; i<links.length; i+=2){
      linksS.append(String.format("%4s : %s\n",links[i],linkMap.get(links[i])));
    }
    String msg =
      String.format("Incorrect cycle detection\n")+
      String.format("Links:\n%s\n",linksS.toString())+
      String.format("Start: %s\n",start)+
      String.format("Expect Cycle/Path: %5s %s\nActual Cycle/Path: %5s %s\n",expectCycle,expectPathS,actualCycle,actualPathS)+
      "";
    fail(msg);
  }

  // Utility to test checkForCycle(..) behavior; checks that a specific path is found in the 
  public static void test_checkForCycle2(String links[], String start, boolean expectHasCycle, Set<String> expectCycles){
    Map<String,Set<String>> linkMap = toMap(links);
    List<String> actualCycle = new LinkedList<String>();
    actualCycle.add(start);
    boolean actualHasCycle = DAG.checkForCycles(linkMap, actualCycle);

    boolean pass = true;
    pass = pass && actualHasCycle==expectHasCycle;
    pass = pass && expectCycles.contains(actualCycle.toString());

    if(pass) { return; }

    // Failed test
    StringBuilder linksS = new StringBuilder();
    for(int i=0; i<links.length; i+=2){
      linksS.append(String.format("%4s : %s\n",links[i],linkMap.get(links[i])));
    }
    String msg =
      String.format("Incorrect cycle detection\n")+
      String.format("Links:\n%s\n",linksS.toString())+
      String.format("Start: %s\n",start)+
      String.format("Actual Has Cycle: %s\n",actualHasCycle)+
      String.format("Actual Cycle:     %s\n",actualCycle)+
      String.format("Expect Has Cycle: %s\n",expectHasCycle)+
      String.format("Possible Cycles:  %s\n",expectCycles.toString())+
      "";

    fail(msg);
  }
        
  @Test public void checkForCycles_cycle1(){
    // String start, expectPathS; boolean expectCycle;

    String [] links = {
      "A1","A1",
    };
    String start = "A1";
    boolean expectHasCycle = true;
    Set<String> expectCycles = toSet("[A1, A1]");
    test_checkForCycle2(links,start,expectHasCycle,expectCycles);
  }
  @Test public void checkForCycles_cycle2(){
    String start; Set<String> expectCycles; boolean expectHasCycle;
    String [] links = {
      "A1","B1",
      "B1","A1",
    };
    start = "A1";
    expectHasCycle = true;
    // expectPathS = "[A1, B1, A1]";
    expectCycles = toSet("[A1, B1, A1]");
    test_checkForCycle2(links,start,expectHasCycle,expectCycles);
  }
  @Test public void checkForCycles_cycle3(){
    String start; Set<String> expectCycles; boolean expectHasCycle;
    String [] links = {
      "A1","B1",
      "B1","C1",
      "C1","D1",
      "D1","E1",
      "E1","A1",
    };
    start = "A1";
    expectHasCycle = true;
    // expectPathS = "[A1, B1, C1, D1, E1, A1]";
    expectCycles = toSet("[A1, B1, C1, D1, E1, A1]");
    test_checkForCycle2(links,start,expectHasCycle,expectCycles);
  }
  @Test public void checkForCycles_cycle4(){
    String start; Set<String> expectCycles; boolean expectHasCycle;
    String [] links = {
      "A1","B1",
      "B1","C1",
      "C1","D1",
      "D1","E1",
      "E1","A1",
    };
    start = "C1";
    expectHasCycle = true;
    // expectPathS = "[C1, D1, E1, A1, B1, C1]";
    expectCycles = toSet("[C1, D1, E1, A1, B1, C1]");
    test_checkForCycle2(links,start,expectHasCycle,expectCycles);
  }
  @Test public void checkForCycles_cycle5(){
    String start; Set<String> expectCycles; boolean expectHasCycle;
    String [] links = {
      "A1","B1 D1",
      "B1","C1",
      "D1","E1 F1 G1",
      "E1","F1",
      "G1","B1 C1 A1",
    };
    start = "A1";
    expectHasCycle = true;
    // expectPathS = "[A1, D1, G1, A1]";
    expectCycles = toSet("[A1, D1, G1, A1]");
    test_checkForCycle2(links,start,expectHasCycle,expectCycles);
  }
  @Test public void checkForCycles_cycle6(){
    String start; Set<String> expectCycles; boolean expectHasCycle;
    String [] links = {
      "A1","B1 D1",
      "B1","C1",
      "D1","E1 F1 G1",
      "E1","F1",
      "G1","B1 C1 A1",
    };
    start = "D1";
    expectHasCycle = true;
    // expectPathS = "[D1, G1, A1, D1]";
    expectCycles = toSet("[D1, G1, A1, D1]");
    test_checkForCycle2(links,start,expectHasCycle,expectCycles);
  }
  @Test public void checkForCycles_cycle7(){
    String start; Set<String> expectCycles; boolean expectHasCycle;
    String [] links = {
      "A1","B1 D1",
      "B1","C1",
      "D1","E1 F1 G1",
      "E1","F1",
      "G1","B1 C1",
      "C1","E1 D1",
    };
    start = "C1";
    expectHasCycle = true;
    // expectPathS = "[C1, D1, G1, C1]";
    expectCycles = toSet("[C1, D1, G1, C1]");
    test_checkForCycle2(links,start,expectHasCycle,expectCycles);
  }
  // Multiple cycles exist in these tests
  @Test public void checkForCycles_cycle8(){
    String start; Set<String> expectCycles; boolean expectHasCycle;
    String [] links = {
      "A1","B1 C1",
      "B1","C1",
      "C1","A1 B1 D1",
    };
    start = "A1";
    expectHasCycle = true;
    expectCycles = toSet("[A1, C1, A1]", "[A1, C1, B1, C1]", "[A1, B1, C1, A1]", "[A1, B1, C1, B1]");
    test_checkForCycle2(links,start,expectHasCycle,expectCycles);
  }
  @Test public void checkForCycles_cycle9(){
    String start; Set<String> expectCycles; boolean expectHasCycle;
    String [] links = {
      "A1","B1 C1 D1",
      "B1","D1 E1 F1 G1",
      "C1","G1",
      "D1","E1 F1 G1",
      // "E1","",
      "F1","H1 I1 J1 K1",
      "G1","J1 K1",
      "H1","J1 K1 L1",
      "I1","J1 L1",
      "L1","A1 M1",
    };
    start = "L1";
    expectHasCycle = true;
    // expectPathS = "[L1, A1, D1, F1, I1, L1]";
    expectCycles = toSet("[L1, A1, D1, F1, H1, L1]", "[L1, A1, D1, F1, I1, L1]", "[L1, A1, B1, F1, I1, L1]", "[L1, A1, B1, D1, F1, H1, L1]", "[L1, A1, B1, D1, F1, I1, L1]", "[L1, A1, B1, F1, H1, L1]");
    test_checkForCycle2(links,start,expectHasCycle,expectCycles);
  }


  @Test public void checkForCycles_nocycle1(){
    String start; Set<String> expectCycles; boolean expectHasCycle;
    String [] links = {
      "A1","B1",
      "B1","C1",
      "C1","D1",
      "D1","E1",
    };
    start = "A1";
    expectHasCycle = false;
    expectCycles = toSet("[A1]");
    test_checkForCycle2(links,start,expectHasCycle,expectCycles);
  }
  @Test public void checkForCycles_nocycle2(){
    String start; Set<String> expectCycles; boolean expectHasCycle;
    String [] links = {
      "A1","B1 D1",
      "B1","C1 D1 E1 F1",
      "C1","D1 F1",
      "D1","E1 G1",
    };
    start = "A1";
    expectHasCycle = false;
    expectCycles = toSet("[A1]");
    test_checkForCycle2(links,start,expectHasCycle,expectCycles);
  }
  @Test public void checkForCycles_nocycle3(){
    String start; Set<String> expectCycles; boolean expectHasCycle;
    String [] links = {
      "A1","B1 D1",
      "B1","C1",
      "D1","E1 F1 G1",
      "E1","F1",
      "G1","B1 C1",
    };
    start = "A1";
    expectHasCycle = false;
    expectCycles = toSet("[A1]");
    test_checkForCycle2(links,start,expectHasCycle,expectCycles);
  }
  @Test public void checkForCycles_nocycle4(){
    String start; Set<String> expectCycles; boolean expectHasCycle;
    String [] links = {
      "A1","B1 D1",
      "B1","C1",
      "D1","E1 F1 G1",
      "E1","F1",
      "G1","B1 C1",
    };
    start = "E1";
    expectHasCycle = false;
    expectCycles = toSet("[E1]");
    test_checkForCycle2(links,start,expectHasCycle,expectCycles);
  }
  @Test public void checkForCycles_nocycle5(){
    String start; Set<String> expectCycles; boolean expectHasCycle;
    String [] links = {
      "A1","B1 D1",
      "B1","C1",
      "D1","E1 F1 G1",
      "E1","F1",
      "G1","B1 C1",
    };
    start = "D1";
    expectHasCycle = false;
    expectCycles = toSet("[D1]");
    test_checkForCycle2(links,start,expectHasCycle,expectCycles);
  }

  // Utility to check whether DAG cycle was found or not found
  @SuppressWarnings("unchecked")
  public static void checkDAGCycle(Object additions[],
                                   String expectSunsorted,
                                   boolean expectHasCycle, Set<String> expectCycles)
  {
    DAG dag = new DAG();
    int addI = 0;
    boolean actualHasCycle = false;
    String actualCycle = "";
    try{
      for(addI=0; addI<additions.length; addI+=2){
        String id = (String) additions[addI];
        Set<String> ulinks = (Set<String>) additions[addI+1];
        dag.add(id,ulinks);
      }
    }
    catch(DAG.CycleException e){
      actualHasCycle = true;
      actualCycle = e.getMessage();
    }

    String actualS = sortedLines(dag.toString());
    String expectS = sortedLines(expectSunsorted);

    boolean pass = true;
    pass = pass && expectS.equals(actualS);
    pass = pass && expectHasCycle == actualHasCycle;
    pass = pass && expectCycles.contains(actualCycle);

    if(pass) { return; }

    // Failed test
    StringBuilder additionsS = new StringBuilder();
    for(int i=0; i<additions.length; i+=2){
      additionsS.append(String.format("%2d: dag.add(%4s, %s)\n",i/2,additions[i],additions[i+1]));
      if(i==addI){
        additionsS.deleteCharAt(additionsS.length()-1);
        additionsS.append(" --> CYCLE DETECTED HERE\n");
      }
    }

    String msg =
      String.format("DAG incorrect after possible cycle\n")+
      String.format("ADDITIONS COMPLETED = %d:\n%s\n",addI/2,additionsS.toString())+
      String.format("ACTUAL HAS CYCLE: %s \n",actualHasCycle)+
      String.format("EXPECT HAS CYCLE: %s \n",expectHasCycle)+
      String.format("ACTUAL CYCLE:     %s \n",actualCycle)+
      String.format("POSSIBLE CYCLES:  %s \n",expectCycles)+
      String.format("FINAL DAG EXPECT:\n%s\nFINAL DAG ACTUAL:\n%s\n",expectS,actualS)+
      "";

    fail(msg);
  }

  @Test public void dag_cycle_exception1(){
    String expectS; Set<String> expectCycles; boolean expectHasCycle;
    Object [] additions = {
      "A1",toSet("A1"),
    };
    expectHasCycle  = true;
    expectCycles = toSet("[A1, A1]");
    expectS =
      "Upstream Links:\n"+
      "Downstream Links:\n"+
      "";
    checkDAGCycle(additions,expectS,expectHasCycle,expectCycles);
  } 
  @Test public void dag_cycle_exception2(){
    String expectS; Set<String> expectCycles; boolean expectHasCycle;
    Object [] additions = {
      "A1",toSet("B1"),
      "B1",toSet("A1"),
      "C1",toSet("A1"),
    };
    expectHasCycle  = true;
    expectCycles = toSet("[B1, A1, B1]");
    expectS =
      "Upstream Links:\n"+
      "  A1 : [B1]\n"+
      "Downstream Links:\n"+
      "  B1 : [A1]\n"+
      "";
    checkDAGCycle(additions,expectS,expectHasCycle,expectCycles);
  } 
  @Test public void dag_cycle_exception3(){
    String expectS; Set<String> expectCycles; boolean expectHasCycle;
    Object [] additions = {
      "A1",toSet("B1"),
      "B1",toSet("C1"),
      "C1",toSet("D1"),
      "D1",toSet("A1"),
      "E1",toSet("A1"),
    };
    expectHasCycle  = true;
    expectCycles = toSet("[D1, A1, B1, C1, D1]");
    expectS =
      "Upstream Links:\n"+
      "  A1 : [B1]\n"+
      "  C1 : [D1]\n"+
      "  B1 : [C1]\n"+
      "Downstream Links:\n"+
      "  D1 : [C1]\n"+
      "  C1 : [B1]\n"+
      "  B1 : [A1]\n"+
      "";
    checkDAGCycle(additions,expectS,expectHasCycle,expectCycles);
  } 
  @Test public void dag_cycle_exception4(){
    String expectS; Set<String> expectCycles; boolean expectHasCycle;
    Object [] additions = {
      "A1",toSet("B1","X1","Y1"),
      "B1",toSet("H1","Z1","R1","C1"),
      "C1",toSet("V1","D1","U1"),
      "D1",toSet("Z1","S1","O1","A1"),
      "E1",toSet("A1"),
    };
    expectHasCycle  = true;
    expectCycles = toSet("[D1, A1, B1, C1, D1]");
    expectS =
      "Upstream Links:\n"+
      "  A1 : [Y1, X1, B1]\n"+
      "  C1 : [V1, U1, D1]\n"+
      "  B1 : [Z1, H1, C1, R1]\n"+
      "Downstream Links:\n"+
      "  Z1 : [B1]\n"+
      "  Y1 : [A1]\n"+
      "  X1 : [A1]\n"+
      "  H1 : [B1]\n"+
      "  V1 : [C1]\n"+
      "  U1 : [C1]\n"+
      "  D1 : [C1]\n"+
      "  C1 : [B1]\n"+
      "  B1 : [A1]\n"+
      "  R1 : [B1]\n"+
      "";
    checkDAGCycle(additions,expectS,expectHasCycle,expectCycles);
      // "  H1 : [B1]\n"+
      // "  D1 : [C1]\n"+
      // "  C1 : [B1]\n"+
      // "  B1 : [A1]\n"+
      // "  Z1 : [B1]\n"+
      // "  Y1 : [A1]\n"+
      // "  X1 : [A1]\n"+
      // "  V1 : [C1]\n"+
      // "  U1 : [C1]\n"+
      // "  R1 : [B1]\n"+
      // "";
  } 
  @Test public void dag_cycle_exception5(){
    String expectS; Set<String> expectCycles; boolean expectHasCycle;
    Object [] additions = {
      "A1",toSet("B1","D1","E1"),
      "B1",toSet("D1","F1","G1","C1"),
      "C1",toSet("E1","H1"),
      "D1",toSet("F1","G1","I1"),
      "E1",toSet("B1","F1"),
      "F1",toSet("C1"),
    };
    expectHasCycle  = true;
    expectCycles = toSet("[E1, B1, C1, E1]");
    expectS =
      "Upstream Links:\n"+
      "  A1 : [E1, D1, B1]\n"+
      "  D1 : [I1, G1, F1]\n"+
      "  C1 : [H1, E1]\n"+
      "  B1 : [G1, F1, D1, C1]\n"+
      "Downstream Links:\n"+
      "  I1 : [D1]\n"+
      "  H1 : [C1]\n"+
      "  G1 : [D1, B1]\n"+
      "  F1 : [D1, B1]\n"+
      "  E1 : [A1, C1]\n"+
      "  D1 : [A1, B1]\n"+
      "  C1 : [B1]\n"+
      "  B1 : [A1]\n"+
      "";
    checkDAGCycle(additions,expectS,expectHasCycle,expectCycles);
  } 
  @Test public void dag_cycle_exception6(){
    String expectS; Set<String> expectCycles; boolean expectHasCycle;
    Object [] additions = {
      "A1",toSet("B1"),
      "A1",toSet("A1"),
      "B1",toSet("C1"),
    };
    expectHasCycle  = true;
    expectCycles = toSet("[A1, A1]");
    expectS =
      "Upstream Links:\n"+
      "  A1 : [B1]\n"+
      "Downstream Links:\n"+
      "  B1 : [A1]\n"+
      "";
    checkDAGCycle(additions,expectS,expectHasCycle,expectCycles);
  } 
  @Test public void dag_cycle_exception7(){
    String expectS; Set<String> expectCycles; boolean expectHasCycle;
    Object [] additions = {
      "A1",toSet("B1"),
      "B1",toSet("C1"),
      "C1",toSet("D1"),
      "C1",toSet("A1"),
      "XX",toSet("YY"),
    };
    expectHasCycle  = true;
    expectCycles = toSet("[C1, A1, B1, C1]");
    expectS =
      "Upstream Links:\n"+
      "  A1 : [B1]\n"+
      "  C1 : [D1]\n"+
      "  B1 : [C1]\n"+
      "Downstream Links:\n"+
      "  D1 : [C1]\n"+
      "  C1 : [B1]\n"+
      "  B1 : [A1]\n"+
      "";
    checkDAGCycle(additions,expectS,expectHasCycle,expectCycles);
  } 
  // Multiple cycles possible
  @Test public void dag_cycle_exception8(){
    String expectS; Set<String> expectCycles; boolean expectHasCycle;
    Object [] additions = {
      "A1",toSet("B1","C1"),
      "B1",toSet("C1"),
      "C1",toSet("D1","E1"),
      "D1",toSet("A1"),
    };
    expectHasCycle  = true;
    expectCycles = toSet("[D1, A1, B1, C1, D1]", "[D1, A1, C1, D1]");
    expectS =
      "Upstream Links:\n"+
      "  A1 : [B1, C1]\n"+
      "  B1 : [C1]\n"+
      "  C1 : [D1, E1]\n"+
      "Downstream Links:\n"+
      "  B1 : [A1]\n"+
      "  C1 : [A1, B1]\n"+
      "  D1 : [C1]\n"+
      "  E1 : [C1]\n"+
      "";
    checkDAGCycle(additions,expectS,expectHasCycle,expectCycles);
  } 
  @Test public void dag_cycle_exception10(){
    String expectS; Set<String> expectCycles; boolean expectHasCycle;
    Object [] additions = {
      "A1",toSet("B1","C1"),
      "B1",toSet("C1"),
      "C1",toSet("D1"),
      "D1",toSet("X1","E1"),
      "E1",toSet("Z1","A1","D1"),
    };
    expectHasCycle  = true;
    expectCycles = toSet("[E1, A1, B1, C1, D1, E1]", "[E1, D1, E1]", "[E1, A1, C1, D1, E1]");
    expectS =
      "Upstream Links:\n"+
      "  A1 : [B1, C1]\n"+
      "  B1 : [C1]\n"+
      "  C1 : [D1]\n"+
      "  D1 : [E1, X1]\n"+
      "Downstream Links:\n"+
      "  B1 : [A1]\n"+
      "  C1 : [A1, B1]\n"+
      "  D1 : [C1]\n"+
      "  E1 : [D1]\n"+
      "  X1 : [D1]\n"+
      "";
    checkDAGCycle(additions,expectS,expectHasCycle,expectCycles);
  } 

  @Test public void dag_nocycle_exception1(){
    String expectS; Set<String> expectCycles; boolean expectHasCycle;
    Object [] additions = {
      "A1",toSet("B1"),
      "B1",toSet("C1"),
      "C1",toSet("D1"),
      "A1",toSet(),
      "C1",toSet("A1"),
    };
    expectHasCycle  = false;
    expectCycles = toSet("");
    expectS =
      "Upstream Links:\n"+
      "  C1 : [A1]\n"+
      "  B1 : [C1]\n"+
      "Downstream Links:\n"+
      "  A1 : [C1]\n"+
      "  C1 : [B1]\n"+
      "";
    checkDAGCycle(additions,expectS,expectHasCycle,expectCycles);
  } 
  @Test public void dag_nocycle_exception2(){
    String expectS; Set<String> expectCycles; boolean expectHasCycle;
    Object [] additions = {
      "A1",toSet("B1","D1","E1"),
      "B1",toSet("D1","F1","G1","C1"),
      "C1",toSet("E1","H1"),
      "D1",toSet("F1","G1","I1"),
      "C1",toSet("H1","G1"),
      "E1",toSet("B1","F1"),
      "F1",toSet("C1"),
    };
    expectHasCycle  = false;
    expectCycles = toSet("");
    expectS =
      "Upstream Links:\n"+
      "  A1 : [E1, D1, B1]\n"+
      "  F1 : [C1]\n"+
      "  E1 : [F1, B1]\n"+
      "  D1 : [I1, G1, F1]\n"+
      "  C1 : [H1, G1]\n"+
      "  B1 : [G1, F1, D1, C1]\n"+
      "Downstream Links:\n"+
      "  I1 : [D1]\n"+
      "  H1 : [C1]\n"+
      "  G1 : [D1, C1, B1]\n"+
      "  F1 : [E1, D1, B1]\n"+
      "  E1 : [A1]\n"+
      "  D1 : [A1, B1]\n"+
      "  C1 : [F1, B1]\n"+
      "  B1 : [A1, E1]\n"+
      "";
    checkDAGCycle(additions,expectS,expectHasCycle,expectCycles);
  } 
 
}
