package com.tacticmaster.board;

import com.tacticmaster.puzzle.Puzzle;

import java.util.ArrayList;
import java.util.List;

public class Chessboard {

    private final char[][] board;
    private boolean whiteToMove;
    private final List<int[]> moves;
    private int movesIndex = 0;
    private boolean firstMoveDone = false;
    private final List<Character> promotions = new ArrayList<>();

    public Chessboard(Puzzle puzzle) {
        this.board = new char[8][8];
        setupBoard(puzzle.fen());
        var movesList = List.of(puzzle.moves().split(" "));
        for (var move : movesList) {
            if (move.length() == 5) {
                promotions.add(move.charAt(4));
            }
        }
        this.moves = convertMovesToCoordinates(movesList);
    }

    private List<int[]> convertMovesToCoordinates(List<String> moves) {
        List<int[]> coordinates = new ArrayList<>();
        for (String move : moves) {
            int fromCol = move.charAt(0) - 'a';
            int fromRow = 8 - (move.charAt(1) - '0');
            int toCol = move.charAt(2) - 'a';
            int toRow = 8 - (move.charAt(3) - '0');

            if (!whiteToMove) {
                fromRow = 7 - fromRow;
                toRow = 7 - toRow;
            }

            coordinates.add(new int[]{fromRow, fromCol, toRow, toCol});
        }
        return coordinates;
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

        whiteToMove = parts[1].equals("b");//first move is made by opponent
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

    public boolean isCorrectMove(int fromRow, int fromCol, int toRow, int toCol) {
        if (movesIndex >= moves.size()) {
            return false;
        }
        int[] move = moves.get(movesIndex);
        return move[0] == fromRow && move[1] == fromCol && move[2] == toRow && move[3] == toCol;
    }

    public synchronized void makeFirstMove() {
        if (firstMoveDone) {
            return;
        }
        firstMoveDone = true;
        makeNextMove();
    }

    public boolean isFirstMoveDone() {
        return firstMoveDone;
    }

    public void makeNextMove() {
        if (!firstMoveDone) {
            return;
        }
        if (movesIndex >= moves.size()) {
            return;
        }
        int[] move = moves.get(movesIndex);
        movePiece(move[0], move[1], move[2], move[3]);
    }

    public boolean movePiece(int fromRow, int fromCol, int toRow, int toCol) {
        if (Character.isUpperCase(board[fromRow][fromCol]) && Character.isUpperCase(board[toRow][toCol]) ||
                Character.isLowerCase(board[fromRow][fromCol]) && Character.isLowerCase(board[toRow][toCol]) || board[fromRow][fromCol] == ' ') {
            return false;
        }
        board[toRow][toCol] = board[fromRow][fromCol];
        board[fromRow][fromCol] = ' ';

        if (board[toRow][toCol] == 'P' && toRow == 0 || board[toRow][toCol] == 'P' && toRow == 7) {
            board[toRow][toCol] = Character.toUpperCase(promotions.get(0));
            promotions.remove(0);
        } else if (board[toRow][toCol] == 'p' && toRow == 0 || board[toRow][toCol] == 'p' && toRow == 7) {
            board[toRow][toCol] = Character.toLowerCase(promotions.get(0));
            promotions.remove(0);
        }

        movesIndex++;
        return true;
    }

    public boolean solved() {
        return movesIndex == moves.size();
    }
}