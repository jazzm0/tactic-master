package com.tacticmaster.puzzle;

import static java.util.Objects.requireNonNull;

import java.util.Arrays;
import java.util.Objects;

public class PuzzleGame implements Comparable<PuzzleGame> {

    private final String puzzleId;
    private final String fen;
    private final String[] moves;
    private final int rating;
    private final boolean solved;

    private int currentMoveIndex;

    public PuzzleGame(
            String puzzleId,
            String fen,
            String moves,
            int rating,
            boolean solved
    ) {
        this.puzzleId = puzzleId;
        this.fen = fen;
        this.moves = moves.trim().isEmpty() ? new String[0] : requireNonNull(moves.split(" "), "Moves cannot be null");
        this.rating = rating;
        this.currentMoveIndex = 0;
        this.solved = solved;
    }

    public PuzzleGame(Puzzle puzzleRecord) {
        this(puzzleRecord.puzzleId(), puzzleRecord.fen(), puzzleRecord.moves(), puzzleRecord.rating(), puzzleRecord.solved());
    }

    public PuzzleGame(String puzzleId,
                      String fen,
                      String moves,
                      int rating) {
        this(puzzleId, fen, moves, rating, false);
    }

    public String fen() {
        return this.fen;
    }

    public boolean isCorrectNextMove(String move) {
        return this.moves[currentMoveIndex].equals(move);
    }

    public String getNextMove() {
        return getNextMove(true);
    }

    public String getNextMove(boolean withIncrement) {
        if (currentMoveIndex >= this.moves.length) {
            return "";
        }
        String move = this.moves[currentMoveIndex];
        currentMoveIndex += withIncrement ? 1 : 0;
        return move;
    }

    public String getMoves() {
        return String.join(" ", moves);
    }

    public String getPuzzleId() {
        return puzzleId;
    }

    public int rating() {
        return this.rating;
    }

    public boolean solved() {
        return this.solved;
    }

    public void reset() {
        this.currentMoveIndex = 0;
    }

    public boolean isSolutionFound() {
        return currentMoveIndex == this.moves.length;
    }

    public boolean isStarted() {
        return currentMoveIndex > 0;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        PuzzleGame that = (PuzzleGame) o;
        return rating == that.rating && solved == that.solved && currentMoveIndex == that.currentMoveIndex && Objects.equals(puzzleId, that.puzzleId) && Objects.equals(fen, that.fen) && Objects.deepEquals(moves, that.moves);
    }

    @Override
    public int hashCode() {
        return Objects.hash(puzzleId, fen, Arrays.hashCode(moves), rating, solved, currentMoveIndex);
    }

    @Override
    public int compareTo(PuzzleGame o) {
        int ratingComparison = Integer.compare(this.rating, o.rating);
        if (ratingComparison != 0) {
            return ratingComparison;
        }
        return this.puzzleId.compareTo(o.puzzleId);
    }
}
