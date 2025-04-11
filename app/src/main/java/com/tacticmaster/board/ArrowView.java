package com.tacticmaster.board;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;

public class ArrowView extends View {

    private Paint arrowPaint;
    private Paint arrowHeadPaint;
    private float animationProgress = 0f;
    private float startX, startY, endX, endY;
    private boolean isArrowVisible = false;
    private final Path arrowHeadPath = new Path();

    public ArrowView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        arrowPaint = new Paint();
        arrowPaint.setColor(Color.LTGRAY);
        arrowPaint.setStrokeWidth(13f);
        arrowPaint.setStyle(Paint.Style.STROKE);
        arrowPaint.setAntiAlias(true);

        arrowHeadPaint = new Paint();
        arrowHeadPaint.setColor(Color.LTGRAY);
        arrowHeadPaint.setStyle(Paint.Style.FILL);
        arrowHeadPaint.setAntiAlias(true);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        if (isArrowVisible) {
            float currentX = startX + (endX - startX) * animationProgress;
            float currentY = startY + (endY - startY) * animationProgress;

            canvas.drawLine(startX, startY, currentX * 0.99f, currentY * 0.99f, arrowPaint);

            if (animationProgress > 0) {
                float arrowHeadSize = 40f;
                float angle = (float) Math.atan2(endY - startY, endX - startX);

                float x1 = currentX - arrowHeadSize * (float) Math.cos(angle - Math.PI / 6);
                float y1 = currentY - arrowHeadSize * (float) Math.sin(angle - Math.PI / 6);
                float x2 = currentX - arrowHeadSize * (float) Math.cos(angle + Math.PI / 6);
                float y2 = currentY - arrowHeadSize * (float) Math.sin(angle + Math.PI / 6);

                arrowHeadPath.reset();
                arrowHeadPath.moveTo(currentX, currentY);
                arrowHeadPath.lineTo(x1, y1);
                arrowHeadPath.lineTo(x2, y2);
                arrowHeadPath.close();

                canvas.drawPath(arrowHeadPath, arrowHeadPaint);
            }
        }
    }

    public void drawAnimatedArrow(float startX, float startY, float endX, float endY) {
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
        this.isArrowVisible = true;

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
                    isArrowVisible = false;
                    invalidate();
                }, 2000);
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