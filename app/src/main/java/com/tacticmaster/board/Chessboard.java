package com.tacticmaster.board;

import static java.lang.Math.abs;
import static java.util.Objects.isNull;

import com.tacticmaster.puzzle.Puzzle;

import java.util.ArrayList;
import java.util.List;

public class Chessboard {

    private final char[][] board;
    private boolean whiteToMove;
    private final List<int[]> moves;
    private int movesIndex = 0;
    private int[] lastMove;
    private boolean firstMoveDone = false;
    private final List<Character> promotions = new ArrayList<>();

    public Chessboard(Puzzle puzzle) {
        this.board = new char[8][8];
        setupBoard(puzzle.fen());
        var movesList = List.of(puzzle.moves().split(" "));
        for (var move : movesList) {
            if (move.length() == 5) {
                char promotion = move.charAt(4);
                if ("qrbnQRBN".indexOf(promotion) == -1) {
                    throw new IllegalArgumentException("Invalid promotion piece: " + promotion);
                }
                promotions.add(promotion);
            }
        }
        this.moves = convertMovesToCoordinates(movesList);
    }

    private List<int[]> convertMovesToCoordinates(List<String> moves) {
        List<int[]> coordinates = new ArrayList<>();
        for (String move : moves) {
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
            if (!whiteToMove) {
                fromRow = 7 - fromRow;
                toRow = 7 - toRow;
                fromCol = 7 - fromCol;
                toCol = 7 - toCol;
            }
            coordinates.add(new int[]{fromRow, fromCol, toRow, toCol});
        }
        return coordinates;
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
        if (!whiteToMove) {
            flipBoard();
        }
    }

    private boolean isValidPiece(char c) {
        return "rnbqkpRNBQKP".indexOf(c) != -1;
    }

    private void flipBoard() {
        for (int i = 0; i < 4; i++) {
            char[] src = flipRow(board[i]);
            board[i] = flipRow(board[7 - i]);
            board[7 - i] = src;
        }
    }

    private char[] flipRow(char[] row) {
        for (int i = 0; i < 4; i++) {
            char src = row[i];
            row[i] = row[7 - i];
            row[7 - i] = src;
        }
        return row;
    }

    private boolean isNotValid(int row, int col) {
        return row < 0 || row >= 8 || col < 0 || col >= 8;
    }

    public Character getPieceAt(int row, int col) {
        if (isNotValid(row, col)) {
            throw new IllegalArgumentException("Invalid board coordinates");
        }
        return board[row][col];
    }

    public char[][] getBoard() {
        return board;
    }

    public boolean isWhiteToMove() {
        return whiteToMove;
    }

    public boolean isOwnPiece(Character piece) {
        return Character.isUpperCase(piece) == whiteToMove;
    }

    public boolean isCorrectMove(int fromRow, int fromCol, int toRow, int toCol) {
        if (movesIndex >= moves.size()) {
            return false;
        }
        int[] move = moves.get(movesIndex);
        return move[0] == fromRow && move[1] == fromCol && move[2] == toRow && move[3] == toCol;
    }

    public boolean isPromotionMove(int fromRow, int fromCol, int toRow, int toCol) {
        if (movesIndex >= moves.size() || isNotValid(fromRow, fromCol) || isNotValid(toRow, toCol)) {
            return false;
        }

        return (board[fromRow][fromCol] == 'P' && toRow == 0 || board[fromRow][fromCol] == 'P' && toRow == 7) ||
                (board[fromRow][fromCol] == 'p' && toRow == 0 || board[fromRow][fromCol] == 'p' && toRow == 7);

    }

    public boolean isCorrectPromotionPiece(char piece) {
        if (promotions.isEmpty())
            return false;
        return promotions.get(0) == Character.toLowerCase(piece);
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

    public int[] getNextMove() {
        return movesIndex >= 0 && movesIndex < moves.size() ? moves.get(movesIndex) : null;
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
        if ((board[fromRow][fromCol] == 'p' || board[fromRow][fromCol] == 'P') &&
                board[toRow][toCol] == ' ' && fromCol != toCol) {
            if (board[fromRow][toCol] == (board[fromRow][fromCol] == 'p' ? 'P' : 'p')) {
                if (lastMove != null && lastMove[2] == fromRow && lastMove[3] == toCol &&
                        abs(lastMove[0] - lastMove[2]) == 2) {
                    board[fromRow][toCol] = ' ';
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }

        if ((board[fromRow][fromCol] == 'K' || board[fromRow][fromCol] == 'k') &&
                abs(fromCol - toCol) == 2 && fromRow == toRow) {
            char rook = (board[fromRow][fromCol] == 'K') ? 'R' : 'r';
            if (toCol == 1 && board[toRow][0] == rook) {
                board[toRow][2] = rook;
                board[toRow][0] = ' ';
            } else if (toCol == 2 && board[toRow][0] == rook) {
                board[toRow][3] = rook;
                board[toRow][0] = ' ';
            } else if (toCol == 5 && board[toRow][7] == rook) {
                board[toRow][4] = rook;
                board[toRow][7] = ' ';
            } else if (toCol == 6 && board[toRow][7] == rook) {
                board[toRow][5] = rook;
                board[toRow][7] = ' ';
            }
        }

        board[toRow][toCol] = board[fromRow][fromCol];
        board[fromRow][fromCol] = ' ';

        if (board[toRow][toCol] == 'P' && toRow == 0 || board[toRow][toCol] == 'P' && toRow == 7) {
            board[toRow][toCol] = promotions.isEmpty() ? 'Q' : Character.toUpperCase(promotions.remove(0));
        } else if (board[toRow][toCol] == 'p' && toRow == 0 || board[toRow][toCol] == 'p' && toRow == 7) {
            board[toRow][toCol] = promotions.isEmpty() ? 'q' : Character.toLowerCase(promotions.remove(0));
        }

        lastMove = new int[]{fromRow, fromCol, toRow, toCol};
        movesIndex++;
        return true;
    }

    public boolean solved() {
        return movesIndex == moves.size();
    }
}