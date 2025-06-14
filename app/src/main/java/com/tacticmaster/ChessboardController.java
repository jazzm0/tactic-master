package com.tacticmaster;

import android.content.Intent;

import com.tacticmaster.board.ChessboardView;
import com.tacticmaster.db.DatabaseAccessor;
import com.tacticmaster.puzzle.Puzzle;
import com.tacticmaster.puzzle.PuzzleGame;
import com.tacticmaster.rating.EloRatingCalculator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public class ChessboardController implements ChessboardView.PuzzleFinishedListener {

    private final DatabaseAccessor databaseAccessor;
    private final ChessboardView chessboardView;
    private final PuzzleTextViews puzzleTextViews;

    private int currentPuzzleIndex = -1;
    private final Map<String, PuzzleGame> loadedPuzzles = new LinkedHashMap<>();
    private int playerRating;
    private boolean autoplay;

    public ChessboardController(
            DatabaseAccessor databaseAccessor,
            ChessboardView chessboardView,
            PuzzleTextViews puzzleTextViews) {
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
        int lowestRating = playerRating - 50;
        int highestRating = playerRating + 50;
        List<Puzzle> nextPuzzles = new ArrayList<>();
        while (nextPuzzles.isEmpty() && lowestRating > 0) {
            nextPuzzles = databaseAccessor.getPuzzlesWithinRange(lowestRating, highestRating, loadedPuzzles.keySet());
            lowestRating -= 50;
            highestRating += 50;
        }
        if (nextPuzzles.isEmpty()) {
            throw new NoSuchElementException("No more unsolved puzzles available");
        }
        nextPuzzles.forEach(puzzle -> loadedPuzzles.put(puzzle.puzzleId(), new PuzzleGame(puzzle)));
    }

    private PuzzleGame getCurrentPuzzle() {
        return loadedPuzzles.get(new ArrayList<>(loadedPuzzles.keySet()).get(currentPuzzleIndex));
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
        currentPuzzleIndex = (currentPuzzleIndex - 1 + loadedPuzzles.size()) % loadedPuzzles.size();
        renderPuzzle();
    }

    public void loadNextPuzzle() {
        currentPuzzleIndex++;
        if (currentPuzzleIndex >= loadedPuzzles.size()) {
            try {
                loadNextPuzzles();
            } catch (NoSuchElementException e) {
                currentPuzzleIndex--;
                chessboardView.makeText(R.string.no_more_puzzles);
                return;
            }
        }
        if (currentPuzzleIndex < loadedPuzzles.size()) {
            renderPuzzle();
        }
    }

    public void loadPuzzleById(String puzzleId) {
        try {
            Puzzle nextPuzzle = databaseAccessor.getPuzzleById(puzzleId);
            if (!loadedPuzzles.containsKey(puzzleId)) {
                currentPuzzleIndex = loadedPuzzles.size();
                loadedPuzzles.put(nextPuzzle.puzzleId(), new PuzzleGame(nextPuzzle));
            } else {
                currentPuzzleIndex = new ArrayList<>(loadedPuzzles.keySet()).lastIndexOf(nextPuzzle.puzzleId());
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