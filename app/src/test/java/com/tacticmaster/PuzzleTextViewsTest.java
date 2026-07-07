package com.tacticmaster;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PuzzleTextViewsTest {

    private PuzzleTextViews puzzleTextViews;
    private Activity mockActivity;
    private Context mockContext;
    private TextView mockPuzzleIdLabelTextView;
    private EditText mockPuzzleIdEditText;
    private TextView mockPuzzleRatingTextView;
    private TextView mockPuzzlesSolvedTextView;
    private TextView mockPlayerRatingTextView;
    private MaterialButton mockFilterButton;
    private MaterialAutoCompleteTextView mockFilterDropdown;
    private ImageView mockSolvedIconView;

    @BeforeEach
    void setUp() {
        mockActivity = mock(Activity.class);
        mockContext = mock(Context.class);
        mockPuzzleIdLabelTextView = mock(TextView.class);
        mockPuzzleIdEditText = mock(EditText.class);
        mockPuzzleRatingTextView = mock(TextView.class);
        mockPuzzlesSolvedTextView = mock(TextView.class);
        mockPlayerRatingTextView = mock(TextView.class);
        mockFilterButton = mock(MaterialButton.class);
        mockFilterDropdown = mock(MaterialAutoCompleteTextView.class);
        mockSolvedIconView = mock(ImageView.class);

        when(mockActivity.findViewById(R.id.puzzle_id_label)).thenReturn(mockPuzzleIdLabelTextView);
        when(mockActivity.findViewById(R.id.puzzle_id)).thenReturn(mockPuzzleIdEditText);
        when(mockActivity.findViewById(R.id.puzzle_rating)).thenReturn(mockPuzzleRatingTextView);
        when(mockActivity.findViewById(R.id.puzzles_count)).thenReturn(mockPuzzlesSolvedTextView);
        when(mockActivity.findViewById(R.id.player_rating)).thenReturn(mockPlayerRatingTextView);
        when(mockActivity.findViewById(R.id.filter_button)).thenReturn(mockFilterButton);
        when(mockActivity.findViewById(R.id.filter_dropdown)).thenReturn(mockFilterDropdown);
        when(mockActivity.findViewById(R.id.solved)).thenReturn(mockSolvedIconView);

        when(mockActivity.getString(eq(R.string.rating), anyInt())).thenReturn("Rating: 1500");
        when(mockActivity.getString(eq(R.string.player_rating), anyInt())).thenReturn("Player: 1600");
        when(mockActivity.getString(eq(R.string.puzzles_solved), anyInt(), anyInt())).thenReturn("Solved: 25/100");
        when(mockActivity.getString(eq(R.string.content_desc_puzzle_rating), anyInt())).thenReturn("1500 rating");
        when(mockActivity.getString(eq(R.string.content_desc_puzzle_id), anyString())).thenAnswer(inv -> "Puzzle ID: " + inv.getArgument(1));
        when(mockActivity.getString(eq(R.string.content_desc_puzzles_solved), anyInt(), anyInt())).thenReturn("50 out of 100 puzzles solved");
        when(mockActivity.getString(R.string.content_desc_puzzle_solved)).thenReturn("Puzzle solved");
        when(mockActivity.getString(R.string.content_desc_puzzle_not_solved)).thenReturn("Puzzle not solved");
        when(mockActivity.getString(eq(R.string.content_desc_player_rating), anyInt())).thenReturn("Player rating: 1800");

        puzzleTextViews = new PuzzleTextViews(mockActivity);
    }

    @Test
    void testConstructorWithNullContextThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new PuzzleTextViews(null));
    }

    @Test
    void testConstructorWithNonActivityContextThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new PuzzleTextViews(mockContext));
    }

    @Test
    void testConstructorWithMissingViewsThrowsException() {
        when(mockActivity.findViewById(R.id.puzzle_id_label)).thenReturn(null);
        assertThrows(RuntimeException.class, () -> new PuzzleTextViews(mockActivity));
    }

    @Test
    void testGetFilterButtonReturnsCorrectButton() {
        MaterialButton result = puzzleTextViews.getFilterButton();
        assertEquals(mockFilterButton, result);
    }

    @Test
    void testGetFilterDropdownReturnsCorrectDropdown() {
        MaterialAutoCompleteTextView result = puzzleTextViews.getFilterDropdown();
        assertEquals(mockFilterDropdown, result);
    }

    @Test
    void testSetPuzzleRatingWithValidRating() {
        puzzleTextViews.setPuzzleRating(1500);

        verify(mockPuzzleRatingTextView).setText(anyString());
        verify(mockPuzzleRatingTextView).setTypeface(null, Typeface.BOLD);
        verify(mockPuzzleRatingTextView).setContentDescription(anyString());
    }

    @Test
    void testSetPuzzleIdWithValidId() {
        String puzzleId = "ABC123";
        puzzleTextViews.setPuzzleId(puzzleId);

        verify(mockPuzzleIdLabelTextView).setTypeface(null, Typeface.BOLD);
        verify(mockPuzzleIdEditText).setText(puzzleId);
        verify(mockPuzzleIdEditText).setContentDescription("Puzzle ID: " + puzzleId);
    }

    @Test
    void testSetPuzzleIdWithNullIdDoesNotUpdate() {
        puzzleTextViews.setPuzzleId(null);

        verify(mockPuzzleIdEditText, never()).setText(anyString());
    }

    @Test
    void testSetPuzzleIdWithEmptyIdDoesNotUpdate() {
        puzzleTextViews.setPuzzleId("   ");

        verify(mockPuzzleIdEditText, never()).setText(anyString());
    }

    @Test
    void testSetPuzzlesSolvedCountWithValidCounts() {
        puzzleTextViews.setPuzzlesSolvedCount(50, 100);

        verify(mockPuzzlesSolvedTextView).setText(anyString());
        verify(mockPuzzlesSolvedTextView).setTypeface(null, Typeface.BOLD);
        verify(mockPuzzlesSolvedTextView).setContentDescription(anyString());
    }

    @Test
    void testSetPuzzleSolvedTrueUpdatesStyling() {
        puzzleTextViews.setPuzzleSolved(true);

        verify(mockPuzzleIdEditText).setTextColor(Color.GREEN);
        verify(mockPuzzleIdEditText).setAlpha(0.7f);
        verify(mockPuzzleIdEditText).setTypeface(null, Typeface.BOLD);
        verify(mockPuzzleIdEditText).setContentDescription("Puzzle solved");
        verify(mockSolvedIconView).setVisibility(View.VISIBLE);
    }

    @Test
    void testSetPuzzleSolvedFalseUpdatesToUnsolvedState() {
        puzzleTextViews.setPuzzleSolved(false);

        verify(mockPuzzleIdEditText).clearFocus();
        verify(mockPuzzleIdEditText).setTextColor(Color.BLACK);
        verify(mockPuzzleIdEditText).setAlpha(1.0f);
        verify(mockPuzzleIdEditText).setTypeface(null, Typeface.BOLD);
        verify(mockPuzzleIdEditText).setContentDescription("Puzzle not solved");
        verify(mockSolvedIconView).setVisibility(View.GONE);
    }

    @Test
    void testSetPlayerRatingWithValidRating() {
        puzzleTextViews.setPlayerRating(1800);

        verify(mockPlayerRatingTextView).setText(anyString());
        verify(mockPlayerRatingTextView).setTypeface(null, Typeface.BOLD);
        verify(mockPlayerRatingTextView).setContentDescription("Player rating: 1800");
    }

    @Test
    void testCleanupDoesNotThrowException() {
        puzzleTextViews.cleanup();

        assertNotNull(puzzleTextViews);
    }

    @Test
    void testMultipleCleanupCallsAreSafe() {
        puzzleTextViews.cleanup();
        puzzleTextViews.cleanup();

        assertNotNull(puzzleTextViews);
    }

    @Test
    void testCleanupMarksInstanceAsCleanedUp() {
        // Regression: cleanup() must mark the instance so post-destroy callbacks
        // (e.g. an Activity finishing while the alpha animator is still pending)
        // don't reach back into the destroyed Activity via getString().
        assertFalse(puzzleTextViews.isCleanedUp());
        puzzleTextViews.cleanup();
        assertTrue(puzzleTextViews.isCleanedUp());
    }

    @Test
    void testUpdatePlayerRatingIsNoOpAfterCleanup() {
        // Regression: updatePlayerRating used to start animations even after the
        // Activity was destroyed, leading to crashes when the listener fired
        // setPlayerRating -> activity.getString on a finished Activity.
        puzzleTextViews.cleanup();
        puzzleTextViews.updatePlayerRating(1500, 1600);

        // No text update should be triggered post-cleanup.
        verify(mockPlayerRatingTextView, never()).setText(anyString());
    }

    @Test
    void testZeroValuesAreValid() {
        puzzleTextViews.setPuzzleRating(0);
        verify(mockPuzzleRatingTextView).setText(anyString());

        puzzleTextViews.setPlayerRating(0);
        verify(mockPlayerRatingTextView).setText(anyString());

        puzzleTextViews.setPuzzlesSolvedCount(0, 0);
        verify(mockPuzzlesSolvedTextView).setText(anyString());
    }

    @Test
    void testWhitespaceStringValidation() {
        puzzleTextViews.setPuzzleId("  ");
        verify(mockPuzzleIdEditText, never()).setText(anyString());

        puzzleTextViews.setPuzzleId("\t\n");
        verify(mockPuzzleIdEditText, never()).setText(anyString());

        puzzleTextViews.setPuzzleId("  VALID  ");
        verify(mockPuzzleIdEditText, times(1)).setText("  VALID  ");
    }
}
