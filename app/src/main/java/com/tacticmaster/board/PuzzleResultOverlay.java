package com.tacticmaster.board;

import static java.util.Objects.isNull;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.view.animation.LinearInterpolator;

import androidx.annotation.DrawableRes;
import androidx.appcompat.content.res.AppCompatResources;

import com.tacticmaster.R;

/**
 * Draws the solved/unsolved result feedback over the chessboard: a translucent
 * board tint plus a centered check (solved) or X (unsolved) icon that fades in,
 * holds, then fades out — all within {@link #ANIM_DURATION} so it clears before
 * the next puzzle loads.
 *
 * <p> * {@link ChessboardView} holds one instance, calls {@link #draw(Canvas, float)}
 * from {@code onDraw}, and drives it via {@link #show(boolean)} /
 * {@link #cancel()} / {@link #onSizeChanged()} / {@link #recycle()}. Redraws are
 * requested through the {@code invalidate} callback passed to the constructor.
 */
class PuzzleResultOverlay {

    private static final int ANIM_DURATION = 1600;
    private static final float FADE_IN_END = 0.18f;
    private static final float FADE_OUT_START = 0.75f;
    private static final int TINT_MAX_ALPHA = 90;
    private static final int ICON_MAX_ALPHA = 255;
    private static final float ICON_BOARD_FRACTION = 0.5f;
    private static final int SOLVED_COLOR = 0xFF4CAF50;
    private static final int UNSOLVED_COLOR = 0xFFE53935;

    private final Context context;
    private final Runnable invalidate;
    private final Paint solvedTintPaint;
    private final Paint unsolvedTintPaint;
    private final Paint iconPaint;

    private boolean showing = false;
    private boolean solved = false;
    private float animProgress = 0f;
    private ValueAnimator animator;

    // Tinted icon bitmaps, rendered lazily and re-rendered on size change so
    // draw() stays allocation-free. Recycled via recycle().
    private Bitmap solvedIconBitmap;
    private Bitmap unsolvedIconBitmap;
    private int iconSize = -1;

    PuzzleResultOverlay(Context context, Runnable invalidate) {
        this.context = context;
        this.invalidate = invalidate;
        this.solvedTintPaint = createTintPaint(SOLVED_COLOR);
        this.unsolvedTintPaint = createTintPaint(UNSOLVED_COLOR);
        this.iconPaint = createIconPaint();
    }

    private static Paint createTintPaint(int color) {
        Paint paint = new Paint();
        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);
        return paint;
    }

    private static Paint createIconPaint() {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setDither(true);
        return paint;
    }

    /**
     * Starts the feedback animation for a solved or unsolved result. Cancels any
     * in-flight animation first so results never overlap.
     */
    void show(boolean solved) {
        this.solved = solved;
        this.showing = true;
        this.animProgress = 0f;

        if (!isNull(animator)) {
            animator.cancel();
        }
        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(ANIM_DURATION);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(a -> {
            animProgress = (float) a.getAnimatedValue();
            invalidate.run();
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                showing = false;
                invalidate.run();
            }
        });
        animator.start();
    }

    /**
     * Stops any in-flight animation and hides the overlay. Called when the puzzle
     * changes so a stale overlay never bleeds onto a new puzzle.
     */
    void cancel() {
        if (!isNull(animator)) {
            animator.cancel();
            animator = null;
        }
        showing = false;
    }

    void draw(Canvas canvas, float boardSize) {
        if (!showing) {
            return;
        }
        float fade = fadeFactor();

        Paint tintPaint = solved ? solvedTintPaint : unsolvedTintPaint;
        tintPaint.setAlpha((int) (TINT_MAX_ALPHA * fade));
        canvas.drawRect(0, 0, boardSize, boardSize, tintPaint);

        Bitmap icon = getIcon((int) (boardSize * ICON_BOARD_FRACTION));
        if (!isNull(icon)) {
            float left = (boardSize - icon.getWidth()) / 2f;
            float top = (boardSize - icon.getHeight()) / 2f;
            iconPaint.setAlpha((int) (ICON_MAX_ALPHA * fade));
            canvas.drawBitmap(icon, left, top, iconPaint);
        }
    }

    /**
     * Drops cached icons so they re-render at the new board size.
     */
    void onSizeChanged() {
        recycleIcons();
        iconSize = -1;
    }

    /**
     * Releases all cached bitmaps. Call from the host view's detach.
     */
    void recycle() {
        recycleIcons();
    }

    /**
     * Maps the linear 0..1 progress to an alpha multiplier: ramps up over the
     * fade-in window, holds at full, then ramps down over the fade-out window.
     */
    private float fadeFactor() {
        if (animProgress < FADE_IN_END) {
            return animProgress / FADE_IN_END;
        }
        if (animProgress > FADE_OUT_START) {
            return 1f - (animProgress - FADE_OUT_START) / (1f - FADE_OUT_START);
        }
        return 1f;
    }

    /**
     * Lazily renders (and caches) the current result icon tinted to the
     * solved/unsolved color at {@code size} px. Re-renders when the requested
     * size changes (e.g. board resize/rotation).
     */
    private Bitmap getIcon(int size) {
        if (size <= 0) {
            return null;
        }
        if (size != iconSize) {
            recycleIcons();
            iconSize = size;
        }
        if (solved) {
            if (isNull(solvedIconBitmap)) {
                solvedIconBitmap = renderIcon(R.drawable.ic_solved, SOLVED_COLOR, size);
            }
            return solvedIconBitmap;
        }
        if (isNull(unsolvedIconBitmap)) {
            unsolvedIconBitmap = renderIcon(R.drawable.ic_unsolved, UNSOLVED_COLOR, size);
        }
        return unsolvedIconBitmap;
    }

    private Bitmap renderIcon(@DrawableRes int drawableRes, int tintColor, int size) {
        Drawable drawable = AppCompatResources.getDrawable(context, drawableRes);
        if (isNull(drawable)) {
            return null;
        }
        drawable = drawable.mutate();
        drawable.setColorFilter(new PorterDuffColorFilter(tintColor, PorterDuff.Mode.SRC_IN));
        drawable.setBounds(0, 0, size, size);
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        drawable.draw(new Canvas(bitmap));
        return bitmap;
    }

    private void recycleIcons() {
        if (!isNull(solvedIconBitmap)) {
            solvedIconBitmap.recycle();
            solvedIconBitmap = null;
        }
        if (!isNull(unsolvedIconBitmap)) {
            unsolvedIconBitmap.recycle();
            unsolvedIconBitmap = null;
        }
    }
}
