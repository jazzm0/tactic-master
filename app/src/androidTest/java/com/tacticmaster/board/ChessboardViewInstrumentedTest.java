package com.tacticmaster.board;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.tacticmaster.MainActivity;
import com.tacticmaster.R;
import com.tacticmaster.puzzle.PuzzleGame;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(AndroidJUnit4.class)
public class ChessboardViewInstrumentedTest {

    public static class MockViewTest extends ImageView {
        public MockViewTest(Context context) {
            super(context);
        }
    }

    @Rule
    final public ActivityScenarioRule<MainActivity> activityScenarioRule = new ActivityScenarioRule<>(MainActivity.class);

    private Context context;
    private ChessboardView chessboardView;
    private PuzzleGame puzzle;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        puzzle = new PuzzleGame("1", "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", "e2e4 e7e5", 1049);

        activityScenarioRule.getScenario().onActivity(activity -> {
            PuzzleHintView mockPuzzleHintView = new PuzzleHintView(context, null);
            chessboardView = new ChessboardView(context, null);
            chessboardView.setPuzzleHintView(mockPuzzleHintView);
            chessboardView.setPlayerTurnIcon(new MockViewTest(context));
            chessboardView.setPuzzle(puzzle);
            activity.setContentView(chessboardView);
        });
    }

    @Test
    public void testInitialization() {
        assertNotNull(chessboardView);
    }

    @Test
    public void testPieceBitmaps() {
        Bitmap whiteKing = BitmapFactory.decodeResource(context.getResources(), R.drawable.wk);
        Bitmap blackKing = BitmapFactory.decodeResource(context.getResources(), R.drawable.bk);
        assertNotNull(whiteKing);
        assertNotNull(blackKing);
    }

    @Test
    public void testOnTouchEventFirstMoveMade() {
        activityScenarioRule.getScenario().onActivity(activity -> {
            assertEquals(-1, chessboardView.getSelectedFromFile());
            assertEquals(-1, chessboardView.getSelectedFromRank());
            chessboardView.doFirstMove();
            MotionEvent event = MotionEvent.obtain(100, 100, MotionEvent.ACTION_DOWN, 840, 840, 0);
            boolean result = chessboardView.onTouchEvent(event);
            assertTrue(result);
            assertEquals(6, chessboardView.getSelectedFromFile());
            assertEquals(6, chessboardView.getSelectedFromRank());
        });
    }

    @Test
    public void testOnTouchEventFirstMoveNotMade() {
        activityScenarioRule.getScenario().onActivity(activity -> {
            assertEquals(-1, chessboardView.getSelectedFromFile());
            assertEquals(-1, chessboardView.getSelectedFromRank());
            MotionEvent event = MotionEvent.obtain(100, 100, MotionEvent.ACTION_DOWN, 840, 840, 0);
            boolean result = chessboardView.onTouchEvent(event);
            assertTrue(result);
            assertEquals(-1, chessboardView.getSelectedFromFile());
            assertEquals(-1, chessboardView.getSelectedFromRank());
        });
    }

    @Test
    public void testPerformClick() {
        activityScenarioRule.getScenario().onActivity(activity -> {
            boolean result = chessboardView.performClick();
            assertFalse(result);
        });
    }

    @Test
    public void testHintClickBehavior() {
        activityScenarioRule.getScenario().onActivity(activity -> {
            PuzzleHintView mockPuzzleHintView = new PuzzleHintView(context, null);
            chessboardView.setPuzzleHintView(mockPuzzleHintView);

            chessboardView.doFirstMove();
            chessboardView.puzzleHintClicked();
            assertEquals(6, mockPuzzleHintView.getHintMoveRank());
            assertEquals(3, mockPuzzleHintView.getHintMoveFile());

            chessboardView.puzzleHintClicked();
            assertEquals(View.VISIBLE, mockPuzzleHintView.getVisibility());
        });
    }
}