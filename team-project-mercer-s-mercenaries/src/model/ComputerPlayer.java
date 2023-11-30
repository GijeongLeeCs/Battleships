package model;

/**
 * This class allows a Tic Tac Toe (TTT) player to play games against a variety
 * of AIs. It completely relies on the TicTacToeStrategy for it's next move
 * with the desiredMove(TicTacToeGame theGame) method that can "see" the game.
 * 
 * @author Kartikey Bihani
 */

public class ComputerPlayer {

  private BoardStrategy myStrategy;

  public ComputerPlayer() {
    // This default can be changed with setStrategy
    myStrategy = new RandomAI();
  }

  /**
   * Change the AI for this ComputerPlayer.
   * 
   * @param strategy  Any type that implements TicTacToeStrategy
   */
  public void setStrategy(BoardStrategy strategy) {
    myStrategy = strategy;
  }
 
  /**
   * 
   * @param board The current state of the game when asked for a move
   * 
   * @return A point that store two ints: a row and a column
   */
  public Point desiredMove(Board board) {
    return myStrategy.desiredMove(board);
  }
}
