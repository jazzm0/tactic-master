package com.tacticmaster;

import android.content.Context;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class PuzzleTextViews {

    private final TextView puzzleIdTextView;
    private final TextView puzzleRatingTextView;
    private final TextView puzzlesSolvedTextView;
    private final TextView puzzleThemesTextView;
    private final TextView puzzleMovesTextView;
    private final TextView puzzlePopularityTextView;
    private final TextView puzzleNbPlaysTextView;
    private final TextView playerRatingTextView;
    private final Context context;

    public PuzzleTextViews(Context context) {
        this.context = context;
        this.puzzleIdTextView = findViewById(R.id.puzzle_id);
        this.puzzleRatingTextView = findViewById(R.id.puzzle_rating);
        this.puzzlesSolvedTextView = findViewById(R.id.puzzles_count);
        this.puzzleThemesTextView = findViewById(R.id.puzzle_themes);
        this.puzzleMovesTextView = findViewById(R.id.puzzle_moves);
        this.puzzlePopularityTextView = findViewById(R.id.puzzle_popularity);
        this.puzzleNbPlaysTextView = findViewById(R.id.puzzle_nbplays);
        this.playerRatingTextView = findViewById(R.id.player_rating);
    }

    private TextView findViewById(int id) {
        return ((AppCompatActivity) context).findViewById(id);
    }

    public void setPuzzleId(String puzzleId) {
        puzzleIdTextView.setText(context.getString(R.string.puzzle_id, puzzleId));
    }

    public void setPuzzleRating(int rating) {
        puzzleRatingTextView.setText(context.getString(R.string.rating, rating));
    }

    public void setPuzzlesSolved(int solvedCount, int totalCount) {
        puzzlesSolvedTextView.setText(context.getString(R.string.puzzles_solved, solvedCount, totalCount));
    }

    public void setPuzzleThemes(String themes) {
        puzzleThemesTextView.setText(context.getString(R.string.themes, themes));
    }

    public void setPuzzleMoves(String moves) {
        puzzleMovesTextView.setText(context.getString(R.string.moves, moves));
    }

    public void setPuzzlePopularity(int popularity) {
        puzzlePopularityTextView.setText(context.getString(R.string.puzzle_popularity, popularity));
    }

    public void setPuzzleNbPlays(int nbPlays) {
        puzzleNbPlaysTextView.setText(context.getString(R.string.puzzle_nbplays, nbPlays));
    }

    public void setPlayerRating(int playerRating) {
        playerRatingTextView.setText(context.getString(R.string.player_rating, playerRating));
    }
}