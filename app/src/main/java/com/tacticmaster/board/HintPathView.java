package com.tacticmaster.board;

import static java.util.Objects.isNull;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;

import java.util.concurrent.atomic.AtomicBoolean;

public class HintPathView extends View {

    public interface ViewChangedListener {
        void onViewChanged();
    }

    private Paint paint;
    private float animationProgress = 0f;
    private float startX, startY, endX, endY;
    private boolean isHintPathVisible = false;
    private final AtomicBoolean isHintFirstClick = new AtomicBoolean(false);
    private float shakeOffset = 0;
    private int hintMoveRow = -1;
    private int hintMoveColumn = -1;
    private ViewChangedListener viewChangedListener;

    public HintPathView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setColor(0xFFF6A951);
        paint.setStrokeWidth(9f);
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);

        paint.setAlpha(200);
    }

    private void notifyChildInvalidated() {
        if (!isNull(viewChangedListener)) {
            viewChangedListener.onViewChanged();
        }
    }

    private void shakePiece(int row, int col) {
        hintMoveRow = row;
        hintMoveColumn = col;

        ValueAnimator animator = ValueAnimator.ofFloat(-10, 10);
        animator.setDuration(100);
        animator.setRepeatCount(5);
        animator.setRepeatMode(ValueAnimator.REVERSE);

        animator.addUpdateListener(animation -> {
            shakeOffset = (float) animation.getAnimatedValue();
            notifyChildInvalidated();
        });

        animator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                shakeOffset = 0;
                hintMoveRow = -1;
                hintMoveColumn = -1;
                notifyChildInvalidated();
            }
        });

        animator.start();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        if (isHintPathVisible) {
            float currentX = startX + (endX - startX) * animationProgress;
            float currentY = startY + (endY - startY) * animationProgress;

            canvas.drawLine(startX, startY, currentX, currentY, paint);

            float radius = paint.getStrokeWidth() / 2;
            canvas.drawCircle(startX, startY, radius, paint);
            canvas.drawCircle(currentX, currentY, radius, paint);
        }
    }

    int getHintMoveRow() {
        return hintMoveRow;
    }

    int getHintMoveColumn() {
        return hintMoveColumn;
    }

    public float getShakeOffset(int row, int col) {
        if (row == hintMoveRow && col == hintMoveColumn) {
            return shakeOffset;
        }
        return 0;
    }

    public void setHintPathListener(ViewChangedListener listener) {
        this.viewChangedListener = listener;
    }

    public void resetHintFirstClick() {
        isHintFirstClick.set(false);
    }

    public void puzzleHintClicked(Chessboard chessboard, float tileSize) {
        if (isNull(chessboard)) return;

        var move = chessboard.getNextMove();

        if (!isNull(move) && chessboard.isPlayersTurn()) {
            var fromRow = move[0] + 1;
            var fromCol = move[1] + 1;
            var toRow = move[2] + 1;
            var toCol = move[3] + 1;
            var halfTileSize = tileSize / 2;

            if (!isHintFirstClick.get()) {
                isHintFirstClick.set(true);
                shakePiece(move[0], move[1]);
            } else {
                this.setVisibility(VISIBLE);
                this.drawAnimatedHintPath(
                        (fromCol * tileSize) - halfTileSize,
                        ((fromRow * tileSize) - halfTileSize),
                        (toCol * tileSize) - halfTileSize,
                        ((toRow * tileSize) - halfTileSize)
                );
            }
        }
    }

    public void drawAnimatedHintPath(float startX, float startY, float endX, float endY) {
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
        this.isHintPathVisible = true;

        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(1000);
        animator.addUpdateListener(animation -> {
            animationProgress = (float) animation.getAnimatedValue();
            invalidate();
        });

        animator.addListener(new ValueAnimator.AnimatorListener() {
            @Override
            public void onAnimationStart(@NonNull android.animation.Animator animation) {
            }

            @Override
            public void onAnimationEnd(@NonNull android.animation.Animator animation) {
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    isHintPathVisible = false;
                    invalidate();
                }, 1500);
            }

            @Override
            public void onAnimationCancel(@NonNull android.animation.Animator animation) {
            }

            @Override
            public void onAnimationRepeat(@NonNull android.animation.Animator animation) {
            }
        });
        animator.start();
    }
}