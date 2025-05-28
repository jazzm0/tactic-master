package com.tacticmaster.board;


import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;
import com.tacticmaster.puzzle.Puzzle;

import java.util.Set;

public class Chessboard {

    private final Set<Character> validPieces = Set.of('q', 'r', 'b', 'n', 'k', 'p');
    private final Board board;
    private final boolean isPlayerWhite;
    private final String[] moves;
    private int movesIndex = 0;
    private boolean firstMoveDone = false;

    public Chessboard(Puzzle puzzle) {
        requireNonNull(puzzle, "Puzzle cannot be null");
        this.board = new Board();
        board.loadFromFen(puzzle.fen());
        this.moves = puzzle.moves().trim().isEmpty() ? new String[0] : requireNonNull(puzzle.moves().split(" "), "Moves cannot be null");
        this.isPlayerWhite = board.getSideToMove() == Side.BLACK;
    }

    public Chessboard(Puzzle puzzle, boolean isPlayerWhite) {
        requireNonNull(puzzle, "Puzzle cannot be null");
        this.board = new Board();
        board.loadFromFen(puzzle.fen());
        this.moves = puzzle.moves().trim().isEmpty() ? new String[0] : requireNonNull(puzzle.moves().split(" "), "Moves cannot be null");
        this.isPlayerWhite = isPlayerWhite;
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

        return new int[]{fromRow, fromCol, toRow, toCol};
    }

    public boolean isValidPiece(char c) {
        return validPieces.contains(Character.toLowerCase(c));
    }

    private boolean isInvalidField(int row, int col) {
        return row < 0 || row >= 8 || col < 0 || col >= 8;
    }

    private Square mapToSquare(int row, int column) {
        if (isInvalidField(row, column)) {
            throw new IllegalArgumentException("Invalid row or column");
        }
        var file = (char) ('A' + column);
        var rank = (char) ('8' - row);
        return Square.fromValue(new String(new char[]{file, rank}));
    }

    public Character getPieceAt(int row, int column) {
        var symbol = board.getPiece(mapToSquare(row, column)).getFenSymbol();
        return isNull(symbol) ? '.' : symbol.charAt(0);
    }

    public boolean isPlayerWhite() {
        return isPlayerWhite;
    }

    public boolean isOwnPiece(Character piece) {
        return Character.isUpperCase(piece) == isPlayerWhite;
    }


    public boolean isCorrectMove(int fromRow, int fromCol, int toRow, int toCol) {
        if (movesIndex >= moves.length) {
            return false;
        }
        int[] move = convertMoveToCoordinates(moves[movesIndex]);
        var correctMoveFromPuzzle = move[0] == fromRow && move[1] == fromCol && move[2] == toRow && move[3] == toCol;
        if (correctMoveFromPuzzle) {
            return true;
        }

        var fromSquare = mapToSquare(fromRow, fromCol);
        var toSquare = mapToSquare(toRow, toCol);

        var possibleMatingMove = fromSquare.value().toLowerCase() + toSquare.value().toLowerCase();
        board.doMove(new Move(mapToSquare(fromRow, fromCol), mapToSquare(toRow, toCol)), true);
        var isMate = board.isMated();

        board.undoMove();

        if (isMate) {
            moves[movesIndex] = possibleMatingMove;
        }

        return isMate;
    }

    public boolean isPromotionMove() {
        if (movesIndex < 0 || movesIndex >= moves.length) {
            return false;
        }
        return moves[movesIndex].length() == 5 && moves[movesIndex].charAt(4) != ' ';
    }

    public boolean isCorrectPromotionPiece(char piece) {
        if (!isPromotionMove())
            return false;
        return Character.toLowerCase(moves[movesIndex].charAt(4)) == Character.toLowerCase(piece);
    }

    public void makeFirstMove() {
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
        movePiece();
    }

    public void movePiece() {
        board.doMove(moves[movesIndex]);
        movesIndex++;
    }

    public boolean solved() {
        return movesIndex == moves.length;
    }
}