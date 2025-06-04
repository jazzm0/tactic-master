package com.tacticmaster;

import android.content.Intent;

import com.tacticmaster.board.ChessboardView;
import com.tacticmaster.db.DatabaseAccessor;
import com.tacticmaster.puzzle.Puzzle;
import com.tacticmaster.puzzle.PuzzleGame;
import com.tacticmaster.rating.EloRatingCalculator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;

public class ChessboardController implements ChessboardView.PuzzleFinishedListener {

    private final DatabaseAccessor databaseAccessor;
    private final ChessboardView chessboardView;
    private final PuzzleTextViews puzzleTextViews;

    private int currentPuzzleIndex = -1;
    private final List<String> loadedPuzzleIds = new ArrayList<>();
    private final HashMap<String, PuzzleGame> loadedPuzzles = new HashMap<>();
    private int playerRating;
    private boolean autoplay;

    public ChessboardController(
            DatabaseAccessor databaseAccessor,
            ChessboardView chessboardView,
            PuzzleTextViews puzzleTextViews, Random randomNumberGenerator) {
        this.databaseAccessor = databaseAccessor;
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
    }

    private void loadNextPuzzles() throws NoSuchElementException {
        var nextPuzzles = databaseAccessor
                .getPuzzlesWithinRange(
                        this.playerRating - 50,
                        this.playerRating + 200, loadedPuzzleIds);
        if (nextPuzzles.isEmpty()) {
            throw new NoSuchElementException("No more unsolved puzzles available");
        }
        nextPuzzles.forEach(puzzle -> {
            loadedPuzzleIds.add(puzzle.puzzleId());
            loadedPuzzles.put(puzzle.puzzleId(), new PuzzleGame(puzzle));
        });
    }

    private PuzzleGame getCurrentPuzzle() {
        return loadedPuzzles.get(loadedPuzzleIds.get(currentPuzzleIndex));
    }

    public void renderPuzzle() {
        var puzzle = getCurrentPuzzle();
        chessboardView.setPuzzle(puzzle);

        puzzleTextViews.setPuzzleId(puzzle.getPuzzleId());
        puzzleTextViews.setPuzzleRating(puzzle.rating());
        puzzleTextViews.setPuzzlesSolvedCount(databaseAccessor.getSolvedPuzzleCount(), databaseAccessor.getAllPuzzleCount());
        puzzleTextViews.setPlayerRating(playerRating);
        puzzleTextViews.setPuzzleSolved(puzzle.solved());
    }

    public void loadPreviousPuzzle() {
        currentPuzzleIndex = (currentPuzzleIndex - 1 + loadedPuzzleIds.size()) % loadedPuzzleIds.size();
        renderPuzzle();
    }

    public void loadNextPuzzle() {
        currentPuzzleIndex++;
        if (currentPuzzleIndex >= loadedPuzzleIds.size()) {
            try {
                loadNextPuzzles();
            } catch (NoSuchElementException e) {
                currentPuzzleIndex--;
                chessboardView.makeText(R.string.no_more_puzzles);
                return;
            }
        }
        if (currentPuzzleIndex < loadedPuzzleIds.size()) {
            renderPuzzle();
        }
    }

    public void loadPuzzleById(String puzzleId) {
        try {
            Puzzle nextPuzzle = databaseAccessor.getPuzzleById(puzzleId);
            if (!loadedPuzzleIds.contains(puzzleId)) {
                currentPuzzleIndex = loadedPuzzleIds.size();
                loadedPuzzles.put(nextPuzzle.puzzleId(), new PuzzleGame(nextPuzzle));
                loadedPuzzleIds.add(nextPuzzle.puzzleId());
            } else {
                currentPuzzleIndex = loadedPuzzleIds.lastIndexOf(nextPuzzle.puzzleId());
            }
            renderPuzzle();
        } catch (NoSuchElementException e) {
            chessboardView.makeText(R.string.invalid_puzzle_id);
        }
    }

    public void puzzleIdLinkClicked() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "https://lichess.org/training/" + getCurrentPuzzle().getPuzzleId());
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