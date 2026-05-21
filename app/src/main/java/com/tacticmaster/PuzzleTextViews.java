package com.tacticmaster;

import static java.util.Objects.isNull;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

public class PuzzleTextViews {

    private static final int ANIMATION_DURATION = 1400;
    private static final int ALPHA_ANIMATION_DURATION = ANIMATION_DURATION * 3;
    private static final float ALPHA_START = 0.4f;
    private static final float ALPHA_END = 0.9f;
    private static final float SOLVED_ALPHA = 0.7f;
    private static final float UNSOLVED_ALPHA = 1.0f;

    private final Activity activity;
    private final TextView puzzleIdLabelTextView;
    private final EditText puzzleIdEditText;
    private final TextView puzzleRatingTextView;
    private final TextView puzzlesSolvedTextView;
    private final TextView playerRatingTextView;
    private final ImageView solvedIconView;

    private ValueAnimator currentRatingAnimator;
    private ValueAnimator currentAlphaAnimator;

    public PuzzleTextViews(@NonNull Context context) {
        if (isNull(context)) {
            throw new IllegalArgumentException("Context cannot be null");
        }
        if (!(context instanceof Activity)) {
            throw new IllegalArgumentException("Context must be an Activity instance");
        }

        this.activity = (Activity) context;
        this.puzzleIdLabelTextView = activity.findViewById(R.id.puzzle_id_label);
        this.puzzleIdEditText = activity.findViewById(R.id.puzzle_id);
        this.puzzleRatingTextView = activity.findViewById(R.id.puzzle_rating);
        this.puzzlesSolvedTextView = activity.findViewById(R.id.puzzles_count);
        this.playerRatingTextView = activity.findViewById(R.id.player_rating);
        this.solvedIconView = activity.findViewById(R.id.solved);

        if (isNull(puzzleIdLabelTextView) || isNull(puzzleIdEditText) || isNull(puzzleRatingTextView)
                || isNull(puzzlesSolvedTextView) || isNull(playerRatingTextView)) {
            throw new RuntimeException("One or more required views could not be found");
        }
    }

    @Nullable
    public MaterialButton getFilterButton() {
        return activity.findViewById(R.id.filter_button);
    }

    @Nullable
    public MaterialAutoCompleteTextView getFilterDropdown() {
        return activity.findViewById(R.id.filter_dropdown);
    }

    public void setPuzzleRating(int rating) {
        String text = activity.getString(R.string.rating, rating);
        puzzleRatingTextView.setText(text);
        puzzleRatingTextView.setTypeface(null, Typeface.BOLD);
        puzzleRatingTextView.setContentDescription(text + " rating");
    }

    public void setPuzzleId(@Nullable String puzzleId) {
        if (isNull(puzzleId) || puzzleId.trim().isEmpty()) {
            return;
        }
        puzzleIdLabelTextView.setTypeface(null, Typeface.BOLD);
        puzzleIdEditText.setText(puzzleId);
        puzzleIdEditText.setContentDescription("Puzzle ID: " + puzzleId);
        setUnsolvedState();
    }

    public void setPuzzlesSolvedCount(int solvedCount, int totalCount) {
        puzzlesSolvedTextView.setText(activity.getString(R.string.puzzles_solved, solvedCount, totalCount));
        puzzlesSolvedTextView.setTypeface(null, Typeface.BOLD);
        puzzlesSolvedTextView.setContentDescription(solvedCount + " out of " + totalCount + " puzzles solved");
    }

    public void setPuzzleSolved(boolean solved) {
        if (solved) {
            applyTextViewStyle(puzzleIdEditText, Color.GREEN, SOLVED_ALPHA);
            puzzleIdEditText.setContentDescription("Puzzle solved");
        } else {
            setUnsolvedState();
            puzzleIdEditText.setContentDescription("Puzzle not solved");
        }
        if (!isNull(solvedIconView)) {
            solvedIconView.setVisibility(solved ? View.VISIBLE : View.GONE);
        }
    }

    public void setPlayerRating(int playerRating) {
        playerRatingTextView.setText(activity.getString(R.string.player_rating, playerRating));
        playerRatingTextView.setTypeface(null, Typeface.BOLD);
        playerRatingTextView.setContentDescription("Player rating: " + playerRating);
    }

    public void updatePlayerRating(int oldRating, int newRating) {
        cancelCurrentAnimations();
        startRatingAnimation(oldRating, newRating);
        startAlphaAnimation(newRating);
    }

    public void cleanup() {
        cancelCurrentAnimations();
        currentRatingAnimator = null;
        currentAlphaAnimator = null;
    }

    private void setUnsolvedState() {
        puzzleIdEditText.clearFocus();
        applyTextViewStyle(puzzleIdEditText, Color.BLACK, UNSOLVED_ALPHA);
    }

    private void applyTextViewStyle(@NonNull TextView textView, int color, float alpha) {
        textView.setTextColor(color);
        textView.setAlpha(alpha);
        textView.setTypeface(null, Typeface.BOLD);
    }

    private void startRatingAnimation(int oldRating, int newRating) {
        int color = ratingChangeColor(oldRating, newRating);
        currentRatingAnimator = ValueAnimator.ofInt(oldRating, newRating);
        currentRatingAnimator.setDuration(ANIMATION_DURATION);
        currentRatingAnimator.addUpdateListener(animation -> {
            int value = (int) animation.getAnimatedValue();
            String fullText = activity.getString(R.string.player_rating, value);
            SpannableString spannable = new SpannableString(fullText);
            String ratingString = String.valueOf(value);
            int start = fullText.indexOf(ratingString);
            if (start >= 0) {
                int end = start + ratingString.length();
                spannable.setSpan(new ForegroundColorSpan(color), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            playerRatingTextView.setText(spannable);
        });
        currentRatingAnimator.start();
    }

    private void startAlphaAnimation(int newRating) {
        currentAlphaAnimator = ValueAnimator.ofFloat(ALPHA_START, ALPHA_END);
        currentAlphaAnimator.setDuration(ALPHA_ANIMATION_DURATION);
        currentAlphaAnimator.addUpdateListener(animation ->
                playerRatingTextView.setAlpha((float) animation.getAnimatedValue()));
        currentAlphaAnimator.addListener(new AnimatorListenerAdapter() {
            private boolean cancelled;

            @Override
            public void onAnimationCancel(Animator animation) {
                cancelled = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (cancelled) {
                    return;
                }
                playerRatingTextView.setAlpha(UNSOLVED_ALPHA);
                setPlayerRating(newRating);
            }
        });
        currentAlphaAnimator.start();
    }

    private int ratingChangeColor(int oldRating, int newRating) {
        if (newRating > oldRating) return Color.GREEN;
        if (newRating < oldRating) return Color.RED;
        return Color.BLACK;
    }

    private void cancelCurrentAnimations() {
        if (currentRatingAnimator != null && currentRatingAnimator.isRunning()) {
            currentRatingAnimator.cancel();
        }
        if (currentAlphaAnimator != null && currentAlphaAnimator.isRunning()) {
            currentAlphaAnimator.cancel();
        }
    }
}
