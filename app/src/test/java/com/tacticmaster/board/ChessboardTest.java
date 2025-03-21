package com.tacticmaster.board;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tacticmaster.puzzle.Puzzle;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ChessboardTest {

    private Chessboard chessboard;

    @BeforeEach
    public void setUp() {
        String fen = "1rb2rk1/q5P1/4p2p/3p3p/3P1P2/2P5/2QK3P/3R2R1 b - - 0 29";
        String moves = "f8f7 c2h7 g8h7 g7g8q";
        Puzzle puzzle = new Puzzle("1", fen, moves, 1049, 80, 85, 208, "opening", "url", "tags");
        chessboard = new Chessboard(puzzle);
    }

    @Test
    public void testSetupBoard() {
        char[][] board = chessboard.getBoard();
        assertEquals(' ', board[0][0]);
        assertEquals('r', board[0][1]);
        assertEquals('b', board[0][2]);
        assertEquals(' ', board[0][3]);
        assertEquals(' ', board[0][4]);
        assertEquals('r', board[0][5]);
        assertEquals('k', board[0][6]);
        assertEquals(' ', board[0][7]);

        assertEquals('q', board[1][0]);
        assertEquals(' ', board[1][1]);
        assertEquals(' ', board[1][2]);
        assertEquals(' ', board[1][3]);
        assertEquals(' ', board[1][4]);
        assertEquals(' ', board[1][5]);
        assertEquals('P', board[1][6]);
        assertEquals(' ', board[1][7]);

        assertEquals(' ', board[2][0]);
        assertEquals(' ', board[2][1]);
        assertEquals(' ', board[2][2]);
        assertEquals(' ', board[2][3]);
        assertEquals('p', board[2][4]);
        assertEquals(' ', board[2][5]);
        assertEquals(' ', board[2][6]);
        assertEquals('p', board[2][7]);

        assertEquals(' ', board[3][0]);
        assertEquals(' ', board[3][1]);
        assertEquals(' ', board[3][2]);
        assertEquals('p', board[3][3]);
        assertEquals(' ', board[3][4]);
        assertEquals(' ', board[3][5]);
        assertEquals(' ', board[3][6]);
        assertEquals('p', board[3][7]);

        assertEquals(' ', board[4][0]);
        assertEquals(' ', board[4][1]);
        assertEquals(' ', board[4][2]);
        assertEquals('P', board[4][3]);
        assertEquals(' ', board[4][4]);
        assertEquals('P', board[4][5]);
        assertEquals(' ', board[4][6]);
        assertEquals(' ', board[4][7]);

        assertEquals(' ', board[5][0]);
        assertEquals(' ', board[5][1]);
        assertEquals('P', board[5][2]);
        assertEquals(' ', board[5][3]);
        assertEquals(' ', board[5][4]);
        assertEquals(' ', board[5][5]);
        assertEquals(' ', board[5][6]);
        assertEquals(' ', board[5][7]);

        assertEquals(' ', board[6][0]);
        assertEquals(' ', board[6][1]);
        assertEquals('Q', board[6][2]);
        assertEquals('K', board[6][3]);
        assertEquals(' ', board[6][4]);
        assertEquals(' ', board[6][5]);
        assertEquals(' ', board[6][6]);
        assertEquals('P', board[6][7]);

        assertEquals(' ', board[7][0]);
        assertEquals(' ', board[7][1]);
        assertEquals(' ', board[7][2]);
        assertEquals('R', board[7][3]);
        assertEquals(' ', board[7][4]);
        assertEquals(' ', board[7][5]);
        assertEquals('R', board[7][6]);
        assertEquals(' ', board[7][7]);
    }

    @Test
    public void testIsWhiteToMove() {
        assertTrue(chessboard.isWhiteToMove());
    }

    @Test
    public void testIsCorrectMove() {
        assertTrue(chessboard.isCorrectMove(0, 5, 1, 5));
        assertFalse(chessboard.isCorrectMove(0, 6, 1, 5));
    }

    @Test
    public void testMakeFirstMove() {
        chessboard.makeFirstMove();
        assertTrue(chessboard.isFirstMoveDone());
        assertEquals('r', chessboard.getBoard()[1][5]);
    }

    @Test
    public void testMakeNextMove() {
        chessboard.makeFirstMove();
        chessboard.makeNextMove();
        assertEquals('Q', chessboard.getBoard()[1][7]);
    }

    @Test
    public void testMovePiece() {
        assertTrue(chessboard.movePiece(0, 1, 1, 1));
        assertEquals('r', chessboard.getBoard()[1][1]);
        assertEquals(' ', chessboard.getBoard()[0][1]);
    }

    @Test
    public void testSolved() {
        chessboard.makeFirstMove();
        chessboard.makeNextMove();
        chessboard.makeNextMove();
        chessboard.makeNextMove();
        chessboard.makeNextMove();
        assertTrue(chessboard.solved());
    }

    @Test
    public void testPromotion() {
        chessboard.makeFirstMove();
        chessboard.makeNextMove();
        chessboard.makeNextMove();
        chessboard.makeNextMove();
        chessboard.makeNextMove();
        assertTrue(chessboard.solved());
    }

    @Test
    public void testMultiplePromotionsInOneBoard() {
        String fen = "8/P1P1P1P1/8/5k2/2K5/8/1p1p1p1p/8 w - - 0 1";
        String moves = "a7a8q b2b1q c7c8r d2d1r e7e8b f2f1b g7g8n h2h1n";
        Puzzle puzzle = new Puzzle("1", fen, moves, 1049, 80, 85, 208, "promotion", "url", "tags");
        Chessboard chessboard = new Chessboard(puzzle);

        chessboard.makeFirstMove();
        assertEquals('Q', chessboard.getBoard()[7][0]);
        chessboard.makeNextMove();
        assertEquals('q', chessboard.getBoard()[0][1]);

        chessboard.makeNextMove();
        assertEquals('R', chessboard.getBoard()[7][2]);
        chessboard.makeNextMove();
        assertEquals('r', chessboard.getBoard()[0][3]);

        chessboard.makeNextMove();
        assertEquals('B', chessboard.getBoard()[7][4]);
        chessboard.makeNextMove();
        assertEquals('b', chessboard.getBoard()[0][5]);

        chessboard.makeNextMove();
        assertEquals('N', chessboard.getBoard()[7][6]);
        chessboard.makeNextMove();
        assertEquals('n', chessboard.getBoard()[0][7]);
    }
}