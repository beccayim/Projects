import java.util.*;

// Provides a main method which will play an interactive game of
// Gomoku with an arbitrary winning sequence length and number of
// players
public class Gomoku {

  public static void main(String args[]){
    Scanner in = new Scanner(System.in);

    int winSeqLength = 0;
    while(winSeqLength <= 0){
      System.out.printf("Enter length of winning sequence (int > 0, standard 5): ");
      winSeqLength = in.nextInt();
    }

    int nPlayers = 0;
    while(nPlayers <= 0){
      System.out.printf("Enter number of players (int > 0): ");
      nPlayers = in.nextInt();
    }

    ArrayList<String> playerSymbols = new ArrayList<String>();
    playerSymbols.add("Dummy"); // Player 1 will be in slot 1 now
    for(int i=1; i<=nPlayers; i++){
      System.out.printf("Enter symbol for player %d (2 char max): ",i);
      String symbol = in.next();
      if(symbol.length() > 2 || symbol.length()==0){
        System.out.printf("2 character max for symbols, non-empty string required, try again.\n");
        i--;
      }
      else{
        playerSymbols.add(symbol);
      }
    }

    System.out.println();

    DenseBoard<String> board = new DenseBoard<String>(1,3,1,3,"");

    int playerTurn = 1;
    int round = 1;
    while(true){
      System.out.println();

      System.out.printf("%s\n",board.toString());

      // Check for a win
      List<RowColElem<String>> longestSeq = board.getLongestSequence();
      if(longestSeq.size() >= winSeqLength){
        playerTurn--;
        if(playerTurn == 0){
          playerTurn = nPlayers;
        }
        System.out.printf("Player %d with sybmol %s wins!\nWinning sequence: %s\n",
                          playerTurn, playerSymbols.get(playerTurn),longestSeq);
        break;
      }

      // Continue playing; get a move from the player
      System.out.printf("Round %d Player %d (%s) turn; move (h for help): ",
                        round,playerTurn,playerSymbols.get(playerTurn));
      String move = in.next();


      if(move.equals("h") || move.equals("help")){
        System.out.println("Help on moves:                                                           ");
        System.out.println("  s or set: set an element give row and column, ends turn                ");
        System.out.println("            May set elements outside of board which causes expansion     ");
        System.out.println("            Examples                                                     ");
        System.out.println("            s 3 -2     // set element 3,-2                               ");
        System.out.println("            set 0 +5   // set element 0,+5                               ");
        System.out.println("  e or expand: expand the board, give row and column, turn continues     ");
        System.out.println("            Examples                                                     ");
        System.out.println("            e 3 8          // expand board to include element 3,8        ");
        System.out.println("            expand -4 +5   // expand board to include element -4,+5      ");
        System.out.println("  u or undo: undo last move, ends turn, previous player gets a turn      ");
        System.out.println("            Examples                                                     ");
        System.out.println("            u          // undo the last move                             ");
        System.out.println("            undo       // undo the last move                             ");
        System.out.println("  r or redo: redo last move, ends turn, next player gets a turn          ");
        System.out.println("            Examples                                                     ");
        System.out.println("            r          // redo the last move                             ");
        System.out.println("            redo       // redo the last move                             ");
        System.out.println("  l or longest: show the longest sequence on the board, turn continues   ");
        System.out.println("            Examples                                                     ");
        System.out.println("            l          // print the longest sequence                     ");
        System.out.println("            longest    // print the longest sequence                     "); 
      }

      else if(move.equals("s") || move.equals("set")){
        int row, col;
        try{
          row = in.nextInt();
          col = in.nextInt();
        }
        catch(Exception e){
          System.out.println("Bad input. Try again");
          continue;
        }
        String prevContents = board.get(row,col);
        if(!prevContents.equals(board.getFillElem())){
          System.out.printf("Element %d %d is already set. Try again\n",row,col);
          continue;
        }
        else{
          String symbol = playerSymbols.get(playerTurn);
          board.set(row,col,symbol);
          playerTurn++;
          if(playerTurn > nPlayers){
            playerTurn = 1;
            round++;
          }
        }
      }

      else if(move.equals("e") || move.equals("expand")){
        int row,col;
        try{
          row = in.nextInt();
          col = in.nextInt();
        }
        catch(Exception e){
          System.out.println("Bad input. Try again");
          continue;
        }
        int n = board.expandToInclude(row,col);
        System.out.printf("%d new cells added\n",n);
      }
      else if(move.equals("u") || move.equals("undo")){
        try{
          board.undoSet();
        }
        catch(Exception e){
          System.out.println("Problems undoing, has anyone done anything yet?");
          continue;
        }
        playerTurn--;
        if(playerTurn == 0){
          playerTurn = nPlayers;
          round--;
        }
      }

      else if(move.equals("r") || move.equals("redo")){
        try{
          board.redoSet();
        }
        catch(Exception e){
          System.out.println("Problems redoing, has anything been undone?");
          continue;
        }
        playerTurn++;
        if(playerTurn > nPlayers){
          playerTurn = 1;
          round++;
        }
      }

      else if(move.equals("l") || move.equals("longest")){
        System.out.printf("Longest sequence: %s\n",
                          board.getLongestSequence());
      }      

      else{
        System.out.printf("Unknown command '%s'\n",move);
      }
      
    }
  }
}
    
    
