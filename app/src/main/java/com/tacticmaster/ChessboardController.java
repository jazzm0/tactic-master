package com.tacticmaster;

import android.widget.TextView;

import com.tacticmaster.board.ChessboardView;
import com.tacticmaster.db.DatabaseAccessor;
import com.tacticmaster.puzzle.Puzzle;

import java.util.List;

public class ChessboardController {

    private final DatabaseAccessor databaseAccessor;
    private final ChessboardView chessboardView;
    private final TextView puzzleIdTextView;
    private final TextView puzzleRatingTextView;
    private final TextView puzzleThemesTextView;
    private final TextView puzzleMovesTextView;


    private int currentPuzzleIndex = 0;
    private List<Puzzle> puzzles;

    public ChessboardController(DatabaseAccessor databaseAccessor, ChessboardView chessboardView, TextView puzzleIdTextView, TextView puzzleRatingTextView, TextView puzzleThemesTextView, TextView puzzleMovesTextView) {
        this.databaseAccessor = databaseAccessor;
        this.chessboardView = chessboardView;
        this.puzzleIdTextView = puzzleIdTextView;
        this.puzzleRatingTextView = puzzleRatingTextView;
        this.puzzleThemesTextView = puzzleThemesTextView;
        this.puzzleMovesTextView = puzzleMovesTextView;
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

        puzzleIdTextView.setText("Puzzle ID: " + puzzle.puzzleId());
        puzzleRatingTextView.setText("Rating: " + puzzle.rating());
        puzzleThemesTextView.setText("Themes: " + puzzle.themes());
        puzzleMovesTextView.setText("Moves: " + puzzle.moves());
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
