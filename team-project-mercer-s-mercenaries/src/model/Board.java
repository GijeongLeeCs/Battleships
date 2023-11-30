/**
 * Class to represent the game board. Stores the array as a 2D array of chars.
 * @authors: Ryan Bullard
 */

package model;

import view_controller.BoardGUI;

import java.util.ArrayList;
import java.util.Arrays;

public class Board {

    public static final int SIZE = 10;
	private final char[][] board;
    private final BoardGUI gui;
    private int numMoves;
    private int numHits;
    private int numMisses;
    private int currHitsInRow;
    private int maxHitsInARow;
    private final ArrayList<Point> shipLocations;

    /**
     * Creates a new Board with hardcoded ships.
     * @param shipLocations The list of coordinates the ships have been placed at.
     * @param listener The BoardGUI this board is connected to.
     */
    public Board(char[][] shipLocations, BoardGUI listener) {
        board = new char[10][10]; // Accurate to the real board.
        this.shipLocations = new ArrayList<>();
        for(int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (shipLocations[i][j] == 'S') {
                    board[i][j] = 'S'; // Represents a ship, should not be shown on the enemy board.
                    this.shipLocations.add(new Point(i, j));
                } else {
                    board[i][j] = 'U'; // U means un-hit.
                }
            }
        }
        gui = listener;
        numMoves = 0;
        numHits = 0;
        numMisses = 0;
        currHitsInRow = 0;
        maxHitsInARow = 0;
    }

    /**
     * Creates a new board without hardcoded ship placements.
     * @param boardGUI The boardGui this board is associated with.
     */
    public Board(BoardGUI boardGUI) {
        board = new char[10][10]; // Accurate to the real board.
        for (char[] chars : board) {
            Arrays.fill(chars, 'U');
        }
        gui = boardGUI;
        numMoves = 0;
        numHits = 0;
        numMisses = 0;
        currHitsInRow = 0;
        maxHitsInARow = 0;
        this.shipLocations = new ArrayList<>();
    }

    /**
     * Places a ship on the board. This method takes care of possible out-of-bounds placement by moving ships back to the
     * smallest valid position.
     * @param horizontal If the ship is horizontal or not
     * @param length The length of the ship
     * @param row The row to add the ship to
     * @param col The column to add the ship to.
     * @return True if the placement is a success, false otherwise
     */
    public boolean placeShip(boolean horizontal, int length, int row, int col) {
        if(horizontal && col > (10 - length)) {
            col = 10 - length;
        } else if (!horizontal && row > (10 - length)){
            row = 10 - length;
        }
        if(horizontal) {
            for (int i = 0; i < length; i++) { // Check the placement isn't used already
                if (board[row][col + i] != 'U') {
                    return false;
                }
            }
            for (int i = 0; i < length; i++) { // Place the ship
                board[row][col + i] = 'S';
                shipLocations.add(new Point(row, col + i));
            }
        } else {
            for (int i = 0; i < length; i++) { // Check the placement isn't used already
                if (board[row + i][col] != 'U') {
                    return false;
                }
            }
            for (int i = 0; i < length; i++) { // Place the ship
                board[row + i][col] = 'S';
                shipLocations.add(new Point(row + i, col));
            }
        }
        if(gui != null) {
            update(new Point(row, col));
        }
        return true;
    }

    /**
     * Returns the internal representation of the board.
     * @return The board.
     */
    public char[][] getBoard() {
        return board;
    }

    /**
     * Checks if a space on the board has been used, i.e., hit or missed.
     * @param row The row in the board to check.
     * @param col The col in the board to check.
     * @return True if the space has been hit, false otherwise.
     */
    public boolean used(int row, int col) {
        return board[row][col] == 'H' || board[row][col] == 'M';
    }

    /**
     * Returns the number of moves made on this board.
     * @return The number of moves.
     */
    public int getNumMoves() {
        return numMoves;
    }

    /**
     * Returns the number of shots that hit a ship.
     * @return The number of hits.
     */
    public int getNumHits() {
        return numHits;
    }

    /**
     * Returns the number of shots that did not hit a ship
     * @return The number of misses.
     */
    public int getNumMisses() {
        return numMisses;
    }

    /**
     * Returns the maximum combo for this board.
     * @return The maximum number of hits in  a row.
     */
    public int getMaxHitsInARow() {
        return maxHitsInARow;
    }

    /**
     * Fires a shot at the location on the board, and updates the board based on the success or failure.
     * @param row The row to hit.
     * @param col The column to hit.
     * @return True if the shot is valid, false otherwise.
     */
    public boolean tryShot(int row, int col) {
        if(used(row, col)) {
            return false;
        }
        if(board[row][col] == 'S') {
            board[row][col] = 'H'; // Represents hit
            currHitsInRow++;
            if(currHitsInRow > maxHitsInARow) {
                maxHitsInARow = currHitsInRow;
            }
            numHits++;
        } else {
            board[row][col] = 'M'; // Represents miss
            currHitsInRow = 0;
            numMisses++;
        }
        if(gui != null) {
            update(new Point(row, col));
        }
        numMoves++;
        return true;
    }

    /**
     * Checks if the game is over.
     * @return True, if all ships have been hit on this board, false otherwise.
     */
    public boolean gameOver() {
        if(shipLocations.size() != 17) { // Not all ships have been placed
            return false;
        }
        for(Point point : shipLocations) {
            if(board[point.row][point.col] == 'S') {
                return false;
            }
        }
        return true;
    }

    /**
     * Lets an observer know that the board has been updated, either with a shot or with a placement.
     * @param pos The position of any new shots. This will be null in any case where shots were not made.
     */
    private void update(Point pos) {
        gui.updateBoard(this.getBoard(), this, pos);
    }

    /**
     * Test method to print out the current state of the board.
     */
    public void printBoard() {
        for (char[] row : board) {
            for (char curr : row) {
                System.out.print(curr + " ");
            }
            System.out.println();
        }
    }
    
    /**
     * Checks if the row and col on the board are part of a ship
     * @param row The row to check
     * @param col The column to check
     */
    public boolean hit(int row, int col) {
        return board[row][col] == 'H';
    }
    
    public Point getPoint(int row, int col) {
    	return new Point(row, col);
    }
}
