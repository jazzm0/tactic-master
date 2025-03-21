package com.tacticmaster;

import android.content.Context;
import android.widget.TextView;

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
    private final TextView puzzleIdTextView;
    private final TextView puzzleRatingTextView;
    private final TextView puzzlesSolvedTextView;
    private final TextView puzzleThemesTextView;
    private final TextView puzzleMovesTextView;
    private final TextView puzzlePopularityTextView;
    private final TextView puzzleNbPlaysTextView;
    private final TextView playerRatingTextView;
    private final Context context;

    private int currentPuzzleIndex = 0;
    private final Set<String> loadedPuzzleIds = new HashSet<>();
    private List<Puzzle> puzzles;
    private int playerRating;

    public ChessboardController(Context context,
                                DatabaseAccessor databaseAccessor,
                                ChessboardView chessboardView,
                                TextView puzzleIdTextView,
                                TextView puzzleRatingTextView,
                                TextView puzzleCountTextView,
                                TextView puzzleThemesTextView,
                                TextView puzzleMovesTextView,
                                TextView puzzlePopularityTextView,
                                TextView puzzleNbPlaysTextView,
                                TextView playerRatingTextView
    ) {
        this.context = context;
        this.databaseAccessor = databaseAccessor;
        this.chessboardView = chessboardView;
        this.puzzleIdTextView = puzzleIdTextView;
        this.puzzleRatingTextView = puzzleRatingTextView;
        this.puzzlesSolvedTextView = puzzleCountTextView;
        this.puzzleThemesTextView = puzzleThemesTextView;
        this.puzzleMovesTextView = puzzleMovesTextView;
        this.puzzlePopularityTextView = puzzlePopularityTextView;
        this.puzzleNbPlaysTextView = puzzleNbPlaysTextView;
        this.playerRatingTextView = playerRatingTextView;
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

        puzzleIdTextView.setText(context.getString(R.string.puzzle_id, puzzle.puzzleId()));
        puzzleRatingTextView.setText(context.getString(R.string.rating, puzzle.rating()));
        puzzlesSolvedTextView.setText(context.getString(R.string.puzzles_solved,
                databaseAccessor.getSolvedPuzzleCount(),
                databaseAccessor.getAllPuzzleCount()));
        puzzleThemesTextView.setText(context.getString(R.string.themes, puzzle.themes()));
        puzzleMovesTextView.setText(context.getString(R.string.moves, puzzle.moves()));
        puzzlePopularityTextView.setText(context.getString(R.string.puzzle_popularity, puzzle.popularity()));
        puzzleNbPlaysTextView.setText(context.getString(R.string.puzzle_nbplays, puzzle.nbPlays()));
        playerRatingTextView.setText(context.getString(R.string.player_rating, playerRating));
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
        updatePlayerRating(puzzle.rating(), 1.0); // Assuming a win
        this.puzzles.remove(currentPuzzleIndex);
        loadNextPuzzle();
    }

    @Override
    public void onPuzzleNotSolved(Puzzle puzzle) {
        updatePlayerRating(puzzle.rating(), 0.0); // Assuming a loss
        loadNextPuzzle();
    }

    private void updatePlayerRating(int opponentRating, double result) {
        playerRating = EloRatingCalculator.calculateNewRating(playerRating, opponentRating, result);
        databaseAccessor.storePlayerRating(playerRating);
        playerRatingTextView.setText(context.getString(R.string.player_rating, playerRating));
    }
}