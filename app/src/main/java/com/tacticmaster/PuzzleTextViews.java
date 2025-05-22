package com.tacticmaster;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class PuzzleTextViews {

    private final TextView puzzleIdLabelTextView;
    private final EditText puzzleIdEditText;
    private final TextView puzzleIdLinkTextView;
    private final TextView puzzleRatingTextView;
    private final TextView puzzlesSolvedTextView;
    private final TextView playerRatingTextView;
    private final Context context;

    public PuzzleTextViews(Context context) {
        this.context = context;
        this.puzzleIdLabelTextView = findViewById(R.id.puzzle_id_label);
        this.puzzleIdEditText = findViewById(R.id.puzzle_id);
        this.puzzleIdLinkTextView = findViewById(R.id.puzzle_id_link);
        this.puzzleRatingTextView = findViewById(R.id.puzzle_rating);
        this.puzzlesSolvedTextView = findViewById(R.id.puzzles_count);
        this.playerRatingTextView = findViewById(R.id.player_rating);
    }

    private <T extends android.view.View> T findViewById(int id) {
        return ((AppCompatActivity) context).findViewById(id);
    }

    private void setUnsolved() {
        puzzleIdEditText.clearFocus();
        puzzleIdEditText.setTextColor(Color.BLACK);
        puzzleIdEditText.setTypeface(null, Typeface.BOLD);
    }

    public void setPuzzleRating(int rating) {
        puzzleRatingTextView.setText(context.getString(R.string.rating, rating));
        puzzleRatingTextView.setTypeface(null, Typeface.BOLD);
    }

    public void setPuzzleId(String puzzleId) {
        puzzleIdLabelTextView.setTypeface(null, Typeface.BOLD);
        puzzleIdEditText.setText(puzzleId);
        setUnsolved();
        puzzleIdLinkTextView.setTypeface(null, Typeface.BOLD);
    }

    public void setPuzzlesSolvedCount(int solvedCount, int totalCount) {
        puzzlesSolvedTextView.setText(context.getString(R.string.puzzles_solved, solvedCount, totalCount));
        puzzlesSolvedTextView.setTypeface(null, Typeface.BOLD);
    }

    public void setPuzzleSolved(boolean solved) {
        if (solved) {
            puzzleIdEditText.setTextColor(Color.GREEN);
            puzzleIdEditText.setTypeface(null, Typeface.ITALIC);
        } else {
            setUnsolved();
        }
    }

    public void setPlayerRating(int playerRating) {
        playerRatingTextView.setText(context.getString(R.string.player_rating, playerRating));
        playerRatingTextView.setTypeface(null, Typeface.BOLD);
    }
}