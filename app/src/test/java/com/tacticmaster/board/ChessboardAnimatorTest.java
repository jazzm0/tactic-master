package com.tacticmaster.board;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.graphics.Bitmap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link ChessboardAnimator}.
 * <p>
 * {@link android.animation.ValueAnimator} cannot run on the plain JVM, so
 * {@link ChessboardAnimator} accepts an {@link ChessboardAnimator.AnimatorStarter}
 * that tests replace with a controllable fake. Two fakes cover the two paths:
 * - immediateStarter: fires onAnimationEnd synchronously (simulates completed animation)
 * - noopStarter: never fires (simulates an in-flight animation)
 */
public class ChessboardAnimatorTest {

    private Chessboard mockChessboard;
    private ChessboardPieceManager mockPieceManager;
    private ChessboardAnimator.Callbacks mockCallbacks;
    private Bitmap mockBitmap;

    // Starter that fires the end-callback immediately — simulates completed animation.
    private ChessboardAnimator.AnimatorStarter immediateStarter;
    // Starter that never fires — simulates an animation still in flight.
    private ChessboardAnimator.AnimatorStarter noopStarter;

    @BeforeEach
    void setUp() {
        mockChessboard = mock(Chessboard.class);
        mockPieceManager = mock(ChessboardPieceManager.class);
        mockCallbacks = mock(ChessboardAnimator.Callbacks.class);
        mockBitmap = mock(Bitmap.class);

        when(mockChessboard.transformFenMove(any())).thenReturn(new int[]{1, 0, 3, 0});
        when(mockChessboard.getPiece(anyInt(), anyInt())).thenReturn('P');
        when(mockChessboard.isCaptureMove(any())).thenReturn(false);
        when(mockChessboard.isPlayersTurn()).thenReturn(true);
        when(mockPieceManager.getPieceBitmap(any(Character.class))).thenReturn(mockBitmap);

        immediateStarter = (duration, onFrame, onEnd) -> onEnd.run();
        noopStarter = (duration, onFrame, onEnd) -> {
        };
    }

    private ChessboardAnimator animator(ChessboardAnimator.AnimatorStarter starter, int duration) {
        return new ChessboardAnimator(mockChessboard, mockPieceManager, mockCallbacks, duration, starter);
    }

    // --- skip-animation path ---

    @Test
    void whenDurationIsZero_skipsAnimationAndCompletesImmediately() {
        var anim = animator(noopStarter, 0);
        anim.startMove("e2e4");

        assertFalse(anim.isAnimating());
        verify(mockChessboard).doMove("e2e4");
        verify(mockCallbacks).onMoveCompleted(eq("e2e4"), eq(false), eq(true), any());
    }

    @Test
    void whenDurationIsZero_animPieceBitmapIsNull() {
        var anim = animator(noopStarter, 0);
        anim.startMove("e2e4");

        assertNull(anim.getAnimPieceBitmap());
    }

    // --- normal animation path ---

    @Test
    void whenDurationIsPositive_setsIsAnimatingTrue() {
        var anim = animator(noopStarter, 300);
        anim.startMove("e2e4");

        assertTrue(anim.isAnimating());
    }

    @Test
    void whenDurationIsPositive_storesPieceBitmapForDrawing() {
        var anim = animator(noopStarter, 300);
        anim.startMove("e2e4");

        assertNotNull(anim.getAnimPieceBitmap());
    }

    @Test
    void whenDurationIsPositive_storesFromAndToCoords() {
        when(mockChessboard.transformFenMove("e2e4")).thenReturn(new int[]{6, 4, 4, 4});
        var anim = animator(noopStarter, 300);
        anim.startMove("e2e4");

        assertEquals(6, anim.getAnimFromRank());
        assertEquals(4, anim.getAnimFromFile());
        assertEquals(4, anim.getAnimToRank());
        assertEquals(4, anim.getAnimToFile());
    }

    @Test
    void whenAnimationEnds_clearsIsAnimating() {
        var anim = animator(immediateStarter, 300);
        anim.startMove("e2e4");

        assertFalse(anim.isAnimating());
    }

    @Test
    void whenAnimationEnds_callsDoMoveOnChessboard() {
        var anim = animator(immediateStarter, 300);
        anim.startMove("e2e4");

        verify(mockChessboard).doMove("e2e4");
    }

    @Test
    void whenAnimationEnds_firesOnMoveCompletedCallback() {
        var anim = animator(immediateStarter, 300);
        anim.startMove("e2e4");

        verify(mockCallbacks).onMoveCompleted(eq("e2e4"), eq(false), eq(true), any());
    }

    @Test
    void whenAnimationEnds_animPieceBitmapIsNull() {
        var anim = animator(immediateStarter, 300);
        anim.startMove("e2e4");

        assertNull(anim.getAnimPieceBitmap());
    }

    // --- isAnimating guard ---

    @Test
    void whenAlreadyAnimating_startMoveIsIgnored() {
        var anim = animator(noopStarter, 300);
        anim.startMove("e2e4");
        anim.startMove("d2d4");

        // doMove should never have been called since neither animation completed.
        verify(mockChessboard, never()).doMove(any());
        // coords unchanged from first move
        assertEquals(1, anim.getAnimFromRank());
    }

    // --- capture move ---

    @Test
    void whenMoveIsCapture_callbackReceivesCaptureTrue() {
        when(mockChessboard.isCaptureMove("e2e4")).thenReturn(true);
        var anim = animator(immediateStarter, 300);
        anim.startMove("e2e4");

        verify(mockCallbacks).onMoveCompleted(eq("e2e4"), eq(true), eq(true), any());
    }

    // --- opponent turn after move ---

    @Test
    void whenNotPlayersTurnAfterMove_callbackReceivesIsPlayersTurnFalse() {
        when(mockChessboard.isPlayersTurn()).thenReturn(false);
        var anim = animator(immediateStarter, 300);
        anim.startMove("e2e4");

        verify(mockCallbacks).onMoveCompleted(eq("e2e4"), eq(false), eq(false), any());
    }
}
