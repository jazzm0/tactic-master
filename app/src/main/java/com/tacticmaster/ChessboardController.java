package com.tacticmaster;

import static java.util.Objects.isNull;

import android.content.Intent;
import android.widget.Toast;

import com.tacticmaster.board.ChessboardView;
import com.tacticmaster.db.DatabaseAccessor;
import com.tacticmaster.puzzle.Puzzle;
import com.tacticmaster.rating.EloRatingCalculator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

public class ChessboardController implements ChessboardView.PuzzleFinishedListener {

    private final DatabaseAccessor databaseAccessor;
    private final ChessboardView chessboardView;
    private final PuzzleTextViews puzzleTextViews;

    private int currentPuzzleIndex = 0;
    private String currentPuzzleId = "";
    private final Set<String> loadedPuzzleIds = new HashSet<>();
    private final TreeSet<Puzzle> loadedPuzzles = new TreeSet<>();
    private final List<Puzzle> playedPuzzles = new ArrayList<>();
    private final Random randomNumberGenerator;
    private int playerRating;
    private boolean autoplay;

    public ChessboardController(
            DatabaseAccessor databaseAccessor,
            ChessboardView chessboardView,
            PuzzleTextViews puzzleTextViews, Random randomNumberGenerator) {
        this.databaseAccessor = databaseAccessor;
        this.chessboardView = chessboardView;
        this.puzzleTextViews = puzzleTextViews;
        this.randomNumberGenerator = randomNumberGenerator;
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

    private void loadNextPuzzles() {
        var nextPuzzles = databaseAccessor
                .getPuzzlesWithinRange(
                        this.playerRating - 50,
                        this.playerRating + 200, loadedPuzzleIds);
        nextPuzzles.forEach(puzzle -> loadedPuzzleIds.add(puzzle.puzzleId()));
        this.loadedPuzzles.addAll(nextPuzzles);
    }

    Puzzle getCurrentPuzzle() {
        return playedPuzzles.get(currentPuzzleIndex);
    }

    public void renderPuzzle() {
        Puzzle puzzle = playedPuzzles.get(currentPuzzleIndex);
        currentPuzzleId = puzzle.puzzleId();
        chessboardView.setPuzzle(puzzle);

        puzzleTextViews.setPuzzleId(puzzle.puzzleId());
        puzzleTextViews.setPuzzleRating(puzzle.rating());
        puzzleTextViews.setPuzzlesSolvedCount(databaseAccessor.getSolvedPuzzleCount(), databaseAccessor.getAllPuzzleCount());
        puzzleTextViews.setPlayerRating(playerRating);
        puzzleTextViews.setPuzzleSolved(puzzle.solved());
    }

    public void loadPreviousPuzzle() {
        if (loadedPuzzles.isEmpty()) {
            loadNextPuzzles();
        }
        currentPuzzleIndex = (currentPuzzleIndex - 1 + playedPuzzles.size()) % playedPuzzles.size();
        renderPuzzle();
    }

    public void loadNextPuzzle() {
        if (!this.playedPuzzles.isEmpty()) {
            currentPuzzleIndex += 1;
        }
        if (currentPuzzleIndex >= playedPuzzles.size()) {
            if (loadedPuzzles.isEmpty()) {
                loadNextPuzzles();
            }
            var nextPuzzle = loadedPuzzles.isEmpty() ? null :
                    (randomNumberGenerator.nextDouble() < 0.3 ? loadedPuzzles.pollFirst() : loadedPuzzles.pollLast());
            if (!isNull(nextPuzzle)) {
                this.playedPuzzles.add(nextPuzzle);
            } else {
                Toast.makeText(chessboardView.getContext(), R.string.no_more_puzzles, Toast.LENGTH_SHORT).show();
                return;
            }
        }
        renderPuzzle();
    }

    public void loadPuzzleById(String puzzleId) {
        try {
            Puzzle nextPuzzle = databaseAccessor.getPuzzleById(puzzleId);
            if (!loadedPuzzleIds.contains(puzzleId)) {
                currentPuzzleIndex = this.playedPuzzles.size();
                this.playedPuzzles.add(nextPuzzle);
                loadedPuzzleIds.add(nextPuzzle.puzzleId());
            } else {
                currentPuzzleIndex = this.playedPuzzles.lastIndexOf(nextPuzzle);
            }
        } catch (NoSuchElementException e) {
            Toast.makeText(chessboardView.getContext(), R.string.invalid_puzzle_id, Toast.LENGTH_SHORT).show();
            return;
        }
        renderPuzzle();
    }

    public void puzzleIdLinkClicked() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "https://lichess.org/training/" + currentPuzzleId);
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
    public void onPuzzleSolved(Puzzle puzzle) {
        if (databaseAccessor.wasNotSolved(puzzle.puzzleId())) {
            databaseAccessor.setSolved(puzzle.puzzleId());
            updatePlayerRating(puzzle.rating(), 1.0);
            var updatedPuzzle = new Puzzle(puzzle.puzzleId(), puzzle.fen(), puzzle.moves(), puzzle.rating(), true);
            if (playedPuzzles.isEmpty()) {
                playedPuzzles.add(updatedPuzzle);
            } else {
                playedPuzzles.set(currentPuzzleIndex, updatedPuzzle);
            }
            puzzleTextViews.setPuzzleSolved(true);
        }
        if (this.autoplay) {
            loadNextPuzzle();
        }
    }

    @Override
    public void onPuzzleNotSolved(Puzzle puzzle) {
        if (databaseAccessor.wasNotSolved(puzzle.puzzleId())) {
            updatePlayerRating(puzzle.rating(), 0.0);
        }
    }
}