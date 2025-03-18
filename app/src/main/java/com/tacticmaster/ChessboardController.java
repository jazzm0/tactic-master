package com.tacticmaster;

import com.tacticmaster.board.ChessboardView;
import com.tacticmaster.db.DatabaseAccessor;
import com.tacticmaster.puzzle.Puzzle;

import java.util.List;

public class ChessboardController {

    private final DatabaseAccessor databaseAccessor;
    private final ChessboardView chessboardView;
    private int currentPuzzleIndex = 0;
    private List<Puzzle> puzzles;

    public ChessboardController(DatabaseAccessor databaseAccessor, ChessboardView chessboardView) {
        this.databaseAccessor = databaseAccessor;
        this.chessboardView = chessboardView;
    }

    public void loadPuzzlesWithRatingGreaterThan(int rating) {
        this.puzzles = databaseAccessor.getPuzzlesWithRatingGreaterThan(rating);
        if (!puzzles.isEmpty()) {
            // Assuming you want to display the first puzzle for simplicity
            renderPuzzle();
        }
    }

    private void renderPuzzle() {
        Puzzle puzzle = puzzles.get(currentPuzzleIndex);
        chessboardView.setPuzzle(puzzle);
    }

    public void loadPreviousPuzzle() {
        currentPuzzleIndex -= 1;
        if (currentPuzzleIndex < 0) {
            currentPuzzleIndex = puzzles.size() - 1;
        }
        renderPuzzle();
    }

    public void loadNextPuzzle() {
        currentPuzzleIndex += 1;
        if (currentPuzzleIndex >= puzzles.size()) {
            currentPuzzleIndex = 0;
        }
        renderPuzzle();
    }
}
