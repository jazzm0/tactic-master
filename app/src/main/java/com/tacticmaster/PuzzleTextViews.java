package com.tacticmaster;

import android.content.Context;
import android.graphics.Typeface;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class PuzzleTextViews {

    private final TextView puzzleIdTextView;
    private final TextView sharePuzzleIdTextView;
    private final TextView puzzleRatingTextView;
    private final TextView puzzlesSolvedTextView;
    private final TextView playerRatingTextView;
    private final Context context;

    public PuzzleTextViews(Context context) {
        this.context = context;
        this.puzzleIdTextView = findViewById(R.id.puzzle_id);
        this.sharePuzzleIdTextView = findViewById(R.id.share_puzzle_id);
        this.puzzleRatingTextView = findViewById(R.id.puzzle_rating);
        this.puzzlesSolvedTextView = findViewById(R.id.puzzles_count);
        this.playerRatingTextView = findViewById(R.id.player_rating);
    }

    private TextView findViewById(int id) {
        return ((AppCompatActivity) context).findViewById(id);
    }

    public void setPuzzleRating(int rating) {
        puzzleRatingTextView.setText(context.getString(R.string.rating, rating));
        puzzleRatingTextView.setTypeface(null, Typeface.BOLD);
    }

    public void setPuzzleId(String puzzleId) {
        puzzleIdTextView.setTypeface(null, Typeface.BOLD);
        sharePuzzleIdTextView.setText(puzzleId);
        sharePuzzleIdTextView.setTypeface(null, Typeface.BOLD);
    }

    public void setPuzzlesSolved(int solvedCount, int totalCount) {
        puzzlesSolvedTextView.setText(context.getString(R.string.puzzles_solved, solvedCount, totalCount));
        puzzlesSolvedTextView.setTypeface(null, Typeface.BOLD);
    }

    public void setPlayerRating(int playerRating) {
        playerRatingTextView.setText(context.getString(R.string.player_rating, playerRating));
        playerRatingTextView.setTypeface(null, Typeface.BOLD);
    }
}