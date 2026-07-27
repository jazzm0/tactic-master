package com.tacticmaster.board;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;

import androidx.appcompat.content.res.AppCompatResources;

import com.tacticmaster.R;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Field;

/**
 * Unit tests for {@link PuzzleResultOverlay}. The overlay's animation is driven
 * by a {@link android.animation.ValueAnimator} (which needs a Looper) and its
 * icon rendering uses {@link Canvas}/{@link Bitmap}/{@link AppCompatResources},
 * none of which run on the plain JVM. So the Android graphics factories and
 * constructors are mocked here, and {@code showing}/{@code animProgress} are set
 * directly via reflection to exercise the deterministic draw + fade logic
 * without a real animator. The animator start path itself is covered by the
 * instrumented test.
 */
public class PuzzleResultOverlayTest {

    private Context mockContext;
    private Bitmap mockBitmap;

    private MockedStatic<Bitmap> bitmapStatic;
    private MockedStatic<AppCompatResources> resourcesStatic;

    private PuzzleResultOverlay overlay;

    @BeforeEach
    public void setUp() {
        mockContext = mock(Context.class);
        Runnable invalidate = mock(Runnable.class);
        Drawable mockDrawable = mock(Drawable.class);
        mockBitmap = mock(Bitmap.class);
        when(mockBitmap.getWidth()).thenReturn(50);
        when(mockBitmap.getHeight()).thenReturn(50);
        // mutate() returns the same drawable so tinting/bounds are set on it.
        when(mockDrawable.mutate()).thenReturn(mockDrawable);

        bitmapStatic = Mockito.mockStatic(Bitmap.class);
        bitmapStatic.when(() -> Bitmap.createBitmap(anyInt(), anyInt(), any()))
                .thenReturn(mockBitmap);

        resourcesStatic = Mockito.mockStatic(AppCompatResources.class);
        resourcesStatic.when(() -> AppCompatResources.getDrawable(any(), anyInt()))
                .thenReturn(mockDrawable);

        overlay = new PuzzleResultOverlay(mockContext, invalidate);
    }

    @AfterEach
    public void tearDown() {
        bitmapStatic.close();
        resourcesStatic.close();
    }

    /**
     * Sets the private animation state so draw() can be tested deterministically.
     */
    private void setState(boolean showing, boolean solved, float progress) {
        try {
            Field showingField = PuzzleResultOverlay.class.getDeclaredField("showing");
            Field solvedField = PuzzleResultOverlay.class.getDeclaredField("solved");
            Field progressField = PuzzleResultOverlay.class.getDeclaredField("animProgress");
            showingField.setAccessible(true);
            solvedField.setAccessible(true);
            progressField.setAccessible(true);
            showingField.setBoolean(overlay, showing);
            solvedField.setBoolean(overlay, solved);
            progressField.setFloat(overlay, progress);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isShowing() {
        try {
            Field showingField = PuzzleResultOverlay.class.getDeclaredField("showing");
            showingField.setAccessible(true);
            return showingField.getBoolean(overlay);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testDraw_WhenNotShowing_DrawsNothing() {
        Canvas canvas = mock(Canvas.class);

        overlay.draw(canvas, 800f);

        verify(canvas, never()).drawRect(anyFloat(), anyFloat(), anyFloat(), anyFloat(), any());
        verify(canvas, never()).drawBitmap(any(Bitmap.class), anyFloat(), anyFloat(), any());
    }

    @Test
    public void testDraw_WhenShowing_DrawsTintAndIcon() {
        Canvas canvas = mock(Canvas.class);
        setState(true, true, 0.5f);

        try (MockedConstruction<PorterDuffColorFilter> ignored =
                     Mockito.mockConstruction(PorterDuffColorFilter.class)) {
            overlay.draw(canvas, 800f);
        }

        // Full-board tint rect at the board bounds.
        verify(canvas).drawRect(eq(0f), eq(0f), eq(800f), eq(800f), any(Paint.class));
        // Centered icon.
        verify(canvas).drawBitmap(eq(mockBitmap), anyFloat(), anyFloat(), any(Paint.class));
    }

    @Test
    public void testDraw_Solved_UsesSolvedDrawable() {
        Canvas canvas = mock(Canvas.class);
        setState(true, true, 0.5f);

        try (MockedConstruction<PorterDuffColorFilter> ignored =
                     Mockito.mockConstruction(PorterDuffColorFilter.class)) {
            overlay.draw(canvas, 800f);
        }

        resourcesStatic.verify(() -> AppCompatResources.getDrawable(mockContext, R.drawable.ic_solved));
    }

    @Test
    public void testDraw_Unsolved_UsesUnsolvedDrawable() {
        Canvas canvas = mock(Canvas.class);
        setState(true, false, 0.5f);

        try (MockedConstruction<PorterDuffColorFilter> ignored =
                     Mockito.mockConstruction(PorterDuffColorFilter.class)) {
            overlay.draw(canvas, 800f);
        }

        resourcesStatic.verify(() -> AppCompatResources.getDrawable(mockContext, R.drawable.ic_unsolved));
    }

    @Test
    public void testDraw_CachesIcon_RendersOncePerSize() {
        Canvas canvas = mock(Canvas.class);
        setState(true, true, 0.5f);

        try (MockedConstruction<PorterDuffColorFilter> ignored =
                     Mockito.mockConstruction(PorterDuffColorFilter.class)) {
            overlay.draw(canvas, 800f);
            overlay.draw(canvas, 800f);
        }

        // Second draw at the same size reuses the cached bitmap.
        resourcesStatic.verify(
                () -> AppCompatResources.getDrawable(mockContext, R.drawable.ic_solved),
                times(1));
        bitmapStatic.verify(() -> Bitmap.createBitmap(anyInt(), anyInt(), any()), times(1));
    }

    @Test
    public void testDraw_ReRendersIcon_WhenBoardSizeChanges() {
        Canvas canvas = mock(Canvas.class);
        setState(true, true, 0.5f);

        try (MockedConstruction<PorterDuffColorFilter> ignored =
                     Mockito.mockConstruction(PorterDuffColorFilter.class)) {
            overlay.draw(canvas, 800f);
            overlay.draw(canvas, 400f);
        }

        // A different board size -> different icon px -> re-render.
        bitmapStatic.verify(() -> Bitmap.createBitmap(anyInt(), anyInt(), any()), times(2));
    }

    @Test
    public void testOnSizeChanged_RecyclesCachedIcons() {
        Canvas canvas = mock(Canvas.class);
        setState(true, true, 0.5f);

        try (MockedConstruction<PorterDuffColorFilter> ignored =
                     Mockito.mockConstruction(PorterDuffColorFilter.class)) {
            overlay.draw(canvas, 800f);
            overlay.onSizeChanged();
            overlay.draw(canvas, 800f);
        }

        verify(mockBitmap).recycle();
        // Re-rendered after the cache was dropped, even though the size matches.
        bitmapStatic.verify(() -> Bitmap.createBitmap(anyInt(), anyInt(), any()), times(2));
    }

    @Test
    public void testRecycle_RecyclesCachedIcons() {
        Canvas canvas = mock(Canvas.class);
        setState(true, true, 0.5f);

        try (MockedConstruction<PorterDuffColorFilter> ignored =
                     Mockito.mockConstruction(PorterDuffColorFilter.class)) {
            overlay.draw(canvas, 800f);
        }
        overlay.recycle();

        verify(mockBitmap).recycle();
    }

    @Test
    public void testRecycle_SkipsWhenNothingCached() {
        overlay.recycle();

        verify(mockBitmap, never()).recycle();
    }

    @Test
    public void testCancel_HidesOverlay() {
        setState(true, true, 0.5f);

        overlay.cancel();

        org.junit.jupiter.api.Assertions.assertFalse(isShowing());
    }

    @Test
    public void testDraw_AfterCancel_DrawsNothing() {
        Canvas canvas = mock(Canvas.class);
        setState(true, true, 0.5f);

        overlay.cancel();
        overlay.draw(canvas, 800f);

        verify(canvas, never()).drawRect(anyFloat(), anyFloat(), anyFloat(), anyFloat(), any());
    }

    @Test
    public void testFadeFactor_RampsUpHoldsThenRampsDown() {
        // Fade-in window (< 0.18): partial, and monotonically increasing.
        float earlyIn = fadeFactorAt(0.02f);
        float lateIn = fadeFactorAt(0.15f);
        // Hold window: full opacity.
        float hold = fadeFactorAt(0.5f);
        // Fade-out window (> 0.75): partial, and decreasing toward the end.
        float earlyOut = fadeFactorAt(0.8f);
        float lateOut = fadeFactorAt(0.95f);

        org.junit.jupiter.api.Assertions.assertTrue(earlyIn < lateIn, "fade-in should increase");
        org.junit.jupiter.api.Assertions.assertTrue(lateIn < hold, "should reach full at hold");
        org.junit.jupiter.api.Assertions.assertEquals(1f, hold, 1e-6, "hold should be full opacity");
        org.junit.jupiter.api.Assertions.assertTrue(earlyOut < hold, "fade-out should drop below full");
        org.junit.jupiter.api.Assertions.assertTrue(lateOut < earlyOut, "fade-out should decrease");
    }

    @Test
    public void testFadeFactor_EndpointsAreZero() {
        org.junit.jupiter.api.Assertions.assertEquals(0f, fadeFactorAt(0f), 1e-6, "starts fully transparent");
        org.junit.jupiter.api.Assertions.assertEquals(0f, fadeFactorAt(1f), 1e-6, "ends fully transparent");
    }

    private float fadeFactorAt(float progress) {
        setState(true, true, progress);
        return overlay.fadeFactor();
    }
}
