package com.tacticmaster;

import static java.util.Objects.isNull;

import android.content.Intent;
import android.util.Log;

import com.tacticmaster.board.ChessboardView;
import com.tacticmaster.db.DatabaseAccessor;
import com.tacticmaster.puzzle.PuzzleGame;
import com.tacticmaster.puzzle.PuzzleManager;
import com.tacticmaster.puzzle.PuzzleThemesDialogHelper;
import com.tacticmaster.rating.EloRatingCalculator;

import java.util.NoSuchElementException;

public class ChessboardController implements ChessboardView.PuzzleFinishedListener {

    private static final String TAG = "ChessboardController";
    private static final String LICHESS_TRAINING_URL = "https://lichess.org/training/";

    private final DatabaseAccessor databaseAccessor;
    private final ChessboardView chessboardView;
    private final PuzzleTextViews puzzleTextViews;
    private final PuzzleManager puzzleManager;
    private final PuzzleThemesDialogHelper puzzleThemesDialogHelper;

    private int playerRating;
    private boolean autoplay;

    public ChessboardController(
            DatabaseAccessor databaseAccessor,
            PuzzleManager puzzleManager,
            PuzzleThemesDialogHelper puzzleThemesDialogHelper,
            ChessboardView chessboardView,
            PuzzleTextViews puzzleTextViews) {

        if (isNull(databaseAccessor)) {
            throw new IllegalArgumentException("DatabaseAccessor cannot be null");
        }
        if (isNull(puzzleManager)) {
            throw new IllegalArgumentException("PuzzleManager cannot be null");
        }
        if (isNull(puzzleThemesDialogHelper)) {
            throw new IllegalArgumentException("PuzzleThemesDialogHelper cannot be null");
        }
        if (isNull(chessboardView)) {
            throw new IllegalArgumentException("ChessboardView cannot be null");
        }
        if (isNull(puzzleTextViews)) {
            throw new IllegalArgumentException("PuzzleTextViews cannot be null");
        }

        this.databaseAccessor = databaseAccessor;
        this.puzzleManager = puzzleManager;
        this.puzzleThemesDialogHelper = puzzleThemesDialogHelper;
        this.chessboardView = chessboardView;
        this.puzzleTextViews = puzzleTextViews;
        this.chessboardView.setPuzzleSolvedListener(this);
        this.playerRating = databaseAccessor.getPlayerRating();
        this.autoplay = databaseAccessor.getPlayerAutoplay();

        Log.d(TAG, "ChessboardController initialized with player rating: " + playerRating);
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
        puzzleThemesDialogHelper.prepareDialogContent(chessboardView.getContext(), puzzleTextViews.getFilterButton(), puzzleTextViews.getFilterDropdown(), this::renderPuzzle);
    }

    public void loadPreviousPuzzle() {
        puzzleManager.moveToPreviousPuzzle();
        renderPuzzle();
    }

    public void loadNextPuzzle() {
        try {
            puzzleManager.moveToNextPuzzle();
            renderPuzzle();
            Log.d(TAG, "Successfully loaded next puzzle");
        } catch (NoSuchElementException e) {
            chessboardView.makeText(R.string.no_more_puzzles);
            Log.w(TAG, "No more puzzles available", e);
        } catch (Exception e) {
            Log.e(TAG, "Error loading next puzzle", e);
            chessboardView.makeText(R.string.no_more_puzzles);
        }
    }

    public void loadPuzzleById(String puzzleId) {
        if (isNull(puzzleId) || puzzleId.trim().isEmpty()) {
            Log.w(TAG, "Invalid puzzle ID provided: " + puzzleId);
            chessboardView.makeText(R.string.invalid_puzzle_id);
            return;
        }

        try {
            puzzleManager.loadPuzzleById(puzzleId);
            renderPuzzle();
            Log.d(TAG, "Successfully loaded puzzle by ID: " + puzzleId);
        } catch (NoSuchElementException e) {
            chessboardView.makeText(R.string.invalid_puzzle_id);
            Log.w(TAG, "Invalid puzzle ID: " + puzzleId, e);
        } catch (Exception e) {
            Log.e(TAG, "Error loading puzzle by ID: " + puzzleId, e);
            chessboardView.makeText(R.string.invalid_puzzle_id);
        }
    }

    public void puzzleIdLinkClicked() {
        try {
            PuzzleGame currentPuzzle = puzzleManager.getCurrentPuzzle();
            if (isNull(currentPuzzle)) {
                Log.w(TAG, "No current puzzle available for sharing");
                return;
            }

            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, LICHESS_TRAINING_URL + currentPuzzle.getPuzzleId());
            sendIntent.setType("text/plain");

            Intent shareIntent = Intent.createChooser(sendIntent, null);
            chessboardView.getContext().startActivity(shareIntent);
            Log.d(TAG, "Shared puzzle link: " + currentPuzzle.getPuzzleId());
        } catch (Exception e) {
            Log.e(TAG, "Error sharing puzzle link", e);
        }
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

    /**
     * Cleans up resources and cancels any running operations.
     * Should be called when the controller is no longer needed.
     */
    public void cleanup() {
        if (puzzleTextViews != null) {
            puzzleTextViews.cleanup();
        }
    }
}
