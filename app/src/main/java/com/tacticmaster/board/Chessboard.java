package com.tacticmaster.board;

import static com.tacticmaster.board.ChessboardView.BOARD_SIZE;
import static java.util.Objects.isNull;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.Rank;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;

import java.util.Arrays;

public class Chessboard {

    public final static char NONE_PIECE = Piece.NONE.getFenSymbol().charAt(0);

    private final Board chessboard;
    private final boolean isPlayerWhite;

    public Chessboard(String fen) {
        if (fen == null || fen.trim().isEmpty()) {
            throw new IllegalArgumentException("FEN string cannot be null or empty");
        }
        this.chessboard = new Board();
        this.chessboard.loadFromFen(fen);
        this.isPlayerWhite = chessboard.getSideToMove() == Side.BLACK;
    }

    private Square squareAt(int rank, int file) {
        return Square.squareAt(transformFlippedCoordinate(BOARD_SIZE - rank - 1) * BOARD_SIZE + transformFlippedCoordinate(file));
    }

    private int transformFlippedCoordinate(int i) {
        return !isPlayerWhite ? 7 - i : i;
    }

    Piece getPiece(Square square) {
        return chessboard.getPiece(square);
    }

    Side getSideToMove() {
        return chessboard.getSideToMove();
    }

    public int[] transformFenMove(String fenMove) {
        if (fenMove == null || fenMove.length() < 4 || fenMove.length() > 5) {
            throw new IllegalArgumentException("Invalid move: " + fenMove);
        }
        Move move = new Move(fenMove, chessboard.getSideToMove());
        int[] moveCoordinates = new int[]{BOARD_SIZE - move.getFrom().getRank().ordinal() - 1, move.getFrom().getFile().ordinal(), BOARD_SIZE - move.getTo().getRank().ordinal() - 1, move.getTo().getFile().ordinal()};
        return !isPlayerWhite ? Arrays.stream(moveCoordinates).map(this::transformFlippedCoordinate).toArray() : moveCoordinates;
    }

    public boolean isPlayersTurn() {
        return chessboard.getSideToMove() == (isPlayerWhite ? Side.WHITE : Side.BLACK);
    }

    public boolean isMoveLeadingToMate(String fenMove) {
        // chessboard#isMoveLegal does not check whether the move is legal or not according to the
        // standard chess rules, but rather if the resulting configuration is valid, so we check
        // by searching in legalMoves()
        boolean isMated = false;
        var move = new Move(fenMove, chessboard.getSideToMove());
        boolean isMoveLegal = isMoveLegal(fenMove);
        if (isMoveLegal) {
            chessboard.doMove(move, false);
            isMated = chessboard.isMated();
            chessboard.undoMove();
        }
        return isMated;
    }

    public boolean isMoveLegal(String fenMove) {
        if (isNull(fenMove) || fenMove.length() < 4 || fenMove.length() > 5) {
            return false;
        }
        for (var legalMove : chessboard.legalMoves()) {
            if (legalMove.toString().toLowerCase().startsWith(fenMove.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    public String getProposedMove(int fromRank, int fromFile, int toRank, int toFile) {
        return new Move(squareAt(fromRank, fromFile), squareAt(toRank, toFile)).toString().toLowerCase();
    }

    public String getPromotionMove(int fromRank, int fromFile, int toRank, int toFile, char piece) {
        return new Move(squareAt(fromRank, fromFile), squareAt(toRank, toFile), Piece.fromFenSymbol(Character.toString(piece)))
                .toString()
                .toLowerCase();
    }

    public boolean isPlayerWhite() {
        return isPlayerWhite;
    }

    public boolean isPromotionMove(int fromRank, int fromFile, int toRank, int toFile) {
        Move move = new Move(squareAt(fromRank, fromFile), squareAt(toRank, toFile));
        if (!isMoveLegal(move.toString())) {
            return false;
        }
        if (chessboard.getPiece(move.getFrom()) == Piece.WHITE_PAWN && move.getTo().getRank().equals(Rank.RANK_8)) {
            return true;
        } else {
            return chessboard.getPiece(move.getFrom()) == Piece.BLACK_PAWN && move.getTo().getRank().equals(Rank.RANK_1);
        }
    }

    public boolean isOwnPiece(char piece) {
        return (isPlayerWhite && Character.isUpperCase(piece) || (!isPlayerWhite && Character.isLowerCase(piece)));
    }

    public char getPiece(int rank, int file) {
        return chessboard.getPiece(squareAt(rank, file)).getFenSymbol().charAt(0);
    }

    public void doMove(String nextMove) {
        Move move = new Move(nextMove, chessboard.getSideToMove());
        chessboard.doMove(move, true);
    }
}
