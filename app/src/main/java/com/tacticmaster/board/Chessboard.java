package com.tacticmaster.board;

import static java.lang.Math.abs;
import static java.util.Objects.isNull;

import com.tacticmaster.puzzle.Puzzle;

import java.util.ArrayList;
import java.util.List;

public class Chessboard {

    private final char[][] board;
    private boolean whiteToMove;
    private final String[] moves;
    private int movesIndex = 0;
    private int[] lastMoveCoordinates;
    private boolean firstMoveDone = false;
    private char promotion = ' ';

    public Chessboard(Puzzle puzzle) {
        this.board = new char[8][8];
        setupBoard(puzzle.fen());
        this.moves = puzzle.moves().split(" ");
    }

    private int[] convertMoveToCoordinates(String move) {
        if (move == null || move.length() < 4 || move.length() > 5) {
            throw new IllegalArgumentException("Invalid move: " + move);
        }
        char fromColChar = move.charAt(0);
        char fromRowChar = move.charAt(1);
        char toColChar = move.charAt(2);
        char toRowChar = move.charAt(3);
        if (fromColChar < 'a' || fromColChar > 'h' || toColChar < 'a' || toColChar > 'h' ||
                fromRowChar < '1' || fromRowChar > '8' || toRowChar < '1' || toRowChar > '8') {
            throw new IllegalArgumentException("Invalid move coordinates: " + move);
        }
        int fromCol = fromColChar - 'a';
        int fromRow = 8 - (fromRowChar - '0');
        int toCol = toColChar - 'a';
        int toRow = 8 - (toRowChar - '0');
        if (move.length() == 5) {
            promotion = move.charAt(4);
            if ("qrbnQRBN".indexOf(promotion) == -1) {
                throw new IllegalArgumentException("Invalid promotion piece: " + promotion);
            }
        }
        return new int[]{fromRow, fromCol, toRow, toCol};
    }

    private void setupBoard(String fen) {
        if (isNull(fen) || fen.isEmpty()) {
            throw new IllegalArgumentException("FEN string cannot be null or empty");
        }
        String[] parts = fen.split(" ");
        if (parts.length < 2) {
            throw new IllegalArgumentException("Invalid FEN: Missing turn indicator");
        }
        String[] rows = parts[0].split("/");
        if (rows.length != 8) {
            throw new IllegalArgumentException("Invalid FEN: Must have 8 rows");
        }
        for (int i = 0; i < 8; i++) {
            int col = 0;
            for (char c : rows[i].toCharArray()) {
                if (Character.isDigit(c)) {
                    int emptySquares = Character.getNumericValue(c);
                    if (col + emptySquares > 8) {
                        throw new IllegalArgumentException("Invalid FEN: Row " + i + " exceeds 8 columns");
                    }
                    for (int j = 0; j < emptySquares; j++) {
                        board[i][col++] = ' ';
                    }
                } else if (isValidPiece(c)) {
                    board[i][col++] = c;
                } else {
                    throw new IllegalArgumentException("Invalid FEN: Invalid piece character " + c);
                }
            }
            if (col != 8) {
                throw new IllegalArgumentException("Invalid FEN: Row " + i + " does not have 8 columns");
            }
        }
        whiteToMove = parts[1].equals("b");
    }

    public boolean isValidPiece(char c) {
        return "rnbqkpRNBQKP".indexOf(c) != -1;
    }

    public Character getPieceAt(int row, int col) {
        if (row < 0 || row >= 8 || col < 0 || col >= 8) {
            throw new IllegalArgumentException("Invalid board coordinates");
        }
        return board[row][col];
    }
    public boolean isWhiteToMove() {
        return whiteToMove;
    }

    public boolean isOwnPiece(Character piece) {
        return Character.isUpperCase(piece) == whiteToMove;
    }

    public boolean isCorrectMove(int fromRow, int fromCol, int toRow, int toCol) {
        if (movesIndex >= moves.length) {
            return false;
        }
        int[] move = convertMoveToCoordinates(moves[movesIndex]);
        return move[0] == fromRow && move[1] == fromCol && move[2] == toRow && move[3] == toCol;
    }

    public synchronized void makeFirstMove() {
        if (firstMoveDone) {
            return;
        }
        firstMoveDone = true;
        makeNextMove();
    }

    public boolean isPlayersTurn() {
        return movesIndex % 2 == 1;
    }

    public boolean isFirstMoveDone() {
        return firstMoveDone;
    }

    public int[] getNextMoveCoordinates() {
        return movesIndex >= 0 && movesIndex < moves.length ? convertMoveToCoordinates(moves[movesIndex]) : null;
    }

    public void makeNextMove() {
        if (!firstMoveDone) {
            return;
        }
        if (movesIndex >= moves.length) {
            return;
        }
        int[] move = convertMoveToCoordinates(moves[movesIndex]);
        movePiece(move[0], move[1], move[2], move[3]);
    }

    // not used (yet), just parked old code here
    public boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol) {
        // no move that ends on a field with own piece
        if (Character.isUpperCase(board[fromRow][fromCol]) && Character.isUpperCase(board[toRow][toCol]) ||
                Character.isLowerCase(board[fromRow][fromCol]) && Character.isLowerCase(board[toRow][toCol]) || board[fromRow][fromCol] == ' ') {
            return false;
        }
        // en passant is ok
        if (lastMoveCoordinates != null && lastMoveCoordinates[2] == fromRow && lastMoveCoordinates[3] == toCol &&
                ((board[fromRow][fromCol] == 'p' && fromRow == 3 && lastMoveCoordinates[0]==1) || (board[fromRow][fromCol] == 'P' && fromRow==4 && lastMoveCoordinates[0]==6)) &&
                board[toRow][toCol] == ' ' && abs(fromCol - toCol) == 1 && board[fromRow][toCol] == (board[fromRow][fromCol] == 'p' ? 'P' : 'p')) {
                    return true;
        }
        return true;
    }

    public void movePiece(int fromRow, int fromCol, int toRow, int toCol) {
        // remove captured pawn at en passant
        if ((board[fromRow][fromCol] == 'p' || board[fromRow][fromCol] == 'P') &&
                board[toRow][toCol] == ' ' && abs(fromCol - toCol) == 1 && board[fromRow][toCol] == (board[fromRow][fromCol] == 'p' ? 'P' : 'p')) {
                    board[fromRow][toCol] = ' ';
        }
        // move rook at castling
        else if ((board[fromRow][fromCol] == 'K' || board[fromRow][fromCol] == 'k') &&
                abs(fromCol - toCol) == 2 && fromRow == toRow) {
            char rook = (board[fromRow][fromCol] == 'K') ? 'R' : 'r';
            if (toCol == 2 && board[toRow][0] == rook) {
                board[toRow][3] = rook;
                board[toRow][0] = ' ';
            } else if (toCol == 6 && board[toRow][7] == rook) {
                board[toRow][5] = rook;
                board[toRow][7] = ' ';
            }
        }

        board[toRow][toCol] = board[fromRow][fromCol];
        board[fromRow][fromCol] = ' ';

        // promote pawn
        if (board[toRow][toCol] == 'P' && toRow == 0) {
            board[toRow][toCol] = promotion == ' ' ? 'Q' : Character.toUpperCase(promotion);
        } else if (board[toRow][toCol] == 'p' && toRow == 7) {
            board[toRow][toCol] = promotion == ' ' ? 'q' : Character.toLowerCase(promotion);
        }

        lastMoveCoordinates = new int[]{fromRow, fromCol, toRow, toCol};
        movesIndex++;
    }

    public boolean solved() {
        return movesIndex == moves.length;
    }
}