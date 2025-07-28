package com.tacticmaster;

import static java.util.Objects.isNull;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

/**
 * Manages the text views and UI elements related to puzzle information display.
 * This class handles the visual representation of puzzle data, player ratings,
 * and puzzle statistics with animations and styling.
 */
public class PuzzleTextViews {

    private static final String TAG = "PuzzleTextViews";

    // Animation constants
    private static final int ANIMATION_DURATION = 1400;
    private static final float ALPHA_START = 0.4f;
    private static final float ALPHA_END = 0.9f;
    private static final int ALPHA_ANIMATION_MULTIPLIER = 3;

    // Visual constants
    private static final float SOLVED_ALPHA = 0.7f;
    private static final float UNSOLVED_ALPHA = 1.0f;

    // Color constants
    private static final int COLOR_SOLVED = Color.GREEN;
    private static final int COLOR_UNSOLVED = Color.BLACK;
    private static final int COLOR_RATING_INCREASE = Color.GREEN;
    private static final int COLOR_RATING_DECREASE = Color.RED;
    private static final int COLOR_RATING_UNCHANGED = Color.BLACK;

    private final TextView puzzleIdLabelTextView;
    private final EditText puzzleIdEditText;
    private final TextView puzzleIdLinkTextView;
    private final TextView puzzleRatingTextView;
    private final TextView puzzlesSolvedTextView;
    private final TextView playerRatingTextView;
    private final Context context;

    // Animation management
    private ValueAnimator currentRatingAnimator;
    private ValueAnimator currentAlphaAnimator;

    /**
     * Creates a new PuzzleTextViews instance and initializes all text view references.
     *
     * @param context The context used to access resources and find views
     * @throws IllegalArgumentException if context is null or not an Activity
     */
    public PuzzleTextViews(@NonNull Context context) {
        if (isNull(context)) {
            throw new IllegalArgumentException("Context cannot be null");
        }

        if (!(context instanceof Activity)) {
            throw new IllegalArgumentException("Context must be an Activity instance");
        }

        this.context = context;

        try {
            this.puzzleIdLabelTextView = findViewById(R.id.puzzle_id_label);
            this.puzzleIdEditText = findViewById(R.id.puzzle_id);
            this.puzzleIdLinkTextView = findViewById(R.id.puzzle_id_link);
            this.puzzleRatingTextView = findViewById(R.id.puzzle_rating);
            this.puzzlesSolvedTextView = findViewById(R.id.puzzles_count);
            this.playerRatingTextView = findViewById(R.id.player_rating);

            validateViewReferences();
        } catch (Exception e) {
            Log.e(TAG, "Error initializing PuzzleTextViews", e);
            throw new RuntimeException("Failed to initialize PuzzleTextViews", e);
        }
    }

    /**
     * Safely finds a view by ID with proper error handling.
     *
     * @param id  The resource ID of the view to find
     * @param <T> The type of view to return
     * @return The found view, or null if not found
     */
    @Nullable
    private <T extends android.view.View> T findViewById(int id) {
        try {
            return ((Activity) context).findViewById(id);
        } catch (Exception e) {
            Log.e(TAG, "Error finding view with ID: " + id, e);
            return null;
        }
    }

    /**
     * Validates that all required view references are not null.
     *
     * @throws RuntimeException if any required view is null
     */
    private void validateViewReferences() {
        if (isNull(puzzleIdLabelTextView) || isNull(puzzleIdEditText) ||
                isNull(puzzleIdLinkTextView) || isNull(puzzleRatingTextView) ||
                isNull(puzzlesSolvedTextView) || isNull(playerRatingTextView)) {
            throw new RuntimeException("One or more required views could not be found");
        }
    }

    /**
     * Applies consistent styling to a text view.
     *
     * @param textView The text view to style
     * @param color    The text color
     * @param alpha    The alpha value (0.0 to 1.0)
     * @param typeface The typeface style
     */
    private void applyTextViewStyle(@NonNull TextView textView, int color, float alpha, int typeface) {
        if (isNull(textView)) {
            Log.w(TAG, "Attempted to style null TextView");
            return;
        }

        try {
            textView.setTextColor(color);
            textView.setAlpha(alpha);
            textView.setTypeface(null, typeface);
        } catch (Exception e) {
            Log.e(TAG, "Error applying text view style", e);
        }
    }

    /**
     * Sets the puzzle ID text field to unsolved state.
     */
    private void setUnsolvedState() {
        if (isNull(puzzleIdEditText)) {
            Log.w(TAG, "puzzleIdEditText is null in setUnsolvedState");
            return;
        }

        try {
            puzzleIdEditText.clearFocus();
            applyTextViewStyle(puzzleIdEditText, COLOR_UNSOLVED, UNSOLVED_ALPHA, Typeface.BOLD);
        } catch (Exception e) {
            Log.e(TAG, "Error setting unsolved state", e);
        }
    }

    /**
     * Gets the filter button for puzzle theme selection.
     *
     * @return The filter button, or null if not found
     */
    @Nullable
    public MaterialButton getFilterButton() {
        return findViewById(R.id.filter_button);
    }

    /**
     * Gets the filter dropdown for puzzle theme selection.
     *
     * @return The filter dropdown, or null if not found
     */
    @Nullable
    public MaterialAutoCompleteTextView getFilterDropdown() {
        return findViewById(R.id.filter_dropdown);
    }

    /**
     * Sets the puzzle rating display.
     *
     * @param rating The puzzle rating to display
     */
    public void setPuzzleRating(int rating) {
        if (isNull(puzzleRatingTextView)) {
            Log.w(TAG, "puzzleRatingTextView is null in setPuzzleRating");
            return;
        }

        if (rating < 0) {
            Log.w(TAG, "Invalid rating provided: " + rating);
            return;
        }

        try {
            puzzleRatingTextView.setText(context.getString(R.string.rating, rating));
            puzzleRatingTextView.setTypeface(null, Typeface.BOLD);

            // Add content description for accessibility
            puzzleRatingTextView.setContentDescription(
                    context.getString(R.string.rating, rating) + " rating"
            );
        } catch (Exception e) {
            Log.e(TAG, "Error setting puzzle rating", e);
        }
    }

    /**
     * Sets the puzzle ID and updates related UI elements.
     *
     * @param puzzleId The puzzle ID to display
     */
    public void setPuzzleId(@Nullable String puzzleId) {
        if (isNull(puzzleId) || puzzleId.trim().isEmpty()) {
            Log.w(TAG, "Invalid puzzle ID provided");
            return;
        }

        try {
            if (!isNull(puzzleIdLabelTextView)) {
                puzzleIdLabelTextView.setTypeface(null, Typeface.BOLD);
            }

            if (!isNull(puzzleIdEditText)) {
                puzzleIdEditText.setText(puzzleId);
                puzzleIdEditText.setContentDescription("Puzzle ID: " + puzzleId);
            }

            setUnsolvedState();

            if (!isNull(puzzleIdLinkTextView)) {
                puzzleIdLinkTextView.setTypeface(null, Typeface.BOLD);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting puzzle ID", e);
        }
    }

    /**
     * Sets the puzzles solved count display.
     *
     * @param solvedCount The number of solved puzzles
     * @param totalCount  The total number of puzzles
     */
    public void setPuzzlesSolvedCount(int solvedCount, int totalCount) {
        if (isNull(puzzlesSolvedTextView)) {
            Log.w(TAG, "puzzlesSolvedTextView is null in setPuzzlesSolvedCount");
            return;
        }

        if (solvedCount < 0 || totalCount < 0 || solvedCount > totalCount) {
            Log.w(TAG, "Invalid puzzle counts: solved=" + solvedCount + ", total=" + totalCount);
            return;
        }

        try {
            puzzlesSolvedTextView.setText(context.getString(R.string.puzzles_solved, solvedCount, totalCount));
            puzzlesSolvedTextView.setTypeface(null, Typeface.BOLD);

            // Add content description for accessibility
            puzzlesSolvedTextView.setContentDescription(
                    solvedCount + " out of " + totalCount + " puzzles solved"
            );
        } catch (Exception e) {
            Log.e(TAG, "Error setting puzzles solved count", e);
        }
    }

    /**
     * Sets the visual state of the puzzle based on whether it's solved.
     *
     * @param solved True if the puzzle is solved, false otherwise
     */
    public void setPuzzleSolved(boolean solved) {
        if (puzzleIdEditText == null) {
            Log.w(TAG, "puzzleIdEditText is null in setPuzzleSolved");
            return;
        }

        try {
            if (solved) {
                applyTextViewStyle(puzzleIdEditText, COLOR_SOLVED, SOLVED_ALPHA, Typeface.BOLD);
                puzzleIdEditText.setContentDescription("Puzzle solved");
            } else {
                setUnsolvedState();
                puzzleIdEditText.setContentDescription("Puzzle not solved");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting puzzle solved state", e);
        }
    }

    /**
     * Sets the player rating display.
     *
     * @param playerRating The player's current rating
     */
    public void setPlayerRating(int playerRating) {
        if (playerRatingTextView == null) {
            Log.w(TAG, "playerRatingTextView is null in setPlayerRating");
            return;
        }

        if (playerRating < 0) {
            Log.w(TAG, "Invalid player rating: " + playerRating);
            return;
        }

        try {
            playerRatingTextView.setText(context.getString(R.string.player_rating, playerRating));
            playerRatingTextView.setTypeface(null, Typeface.BOLD);

            // Add content description for accessibility
            playerRatingTextView.setContentDescription("Player rating: " + playerRating);
        } catch (Exception e) {
            Log.e(TAG, "Error setting player rating", e);
        }
    }

    /**
     * Animates the player rating change from old to new value.
     *
     * @param oldRating The previous rating
     * @param newRating The new rating
     */
    public void updatePlayerRating(int oldRating, int newRating) {
        if (playerRatingTextView == null) {
            Log.w(TAG, "playerRatingTextView is null in updatePlayerRating");
            return;
        }

        if (oldRating < 0 || newRating < 0) {
            Log.w(TAG, "Invalid ratings: old=" + oldRating + ", new=" + newRating);
            return;
        }

        // Cancel any existing animations
        cancelCurrentAnimations();

        try {
            createRatingAnimation(oldRating, newRating);
            createAlphaAnimation(newRating);
        } catch (Exception e) {
            Log.e(TAG, "Error updating player rating animation", e);
            // Fallback to direct update
            setPlayerRating(newRating);
        }
    }

    /**
     * Creates and starts the rating value animation.
     *
     * @param oldRating The starting rating value
     * @param newRating The ending rating value
     */
    private void createRatingAnimation(int oldRating, int newRating) {
        currentRatingAnimator = ValueAnimator.ofInt(oldRating, newRating);
        currentRatingAnimator.setDuration(ANIMATION_DURATION);
        currentRatingAnimator.addUpdateListener(animation -> {
            try {
                int animatedValue = (int) animation.getAnimatedValue();
                String fullText = context.getString(R.string.player_rating, animatedValue);

                SpannableString spannable = new SpannableString(fullText);
                String ratingString = String.valueOf(animatedValue);
                int start = fullText.indexOf(ratingString);

                if (start >= 0) {
                    int end = start + ratingString.length();
                    int color = getRatingChangeColor(oldRating, newRating);
                    spannable.setSpan(new ForegroundColorSpan(color), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                playerRatingTextView.setText(spannable);
            } catch (Exception e) {
                Log.e(TAG, "Error in rating animation update", e);
            }
        });

        currentRatingAnimator.start();
    }

    /**
     * Creates and starts the alpha animation for visual feedback.
     *
     * @param newRating The new rating value for content description
     */
    private void createAlphaAnimation(int newRating) {
        currentAlphaAnimator = ValueAnimator.ofFloat(ALPHA_START, ALPHA_END);
        currentAlphaAnimator.setDuration(ANIMATION_DURATION * ALPHA_ANIMATION_MULTIPLIER);
        currentAlphaAnimator.addUpdateListener(animation -> {
            try {
                float alphaValue = (float) animation.getAnimatedValue();
                playerRatingTextView.setAlpha(alphaValue);
            } catch (Exception e) {
                Log.e(TAG, "Error in alpha animation update", e);
            }
        });

        currentAlphaAnimator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                try {
                    setPlayerRating(newRating);
                } catch (Exception e) {
                    Log.e(TAG, "Error in animation end callback", e);
                }
            }
        });

        currentAlphaAnimator.start();
    }

    /**
     * Determines the color for rating change based on increase/decrease.
     *
     * @param oldRating The previous rating
     * @param newRating The new rating
     * @return The appropriate color for the rating change
     */
    private int getRatingChangeColor(int oldRating, int newRating) {
        if (newRating > oldRating) {
            return COLOR_RATING_INCREASE;
        } else if (newRating < oldRating) {
            return COLOR_RATING_DECREASE;
        } else {
            return COLOR_RATING_UNCHANGED;
        }
    }

    /**
     * Cancels any currently running animations to prevent conflicts.
     */
    private void cancelCurrentAnimations() {
        if (currentRatingAnimator != null && currentRatingAnimator.isRunning()) {
            currentRatingAnimator.cancel();
        }

        if (currentAlphaAnimator != null && currentAlphaAnimator.isRunning()) {
            currentAlphaAnimator.cancel();
        }
    }

    /**
     * Cleans up resources and cancels any running animations.
     * Should be called when the view is no longer needed.
     */
    public void cleanup() {
        try {
            cancelCurrentAnimations();
            currentRatingAnimator = null;
            currentAlphaAnimator = null;
        } catch (Exception e) {
            Log.e(TAG, "Error during cleanup", e);
        }
    }
}
