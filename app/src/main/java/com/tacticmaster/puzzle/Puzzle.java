package com.tacticmaster.puzzle;

public record Puzzle(
        String puzzleId,
        String fen,
        String moves,
        int rating,
        boolean solved
) {

    public Puzzle(String puzzleId,
                  String fen,
                  String moves,
                  int rating) {
        this(puzzleId, fen, moves, rating, false);
    }
}
