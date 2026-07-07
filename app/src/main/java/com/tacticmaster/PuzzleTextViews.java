package com.tacticmaster;

import static java.util.Objects.isNull;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
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
    private boolean cleanedUp;

    // Reused per-frame to avoid allocating a fresh SpannableString during the rating tween.
    private final SpannableStringBuilder ratingTextBuffer = new SpannableStringBuilder();
    private ForegroundColorSpan ratingColorSpan = new ForegroundColorSpan(Color.BLACK);

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
        puzzleRatingTextView.setContentDescription(activity.getString(R.string.content_desc_puzzle_rating, rating));
    }

    public void setPuzzleId(@Nullable String puzzleId) {
        if (isNull(puzzleId) || puzzleId.trim().isEmpty()) {
            return;
        }
        puzzleIdLabelTextView.setTypeface(null, Typeface.BOLD);
        puzzleIdEditText.setText(puzzleId);
        puzzleIdEditText.setContentDescription(activity.getString(R.string.content_desc_puzzle_id, puzzleId));
        setUnsolvedState();
    }

    public void setPuzzlesSolvedCount(int solvedCount, int totalCount) {
        puzzlesSolvedTextView.setText(activity.getString(R.string.puzzles_solved, solvedCount, totalCount));
        puzzlesSolvedTextView.setTypeface(null, Typeface.BOLD);
        puzzlesSolvedTextView.setContentDescription(activity.getString(R.string.content_desc_puzzles_solved, solvedCount, totalCount));
    }

    public void setPuzzleSolved(boolean solved) {
        if (solved) {
            applyTextViewStyle(puzzleIdEditText, Color.GREEN, SOLVED_ALPHA);
            puzzleIdEditText.setContentDescription(activity.getString(R.string.content_desc_puzzle_solved));
        } else {
            setUnsolvedState();
            puzzleIdEditText.setContentDescription(activity.getString(R.string.content_desc_puzzle_not_solved));
        }
        if (!isNull(solvedIconView)) {
            solvedIconView.setVisibility(solved ? View.VISIBLE : View.GONE);
        }
    }

    public void setPlayerRating(int playerRating) {
        playerRatingTextView.setText(activity.getString(R.string.player_rating, playerRating));
        playerRatingTextView.setTypeface(null, Typeface.BOLD);
        playerRatingTextView.setContentDescription(activity.getString(R.string.content_desc_player_rating, playerRating));
    }

    public void updatePlayerRating(int oldRating, int newRating) {
        if (cleanedUp) {
            return;
        }
        cancelCurrentAnimations();
        startRatingAnimation(oldRating, newRating);
        startAlphaAnimation(newRating);
    }

    public void cleanup() {
        cancelCurrentAnimations();
        // Drop animator update/listener callbacks — they capture the Activity-owned
        // playerRatingTextView via the lambdas, leaking the Activity until the animators
        // are GC'd otherwise.
        if (!isNull(currentRatingAnimator)) {
            currentRatingAnimator.removeAllUpdateListeners();
            currentRatingAnimator.removeAllListeners();
        }
        if (!isNull(currentAlphaAnimator)) {
            currentAlphaAnimator.removeAllUpdateListeners();
            currentAlphaAnimator.removeAllListeners();
        }
        currentRatingAnimator = null;
        currentAlphaAnimator = null;
        cleanedUp = true;
    }

    public boolean isCleanedUp() {
        return cleanedUp;
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
        // Allocated once per animation, not per frame. ForegroundColorSpan's color is final,
        // so we can only reuse the span when the color hasn't changed since the last run.
        if (ratingColorSpan.getForegroundColor() != color) {
            ratingColorSpan = new ForegroundColorSpan(color);
        }
        currentRatingAnimator = ValueAnimator.ofInt(oldRating, newRating);
        currentRatingAnimator.setDuration(ANIMATION_DURATION);
        currentRatingAnimator.addUpdateListener(animation -> {
            int value = (int) animation.getAnimatedValue();
            String fullText = activity.getString(R.string.player_rating, value);
            String ratingString = Integer.toString(value);

            ratingTextBuffer.clear();
            ratingTextBuffer.clearSpans();
            ratingTextBuffer.append(fullText);

            int start = fullText.indexOf(ratingString);
            if (start >= 0) {
                int end = start + ratingString.length();
                ratingTextBuffer.setSpan(ratingColorSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            playerRatingTextView.setText(ratingTextBuffer);
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
        if (!isNull(currentRatingAnimator) && currentRatingAnimator.isRunning()) {
            currentRatingAnimator.cancel();
        }
        if (!isNull(currentAlphaAnimator) && currentAlphaAnimator.isRunning()) {
            currentAlphaAnimator.cancel();
        }
    }
}
