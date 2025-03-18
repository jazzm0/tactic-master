package com.tacticmaster.board;

import com.tacticmaster.puzzle.Puzzle;

public class Chessboard {

    private final char[][] board;
    private boolean whiteToMove;

    public Chessboard(Puzzle puzzle) {
        this.board = new char[8][8];
        setupBoard(puzzle.fen());
    }

    private void setupBoard(String fen) {
        String[] parts = fen.split(" ");
        String[] rows = parts[0].split("/");
        for (int i = 0; i < 8; i++) {
            int col = 0;
            for (char c : rows[i].toCharArray()) {
                if (Character.isDigit(c)) {
                    int emptySquares = Character.getNumericValue(c);
                    for (int j = 0; j < emptySquares; j++) {
                        board[i][col++] = ' ';
                    }
                } else {
                    board[i][col++] = c;
                }
            }
        }

        whiteToMove = parts[1].equals("w");
        if (!whiteToMove) {
            for (int i = 0; i < 4; i++) {
                var src = board[i];
                board[i] = board[7 - i];
                board[7 - i] = src;
            }
        }
    }

    public char[][] getBoard() {
        return board;
    }

    public boolean isWhiteToMove() {
        return whiteToMove;
    }
}
