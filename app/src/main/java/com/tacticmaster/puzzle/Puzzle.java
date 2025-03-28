package com.tacticmaster.puzzle;

public record Puzzle(
        String puzzleId,
        String fen,
        String moves,
        int rating,
        int ratingDeviation,
        int popularity,
        int nbPlays,
        String themes,
        String gameUrl,
        String openingTags
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
