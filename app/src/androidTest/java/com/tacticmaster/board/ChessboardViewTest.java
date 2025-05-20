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
import com.tacticmaster.puzzle.Puzzle;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(AndroidJUnit4.class)
public class ChessboardViewTest {

    public static class MockViewTest extends ImageView {
        public MockViewTest(Context context) {
            super(context);
        }
    }

    @Rule
    final public ActivityScenarioRule<MainActivity> activityScenarioRule = new ActivityScenarioRule<>(MainActivity.class);

    private Context context;
    private ChessboardView chessboardView;
    private Puzzle puzzle;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        puzzle = new Puzzle("1", "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", "e2e4 e7e5", 1049);

        activityScenarioRule.getScenario().onActivity(activity -> {
            chessboardView = new ChessboardView(context, null);
            chessboardView.setPuzzle(puzzle);
            chessboardView.setPlayerTurnIcon(new MockViewTest(context));
            activity.setContentView(chessboardView);
        });
    }

    @Test
    public void testInitialization() {
        assertNotNull(chessboardView);
        assertNotNull(chessboardView.getPuzzle());
        assertNotNull(chessboardView.getChessboard());
    }

    @Test
    public void testPieceBitmaps() {
        Bitmap whiteKing = BitmapFactory.decodeResource(context.getResources(), R.drawable.wk);
        Bitmap blackKing = BitmapFactory.decodeResource(context.getResources(), R.drawable.bk);
        assertNotNull(whiteKing);
        assertNotNull(blackKing);
    }

    @Test
    public void testOnTouchEvent() {
        activityScenarioRule.getScenario().onActivity(activity -> {
            assertEquals(-1, chessboardView.getSelectedCol());
            assertEquals(-1, chessboardView.getSelectedRow());
            MotionEvent event = MotionEvent.obtain(100, 100, MotionEvent.ACTION_DOWN, 900, 935, 0);
            boolean result = chessboardView.onTouchEvent(event);
            assertTrue(result);
            assertEquals(6, chessboardView.getSelectedCol());
            assertEquals(6, chessboardView.getSelectedRow());
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
    public void testSetPuzzle() {
        Puzzle newPuzzle = new Puzzle("2", "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", "e2e4 e7e5", 1049);
        activityScenarioRule.getScenario().onActivity(activity -> {
            chessboardView.setPuzzle(newPuzzle);
            assertEquals(newPuzzle, chessboardView.getPuzzle());
        });
    }

    @Test
    public void testHintClickBehavior() {
        activityScenarioRule.getScenario().onActivity(activity -> {
            HintPathView mockHintPathView = new HintPathView(context, null);
            chessboardView.setHintPathView(mockHintPathView);

            chessboardView.getChessboard().makeFirstMove();
            chessboardView.puzzleHintClicked();
            assertEquals(6, chessboardView.getHintMoveRow());
            assertEquals(3, chessboardView.getHintMoveColumn());

            chessboardView.puzzleHintClicked();
            assertEquals(View.VISIBLE, mockHintPathView.getVisibility());
        });
    }
}