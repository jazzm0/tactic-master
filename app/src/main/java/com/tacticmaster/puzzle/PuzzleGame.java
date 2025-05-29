package com.tacticmaster.puzzle;

import static java.util.Objects.requireNonNull;

public class PuzzleGame implements Comparable<PuzzleGame> {
    private final String puzzleId;
    private final String fen;
    private final String[] moves;
    private final int rating;
    private boolean solved;

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
        this.solved = solved;
        this.currentMoveIndex = 0;
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

    public String puzzleId() {
        return this.puzzleId;
    }

    public String fen() {
        return this.fen;
    }

    public String moves() {
        return String.join(" ", this.moves);
    }

    public boolean isCorrectNextMove(String move) {
        return this.moves[currentMoveIndex].equals(move);
    }

    public String getNextMove() {
        return getNextMove(true);
    }

    public String getNextMove(boolean inc) {
        if (currentMoveIndex >= this.moves.length) {
            return "";
        }
        String move = this.moves[currentMoveIndex];
        currentMoveIndex += inc ? 1 : 0;
        return move;
    }

    public int rating() {
        return this.rating;
    }

    public boolean solved() {
        return this.solved;
    }

    public void setSolved(boolean isSolved) {
        this.solved = isSolved;
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
    public int compareTo(PuzzleGame o) {
        int ratingComparison = Integer.compare(this.rating, o.rating);
        if (ratingComparison != 0) {
            return ratingComparison;
        }
        return this.puzzleId.compareTo(o.puzzleId);
    }
}
