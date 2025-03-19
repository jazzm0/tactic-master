// src/main/java/com/tacticmaster/ChessboardController.java
package com.tacticmaster;

import android.content.Context;
import android.widget.TextView;

import com.tacticmaster.board.ChessboardView;
import com.tacticmaster.db.DatabaseAccessor;
import com.tacticmaster.puzzle.Puzzle;

import java.util.List;

public class ChessboardController implements ChessboardView.PuzzleFinishedListener {

    private final DatabaseAccessor databaseAccessor;
    private final ChessboardView chessboardView;
    private final TextView puzzleIdTextView;
    private final TextView puzzleRatingTextView;
    private final TextView puzzleThemesTextView;
    private final TextView puzzleMovesTextView;
    private final TextView puzzlePopularityTextView;
    private final TextView puzzleNbPlaysTextView;
    private final Context context;

    private int currentPuzzleIndex = 0;
    private List<Puzzle> puzzles;

    public ChessboardController(Context context,
                                DatabaseAccessor databaseAccessor,
                                ChessboardView chessboardView,
                                TextView puzzleIdTextView,
                                TextView puzzleRatingTextView,
                                TextView puzzleThemesTextView,
                                TextView puzzleMovesTextView,
                                TextView puzzlePopularityTextView,
                                TextView puzzleNbPlaysTextView) {
        this.context = context;
        this.databaseAccessor = databaseAccessor;
        this.chessboardView = chessboardView;
        this.puzzleIdTextView = puzzleIdTextView;
        this.puzzleRatingTextView = puzzleRatingTextView;
        this.puzzleThemesTextView = puzzleThemesTextView;
        this.puzzleMovesTextView = puzzleMovesTextView;
        this.puzzlePopularityTextView = puzzlePopularityTextView;
        this.puzzleNbPlaysTextView = puzzleNbPlaysTextView;
        this.chessboardView.setPuzzleSolvedListener(this);
    }

    public void loadPuzzlesWithRatingGreaterThan(int rating) {
        this.puzzles = databaseAccessor.getPuzzlesWithRatingGreaterThan(0);
        if (!puzzles.isEmpty()) {
            // Assuming you want to display the first puzzle for simplicity
            renderPuzzle();
        }
    }

    private void renderPuzzle() {
        Puzzle puzzle = puzzles.get(currentPuzzleIndex);
        chessboardView.setPuzzle(puzzle);

        puzzleIdTextView.setText(context.getString(R.string.puzzle_id, puzzle.puzzleId()));
        puzzleRatingTextView.setText(context.getString(R.string.rating, puzzle.rating()));
        puzzleThemesTextView.setText(context.getString(R.string.themes, puzzle.themes()));
        puzzleMovesTextView.setText(context.getString(R.string.moves, puzzle.moves()));
        puzzlePopularityTextView.setText(context.getString(R.string.puzzle_popularity, puzzle.popularity()));
        puzzleNbPlaysTextView.setText(context.getString(R.string.puzzle_nbplays, puzzle.nbPlays()));
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

    @Override
    public void onPuzzleSolved(Puzzle puzzle) {
        databaseAccessor.setSolved(puzzle.puzzleId());
        this.puzzles.remove(currentPuzzleIndex);
        loadNextPuzzle();
    }

    @Override
    public void onPuzzleNotSolved(Puzzle puzzle) {
        loadNextPuzzle();
    }
}