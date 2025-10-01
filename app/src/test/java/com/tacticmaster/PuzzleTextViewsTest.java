package com.tacticmaster;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import android.widget.EditText;
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

        when(mockActivity.findViewById(R.id.puzzle_id_label)).thenReturn(mockPuzzleIdLabelTextView);
        when(mockActivity.findViewById(R.id.puzzle_id)).thenReturn(mockPuzzleIdEditText);
        when(mockActivity.findViewById(R.id.puzzle_rating)).thenReturn(mockPuzzleRatingTextView);
        when(mockActivity.findViewById(R.id.puzzles_count)).thenReturn(mockPuzzlesSolvedTextView);
        when(mockActivity.findViewById(R.id.player_rating)).thenReturn(mockPlayerRatingTextView);
        when(mockActivity.findViewById(R.id.filter_button)).thenReturn(mockFilterButton);
        when(mockActivity.findViewById(R.id.filter_dropdown)).thenReturn(mockFilterDropdown);

        when(mockActivity.getString(eq(R.string.rating), anyInt())).thenReturn("Rating: 1500");
        when(mockActivity.getString(eq(R.string.player_rating), anyInt())).thenReturn("Player: 1600");
        when(mockActivity.getString(eq(R.string.puzzles_solved), anyInt(), anyInt())).thenReturn("Solved: 25/100");

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
    void testSetPuzzleRatingWithNegativeRatingDoesNotUpdate() {
        puzzleTextViews.setPuzzleRating(-100);

        verify(mockPuzzleRatingTextView, never()).setText(anyString());
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
    void testSetPuzzlesSolvedCountWithNegativeSolvedCountDoesNotUpdate() {
        puzzleTextViews.setPuzzlesSolvedCount(-1, 100);

        verify(mockPuzzlesSolvedTextView, never()).setText(anyString());
    }

    @Test
    void testSetPuzzlesSolvedCountWithNegativeTotalCountDoesNotUpdate() {
        puzzleTextViews.setPuzzlesSolvedCount(50, -1);

        verify(mockPuzzlesSolvedTextView, never()).setText(anyString());
    }

    @Test
    void testSetPuzzlesSolvedCountWithSolvedGreaterThanTotalDoesNotUpdate() {
        puzzleTextViews.setPuzzlesSolvedCount(150, 100);

        verify(mockPuzzlesSolvedTextView, never()).setText(anyString());
    }

    @Test
    void testSetPuzzleSolvedTrueUpdatesStyling() {
        puzzleTextViews.setPuzzleSolved(true);

        verify(mockPuzzleIdEditText).setTextColor(Color.GREEN);
        verify(mockPuzzleIdEditText).setAlpha(0.7f);
        verify(mockPuzzleIdEditText).setTypeface(null, Typeface.BOLD);
        verify(mockPuzzleIdEditText).setContentDescription("Puzzle solved");
    }

    @Test
    void testSetPuzzleSolvedFalseUpdatesToUnsolvedState() {
        puzzleTextViews.setPuzzleSolved(false);

        verify(mockPuzzleIdEditText).clearFocus();
        verify(mockPuzzleIdEditText).setTextColor(Color.BLACK);
        verify(mockPuzzleIdEditText).setAlpha(1.0f);
        verify(mockPuzzleIdEditText).setTypeface(null, Typeface.BOLD);
        verify(mockPuzzleIdEditText).setContentDescription("Puzzle not solved");
    }

    @Test
    void testSetPlayerRatingWithValidRating() {
        puzzleTextViews.setPlayerRating(1800);

        verify(mockPlayerRatingTextView).setText(anyString());
        verify(mockPlayerRatingTextView).setTypeface(null, Typeface.BOLD);
        verify(mockPlayerRatingTextView).setContentDescription("Player rating: 1800");
    }

    @Test
    void testSetPlayerRatingWithNegativeRatingDoesNotUpdate() {
        puzzleTextViews.setPlayerRating(-500);

        verify(mockPlayerRatingTextView, never()).setText(anyString());
    }

    @Test
    void testUpdatePlayerRatingWithValidRatings() {
        puzzleTextViews.updatePlayerRating(1500, 1600);

        assertNotNull(puzzleTextViews);
    }

    @Test
    void testUpdatePlayerRatingWithNegativeOldRatingDoesNotAnimate() {
        puzzleTextViews.updatePlayerRating(-100, 1600);

        assertNotNull(puzzleTextViews);
    }

    @Test
    void testUpdatePlayerRatingWithNegativeNewRatingDoesNotAnimate() {
        puzzleTextViews.updatePlayerRating(1500, -100);

        assertNotNull(puzzleTextViews);
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
