package com.tacticmaster;

import android.content.Intent;

import com.tacticmaster.board.ChessboardView;
import com.tacticmaster.db.DatabaseAccessor;
import com.tacticmaster.db.PuzzleThemesManager;
import com.tacticmaster.puzzle.PuzzleGame;
import com.tacticmaster.puzzle.PuzzleManager;
import com.tacticmaster.rating.EloRatingCalculator;

import java.util.NoSuchElementException;

public class ChessboardController implements ChessboardView.PuzzleFinishedListener {

    private final DatabaseAccessor databaseAccessor;
    private final ChessboardView chessboardView;
    private final PuzzleTextViews puzzleTextViews;
    private final PuzzleManager puzzleManager;
    private final PuzzleThemesManager puzzleThemesManager;

    private int playerRating;
    private boolean autoplay;

    public ChessboardController(
            DatabaseAccessor databaseAccessor,
            PuzzleManager puzzleManager,
            PuzzleThemesManager puzzleThemesManager,
            ChessboardView chessboardView,
            PuzzleTextViews puzzleTextViews) {
        this.databaseAccessor = databaseAccessor;
        this.puzzleManager = puzzleManager;
        this.puzzleThemesManager = puzzleThemesManager;
        this.chessboardView = chessboardView;
        this.puzzleTextViews = puzzleTextViews;
        this.chessboardView.setPuzzleSolvedListener(this);
        this.playerRating = databaseAccessor.getPlayerRating();
        this.autoplay = databaseAccessor.getPlayerAutoplay();
    }

    private void updatePlayerRating(int opponentRating, double result) {
        var newRating = EloRatingCalculator.calculateNewRating(playerRating, opponentRating, result);
        databaseAccessor.storePlayerRating(newRating);
        puzzleTextViews.updatePlayerRating(playerRating, newRating);
        this.playerRating = newRating;
        puzzleManager.updateRating(newRating);
    }


    public void renderPuzzle() {
        var puzzle = puzzleManager.getCurrentPuzzle();
        chessboardView.setPuzzle(puzzle);

        puzzleTextViews.setPuzzleId(puzzle.getPuzzleId());
        puzzleTextViews.setPuzzleRating(puzzle.rating());
        puzzleTextViews.setPuzzlesSolvedCount(databaseAccessor.getSolvedPuzzleCount(), databaseAccessor.getAllPuzzleCount());
        puzzleTextViews.setPlayerRating(playerRating);
        puzzleTextViews.setPuzzleSolved(puzzle.solved());
        puzzleThemesManager.setThemes(chessboardView.getContext(), puzzleTextViews.getFilterButton(), puzzleTextViews.getFilterDropdown(), this::renderPuzzle);
    }

    public void loadPreviousPuzzle() {
        puzzleManager.moveToPreviousPuzzle();
        renderPuzzle();
    }

    public void loadNextPuzzle() {
        try {
            puzzleManager.moveToNextPuzzle();
            renderPuzzle();
        } catch (NoSuchElementException e) {
            chessboardView.makeText(R.string.no_more_puzzles);
        }
    }

    public void loadPuzzleById(String puzzleId) {
        try {
            puzzleManager.loadPuzzleById(puzzleId);
            renderPuzzle();
        } catch (NoSuchElementException e) {
            chessboardView.makeText(R.string.invalid_puzzle_id);
        }
    }

    public void puzzleIdLinkClicked() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "https://lichess.org/training/" + puzzleManager.getCurrentPuzzle().getPuzzleId());
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, null);
        chessboardView.getContext().startActivity(shareIntent);
    }

    public void puzzleHintClicked() {
        chessboardView.puzzleHintClicked();
    }

    public void setAutoplay(boolean isChecked) {
        this.autoplay = isChecked;
        databaseAccessor.storePlayerAutoplay(isChecked);
    }

    public boolean getAutoplay() {
        return this.autoplay;
    }

    @Override
    public void onPuzzleSolved(PuzzleGame puzzle) {
        if (databaseAccessor.wasNotSolved(puzzle.getPuzzleId())) {
            databaseAccessor.setSolved(puzzle.getPuzzleId());
            updatePlayerRating(puzzle.rating(), 1.0);
            puzzle.setSolved(true);
            puzzleTextViews.setPuzzleSolved(true);
        }
    }

    @Override
    public void onPuzzleNotSolved(PuzzleGame puzzle) {
        if (databaseAccessor.wasNotSolved(puzzle.getPuzzleId())) {
            updatePlayerRating(puzzle.rating(), 0.0);
        }
    }

    @Override
    public void onAfterPuzzleFinished(PuzzleGame puzzle) {
        if (this.autoplay) {
            loadNextPuzzle();
        }
    }
}