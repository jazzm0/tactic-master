package com.tacticmaster;


import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.widget.EditText;
import android.widget.TextView;

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

    <T extends android.view.View> T findViewById(int id) {
        return ((Activity) context).findViewById(id);
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
            puzzleIdEditText.setAlpha(0.7f);
            puzzleIdEditText.setTypeface(null, Typeface.ITALIC);
        } else {
            setUnsolved();
        }
    }

    public void setPlayerRating(int playerRating) {
        playerRatingTextView.setText(context.getString(R.string.player_rating, playerRating));
        playerRatingTextView.setTypeface(null, Typeface.BOLD);
    }

    public void updatePlayerRating(int oldRating, int newRating) {
        int duration = 1400;
        ValueAnimator animator = ValueAnimator.ofInt(oldRating, newRating);
        animator.setDuration(duration);
        animator.addUpdateListener(animation -> {
            int animatedValue = (int) animation.getAnimatedValue();
            String fullText = context.getString(R.string.player_rating, animatedValue);

            SpannableString spannable = new SpannableString(fullText);
            int start = fullText.indexOf(String.valueOf(animatedValue));
            int end = start + String.valueOf(animatedValue).length();

            int color = (newRating > oldRating) ? Color.GREEN : (newRating < oldRating) ? Color.RED : Color.BLACK;
            spannable.setSpan(new ForegroundColorSpan(color), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            playerRatingTextView.setText(spannable);
        });

        ValueAnimator alphaAnimator = ValueAnimator.ofFloat(0.2f, 0.8f);
        alphaAnimator.setDuration(duration);
        alphaAnimator.addUpdateListener(animation -> {
            float alphaValue = (float) animation.getAnimatedValue();
            playerRatingTextView.setAlpha(alphaValue);
        });

        animator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                String fullText = context.getString(R.string.player_rating, newRating);
                SpannableString spannable = new SpannableString(fullText);
                int start = fullText.indexOf(String.valueOf(newRating));
                int end = start + String.valueOf(newRating).length();

                spannable.setSpan(new ForegroundColorSpan(Color.BLACK), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                playerRatingTextView.setText(spannable);
            }
        });

        animator.start();
        alphaAnimator.start();
    }
}