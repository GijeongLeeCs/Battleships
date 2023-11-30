package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SmartAI implements BoardStrategy {

    private static Random generator;
    private List<Point> hitCells;
    private boolean isDiagonalSpree;
    private boolean isCenterSpree;
    private boolean isHorizontalSpree;

    public SmartAI() {
        generator = new Random();
        hitCells = new ArrayList<>();
        isDiagonalSpree = true;
        isCenterSpree = false;
        isHorizontalSpree = false;
    }

    // Find an open spot while considering adjacent cells and forming a line of hit cells
    public Point desiredMove(Board board) {
        // Check if there are hit cells to follow
        if (!hitCells.isEmpty()) {
            Point nextMove = getNextHitMove(board);
            if (nextMove != null) {
                return nextMove;
            }
        }

        // If no hit cells to follow, choose a random move
        if (isDiagonalSpree) {
            return getDiagonalMove(board);
        }
        if (isCenterSpree) {
            return getCenterSpreeMove(board);
        }
        if (isHorizontalSpree) {
            return getHorizontalSpreeMove(board);
        }
        else {
            return getRandomMove(board);
        }
    }

    // Helper method to get the next move when following a line of hit cells
    private Point getNextHitMove(Board board) {
        Point lastHit = hitCells.get(hitCells.size() - 1);
        
        // Try to form a line of hit cells
        Point nextMove = formLine(board, lastHit.getRow(), lastHit.getCol());
        if (nextMove != null) {
            return nextMove;
        }

        // If no line is formed, remove the last hit cell
        hitCells.remove(hitCells.size() - 1);

        // If the diagonal spree is still active, continue diagonally
        if (isDiagonalSpree) {
            return getDiagonalMove(board);
        }
        
        if (isCenterSpree) {
            return getCenterSpreeMove(board);
        }

        // Resort to random hitting
        return getRandomMove(board);
    }

    // Helper method to get a move for a spree from the top line center to the bottom line center
    private Point getCenterSpreeMove(Board board) {
        int centerCol = Board.SIZE / 2;

        // Check for a line of hit cells from top line center to bottom line center in the 5th column
        for (int i = 0; i < Board.SIZE; i++) {
            int row = i;
            int col = centerCol;
            if (!board.used(row, col)) {
                Point centerSpreeMove = new Point(row, col);
                hitCells.add(centerSpreeMove);
                return centerSpreeMove;
            }
        }

        // If no valid move found in the systematic patterns, resort to random move
        isCenterSpree = false;
        isHorizontalSpree = true;
        return getRandomMove(board);
    }

    // Helper method to get a move for a spree from the 1st column to the 10th column in the 5th row
    private Point getHorizontalSpreeMove(Board board) {
        int targetRow = Board.SIZE / 2;

        // Check for a line of hit cells from the 1st column to the 10th column in the 5th row
        for (int i = 0; i < Board.SIZE; i++) {
            int row = targetRow;
            int col = i;
            if (!board.used(row, col)) {
                Point horizontalSpreeMove = new Point(row, col);
                hitCells.add(horizontalSpreeMove);
                return horizontalSpreeMove;
            }
        }

        // If no valid move found in the systematic patterns, resort to random move
        isHorizontalSpree = false;
        return getRandomMove(board);
    }

    // Helper method to get a random move
    private Point getRandomMove(Board board) {
        boolean set = false;
        while (!set) {
            int row = generator.nextInt(Board.SIZE);
            int col = generator.nextInt(Board.SIZE);

            if (!board.used(row, col)) {
                set = true;
                Point randPoint = new Point(row, col);
                if (board.hit(row, col)) {
                	hitCells.add(randPoint);
                }
                return randPoint;
            }
        }
        return null;
    }

    // Helper method to form a line of hit cells
    private Point formLine(Board board, int row, int col) {
        // Check horizontally for a line of hit cells
        Point horizontalMove = checkLine(board, row, col, 0, 1);
        if (horizontalMove != null) {
            return horizontalMove;
        }

        // Check vertically for a line of hit cells
        Point verticalMove = checkLine(board, row, col, 1, 0);
        if (verticalMove != null) {
            return verticalMove;
        }

        // Check left horizontally for a line of hit cells
        Point leftMove = checkLine(board, row, col, 0, -1);
        if (leftMove != null) {
            return leftMove;
        }

        // Check above vertically for a line of hit cells
        Point aboveMove = checkLine(board, row, col, -1, 0);
        return aboveMove;
    }


    // Helper method to check for a line of hit cells in a given direction
    private Point checkLine(Board board, int row, int col, int rowIncrement, int colIncrement) {
        int currentRow = row;
        int currentCol = col;

        // Move in the specified direction until an open spot is found
        while (currentRow >= 0 && currentRow < Board.SIZE && currentCol >= 0 && currentCol < Board.SIZE
                && board.hit(currentRow, currentCol)) {
            currentRow += rowIncrement;
            currentCol += colIncrement;
        }

        // Check if a line of hit cells is formed
        if (currentRow >= 0 && currentRow < Board.SIZE && currentCol >= 0 && currentCol < Board.SIZE
                && !board.used(currentRow, currentCol)) {
            return new Point(currentRow, currentCol);
        }

        return null;
    }

    // Helper method to get a diagonal move
    private Point getDiagonalMove(Board board) {
        // Check for diagonal cells from top left to bottom right and hit them
        for (int i = 0; i < Board.SIZE; i++) {
            if (!board.used(i, i)) {
                Point diagonalMove = new Point(i, i);
                hitCells.add(diagonalMove);
                return diagonalMove;
            }
        }

        // Check for diagonal cells from top right to bottom left and hit them
        for (int i = 0; i < Board.SIZE; i++) {
            int row = i;
            int col = Board.SIZE - 1 - i;
            if (!board.used(row, col)) {
                Point diagonalMove = new Point(row, col);
                hitCells.add(diagonalMove);
                return diagonalMove;
            }
        }

        // If all diagonal cells are hit, shift to random hit
        isDiagonalSpree = false;
        isCenterSpree = true;
        return getRandomMove(board);
    }

}
