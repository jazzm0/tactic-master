package com.tacticmaster.puzzle;

public record Puzzle(
        String puzzleId,
        String fen,
        String moves,
        int rating,
        boolean solved
) implements Comparable<Puzzle> {

    public Puzzle(String puzzleId,
                  String fen,
                  String moves,
                  int rating) {
        this(puzzleId, fen, moves, rating, false);
    }

    @Override
    public int compareTo(Puzzle o) {
        int ratingComparison = Integer.compare(this.rating, o.rating);
        if (ratingComparison != 0) {
            return ratingComparison;
        }
        return this.puzzleId.compareTo(o.puzzleId);
    }
}
