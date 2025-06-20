package com.tacticmaster.puzzle;

public record Puzzle(
        String puzzleId,
        String fen,
        String moves,
        int rating,
        String themes,
        boolean solved
) {

    public Puzzle(String puzzleId,
                  String fen,
                  String moves,
                  int rating,
                  String themes) {
        this(puzzleId, fen, moves, rating, themes, false);
    }

    public Puzzle(String puzzleId,
                  String fen,
                  String moves,
                  int rating) {
        this(puzzleId, fen, moves, rating, "", false);
    }
}
