package com.tacticmaster.board;

import android.graphics.BlurMaskFilter;
import android.graphics.Color;
import android.graphics.Paint;

class ChessboardPaintFactory {

    static final int STROKE_WIDTH = 8;
    static final int LABEL_TEXT_SIZE = 30;
    static final float SHADOW_BLUR_RATIO = 0.50f;
    static final float SHADOW_OFFSET_RATIO = 0.04f;
    static final float BEVEL_RATIO = 0.015f;
    static final float EXTRUSION_OFFSET_RATIO = 0.025f;
    static final int EXTRUSION_LAYERS = 3;

    static Paint createSquarePaint(String color) {
        Paint paint = new Paint();
        paint.setColor(Color.parseColor(color));
        return paint;
    }

    static Paint createExtrusionPaint() {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setColor(0x44000000);
        return paint;
    }

    static Paint createBevelHighlightPaint() {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(0x55FFFFFF);
        paint.setStyle(Paint.Style.STROKE);
        return paint;
    }

    static Paint createBevelShadowPaint() {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(0x55000000);
        paint.setStyle(Paint.Style.STROKE);
        return paint;
    }

    static Paint createBitmapPaint() {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setDither(true);
        return paint;
    }

    static Paint createShadowPaint(float tileSize) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setAlpha(0x55);
        paint.setMaskFilter(new BlurMaskFilter(tileSize * SHADOW_BLUR_RATIO, BlurMaskFilter.Blur.NORMAL));
        return paint;
    }

    static Paint createSelectionPaint(boolean isPlayerWhite, boolean isOpponent) {
        Paint paint = new Paint();
        if (isPlayerWhite != isOpponent) {
            paint.setColor(Color.WHITE);
        } else {
            paint.setColor(Color.BLACK);
        }
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(STROKE_WIDTH);
        return paint;
    }

    static Paint createTextPaint() {
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(LABEL_TEXT_SIZE);
        paint.setAntiAlias(true);
        return paint;
    }
}
