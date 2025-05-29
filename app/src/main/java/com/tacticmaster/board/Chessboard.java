package com.tacticmaster.board;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.Rank;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;

import java.util.List;

// Adapter for chesslib-Board
public class Chessboard {
    private final Board chessboard;

    public Chessboard() {
        this.chessboard = new Board();
    }

    public boolean isMoveLeadingToMate(Move move) {
        // chessboard#isMoveLegal does not check whether the move is legal or not according to the
        // standard chess rules, but rather if the resulting configuration is valid, so we check
        // by searching in legalMoves()
        boolean isMated = false;
        boolean isMoveLegal = chessboard.legalMoves().contains(move);
        if (isMoveLegal) {
            chessboard.doMove(move, false);
            isMated = chessboard.isMated();
            chessboard.undoMove();
        }
        return isMated;
    }

    public boolean isPromotionMove(Move move) {
        if (chessboard.getPiece(move.getFrom()) == Piece.WHITE_PAWN && move.getTo().getRank().equals(Rank.RANK_8)) {
            return true;
        } else {
            return chessboard.getPiece(move.getFrom()) == Piece.BLACK_PAWN && move.getTo().getRank().equals(Rank.RANK_1);
        }
    }

    public void loadFromFen(String fen) {
        chessboard.loadFromFen(fen);
    }

    public Piece getPiece(Square square) {
        return chessboard.getPiece(square);
    }

    public boolean doMove(final Move move) {
        return doMove(move, false);
    }

    public boolean doMove(final Move move, boolean fullValidation) {
        return chessboard.doMove(move, fullValidation);
    }

    public Side getSideToMove() {
        return chessboard.getSideToMove();
    }
}
