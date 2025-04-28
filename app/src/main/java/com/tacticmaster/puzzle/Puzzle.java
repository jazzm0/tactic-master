package com.tacticmaster.puzzle;

public record Puzzle(
        String puzzleId,
        String fen,
        String moves,
        int rating
) implements Comparable<Puzzle> {

    @Override
    public int compareTo(Puzzle o) {
        int ratingComparison = Integer.compare(this.rating, o.rating);
        if (ratingComparison != 0) {
            return ratingComparison;
        }
        return this.puzzleId.compareTo(o.puzzleId);
    }
}
