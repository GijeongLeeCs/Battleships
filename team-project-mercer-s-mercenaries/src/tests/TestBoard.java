package tests;

import org.junit.jupiter.api.Test;
import model.Board;

import static org.junit.jupiter.api.Assertions.*;

public class TestBoard {

    private final char[][] testBoard = {{'U', 'U', 'S', 'U', 'U', 'U', 'S', 'U', 'U', 'U'},
                                        {'U', 'U', 'S', 'U', 'U', 'U', 'S', 'U', 'U', 'U'},
                                        {'U', 'U', 'S', 'U', 'U', 'U', 'S', 'U', 'U', 'S'},
                                        {'U', 'U', 'S', 'U', 'U', 'U', 'U', 'U', 'U', 'S'},
                                        {'U', 'U', 'S', 'U', 'U', 'U', 'U', 'U', 'U', 'S'},
                                        {'U', 'U', 'U', 'U', 'S', 'S', 'S', 'S', 'U', 'U'},
                                        {'U', 'U', 'U', 'U', 'U', 'U', 'U', 'U', 'U', 'U'},
                                        {'U', 'U', 'U', 'U', 'U', 'U', 'U', 'U', 'U', 'U'},
                                        {'U', 'S', 'S', 'U', 'U', 'U', 'U', 'U', 'U', 'U'},
                                        {'U', 'U', 'U', 'U', 'U', 'U', 'U', 'U', 'U', 'S'}};

    @Test
    public void testBoardCreation() {
        Board board = new Board(testBoard, null);
        assertEquals(testBoard.length, board.getBoard().length);
        assertEquals(testBoard[0].length, board.getBoard()[0].length);
        for(int i = 0; i < testBoard.length; i++) {
            for(int j = 0; j < testBoard[i].length; j++) {
                assertFalse(board.used(i, j));
            }
        }
        for(int i = 0; i < testBoard.length; i++) {
            for(int j = 0; j < testBoard[i].length; j++) {
                assertEquals(testBoard[i][j], board.getBoard()[i][j]);
            }
        }

        Board board2 = new Board(null);
        assertTrue(board2.placeShip(true, 5, 0, 0));
        assertTrue(board2.placeShip(true, 5, 9, 9));
        assertTrue(board2.placeShip(true, 5, 4, 5));
        assertTrue(board2.placeShip(false, 5, 3, 2));
        board2.printBoard();
    }

    @Test
    public void testShots() {
        Board board = new Board(testBoard, null);
        for(int i = 0; i < testBoard.length; i++) {
            for(int j = 0; j < testBoard[i].length; j++) {
                assertFalse(board.used(i, j));
            }
        }
        assertTrue(board.tryShot(0, 0));
        assertFalse(board.tryShot(0,0));
        assertEquals('M', board.getBoard()[0][0]);
        assertTrue(board.tryShot(0, 2));
        assertEquals('H', board.getBoard()[0][2]);
        assertTrue(board.used(0, 2));
    }

    @Test
    public void testGameOver() {
        Board board = new Board(testBoard, null);
        for(int i = 0; i < testBoard.length; i++) {
            for(int j = 0; j < testBoard[i].length; j++) {
                assertTrue(board.tryShot(i, j));
                if(board.getNumMoves() != 100) {
                    assertFalse(board.gameOver());
                }
            }
        }
    assertTrue(board.gameOver());
    }
}
