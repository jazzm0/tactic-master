package com.tacticmaster;

import android.content.Intent;
import android.widget.Toast;

import com.tacticmaster.board.ChessboardView;
import com.tacticmaster.db.DatabaseAccessor;
import com.tacticmaster.puzzle.Puzzle;
import com.tacticmaster.rating.EloRatingCalculator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
    }

    private void updatePlayerRating(int opponentRating, double result) {
        playerRating = EloRatingCalculator.calculateNewRating(playerRating, opponentRating, result);
        databaseAccessor.storePlayerRating(playerRating);
        puzzleTextViews.setPlayerRating(playerRating);
    }

    private void loadNextPuzzles() {
        var nextPuzzles = databaseAccessor
                .getPuzzlesWithinRange(
                        this.playerRating - 50,
                        this.playerRating + 200, loadedPuzzleIds);
        nextPuzzles.forEach(puzzle -> loadedPuzzleIds.add(puzzle.puzzleId()));
        this.loadedPuzzles.addAll(nextPuzzles);
    }

    private void shareText(String t) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, t);
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, null);
        chessboardView.getContext().startActivity(shareIntent);
    }

    public void renderPuzzle() {
        Puzzle puzzle = playedPuzzles.get(currentPuzzleIndex);
        currentPuzzleId = puzzle.puzzleId();
        chessboardView.setPuzzle(puzzle);

        puzzleTextViews.setPuzzleId(puzzle.puzzleId());
        puzzleTextViews.setPuzzleRating(puzzle.rating());
        puzzleTextViews.setPuzzlesSolved(databaseAccessor.getSolvedPuzzleCount(), databaseAccessor.getAllPuzzleCount());
        puzzleTextViews.setPlayerRating(playerRating);
    }

    public void loadPreviousPuzzle() {
        currentPuzzleIndex -= 1;
        if (currentPuzzleIndex < 0) {
            currentPuzzleIndex = playedPuzzles.size() - 1;
        }
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
            var nextPuzzle = randomNumberGenerator.nextDouble() < 0.3 ? loadedPuzzles.pollFirst() : loadedPuzzles.pollLast();
            this.playedPuzzles.add(nextPuzzle);
        }
        renderPuzzle();
    }

    public void loadPuzzleById(String puzzleId) {
        try {
            Puzzle nextPuzzle = databaseAccessor.getPuzzleById(puzzleId);
            if(!loadedPuzzleIds.contains(puzzleId)) {
                currentPuzzleIndex = this.playedPuzzles.size();
                this.playedPuzzles.add(nextPuzzle);
                loadedPuzzleIds.add(nextPuzzle.puzzleId());
            }
            else {
                currentPuzzleIndex = this.playedPuzzles.lastIndexOf(nextPuzzle);
            }
        }
        catch (RuntimeException e) {
            Toast.makeText(chessboardView.getContext(), "Invalid puzzle ID", Toast.LENGTH_SHORT).show();
            return;
        }
        renderPuzzle();
    }

    public void puzzleIdLinkClicked() {
        shareText("https://lichess.org/training/"+currentPuzzleId);
    }
    
    public void puzzleHintClicked() {
        chessboardView.puzzleHintClicked();
    }

    @Override
    public void onPuzzleSolved(Puzzle puzzle) {
        databaseAccessor.setSolved(puzzle.puzzleId());
        updatePlayerRating(puzzle.rating(), 1.0);
        loadNextPuzzle();
    }

    @Override
    public void onPuzzleNotSolved(Puzzle puzzle) {
        updatePlayerRating(puzzle.rating(), 0.0);
        loadNextPuzzle();
    }
}