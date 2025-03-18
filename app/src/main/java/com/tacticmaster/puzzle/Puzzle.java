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
) {
}
