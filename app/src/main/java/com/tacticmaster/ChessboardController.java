package com.tacticmaster;

import com.tacticmaster.board.ChessboardView;
import com.tacticmaster.db.DatabaseAccessor;
import com.tacticmaster.puzzle.Puzzle;
import com.tacticmaster.rating.EloRatingCalculator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ChessboardController implements ChessboardView.PuzzleFinishedListener {

    private final DatabaseAccessor databaseAccessor;
    private final ChessboardView chessboardView;
    private final PuzzleTextViews puzzleTextViews;

    private int currentPuzzleIndex = 0;
    private final Set<String> loadedPuzzleIds = new HashSet<>();
    private List<Puzzle> puzzles;
    private int playerRating;

    public ChessboardController(
            DatabaseAccessor databaseAccessor,
            ChessboardView chessboardView,
            PuzzleTextViews puzzleTextViews) {
        this.databaseAccessor = databaseAccessor;
        this.chessboardView = chessboardView;
        this.puzzleTextViews = puzzleTextViews;
        this.chessboardView.setPuzzleSolvedListener(this);
        this.playerRating = databaseAccessor.getPlayerRating();
    }

    public void loadNextPuzzles() {
        this.puzzles = fetchNextPuzzles();
        if (!puzzles.isEmpty()) {
            renderPuzzle();
        }
    }

    private List<Puzzle> fetchNextPuzzles() {
        var nextPuzzles = databaseAccessor
                .getPuzzlesWithinRange(
                        this.playerRating - 200,
                        this.playerRating + 200, loadedPuzzleIds);
        nextPuzzles.forEach(puzzle -> loadedPuzzleIds.add(puzzle.puzzleId()));
        return nextPuzzles;
    }

    private void renderPuzzle() {
        Puzzle puzzle = puzzles.get(currentPuzzleIndex);
        chessboardView.setPuzzle(puzzle);

        puzzleTextViews.setPuzzleId(puzzle.puzzleId());
        puzzleTextViews.setPuzzleRating(puzzle.rating());
        puzzleTextViews.setPuzzlesSolved(databaseAccessor.getSolvedPuzzleCount(), databaseAccessor.getAllPuzzleCount());
        puzzleTextViews.setPuzzleThemes(puzzle.themes());
        puzzleTextViews.setPuzzleMoves(puzzle.moves());
        puzzleTextViews.setPuzzlePopularity(puzzle.popularity());
        puzzleTextViews.setPuzzleNbPlays(puzzle.nbPlays());
        puzzleTextViews.setPlayerRating(playerRating);
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
            var newPuzzles = fetchNextPuzzles();
            this.puzzles.addAll(newPuzzles);
        }
        renderPuzzle();
    }

    @Override
    public void onPuzzleSolved(Puzzle puzzle) {
        databaseAccessor.setSolved(puzzle.puzzleId());
        updatePlayerRating(puzzle.rating(), 1.0);
        this.puzzles.remove(currentPuzzleIndex);
        loadNextPuzzle();
    }

    @Override
    public void onPuzzleNotSolved(Puzzle puzzle) {
        updatePlayerRating(puzzle.rating(), 0.0);
        loadNextPuzzle();
    }

    private void updatePlayerRating(int opponentRating, double result) {
        playerRating = EloRatingCalculator.calculateNewRating(playerRating, opponentRating, result);
        databaseAccessor.storePlayerRating(playerRating);
        puzzleTextViews.setPlayerRating(playerRating);
    }
}