import java.util.*;
public class FillAtBeginning{
  public static <T> void fillAtBeginning(ArrayList<T> a, T fillElem, int nFills){
    for(int fills=0; fills<nFills; fills++){
      a.add(fillElem);
      for(int i=a.size()-1; i>0; i--){
        a.set(i, a.get(i-1));
      }
      a.set(0, fillElem);
    }
  }

  public static void main(String args[]){
    ArrayList<Integer> a = new ArrayList<Integer>();
    for(int i=1; i<=5; i++){
      a.add(i);
    }
    System.out.println(a);
    // [1, 2, 3, 4, 5]

    fillAtBeginning(a, -1, 4);
    System.out.println(a);
    // [-1, -1, -1, -1, 1, 2, 3, 4, 5]    

    fillAtBeginning(a, -2, 7);
    System.out.println(a);
    // [-2, -2, -2, -2, -2, -2, -2, -1, -1, -1, -1, 1, 2, 3, 4, 5]
  }
}
    
