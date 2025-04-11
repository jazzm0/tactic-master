package com.tacticmaster.board;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;

public class HintPathView extends View {

    private Paint arrowPaint;

    private float animationProgress = 0f;
    private float startX, startY, endX, endY;
    private boolean isHintPathVisible = false;

    public HintPathView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        arrowPaint = new Paint();
        arrowPaint.setColor(Color.LTGRAY);
        arrowPaint.setStrokeWidth(9f);
        arrowPaint.setStyle(Paint.Style.STROKE);
        arrowPaint.setAntiAlias(true);

        arrowPaint.setAlpha(150);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        if (isHintPathVisible) {
            float currentX = startX + (endX - startX) * animationProgress;
            float currentY = startY + (endY - startY) * animationProgress;

            canvas.drawLine(startX, startY, currentX, currentY, arrowPaint);

            float radius = arrowPaint.getStrokeWidth() / 2;
            canvas.drawCircle(startX, startY, radius, arrowPaint);
            canvas.drawCircle(currentX, currentY, radius, arrowPaint);
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