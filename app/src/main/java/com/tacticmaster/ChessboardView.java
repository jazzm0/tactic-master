package com.tacticmaster;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

public class ChessboardView extends View {

    private static final int BOARD_SIZE = 8;
    private Paint lightBrownPaint;
    private Paint darkBrownPaint;

    public ChessboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        lightBrownPaint = new Paint();
        lightBrownPaint.setColor(Color.parseColor("#D2B48C")); // Light brown color
        darkBrownPaint = new Paint();
        darkBrownPaint.setColor(Color.parseColor("#8B4513")); // Darker brown color
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();
        int tileSize = Math.min(width, height) / BOARD_SIZE;

        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                Paint paint = (row + col) % 2 == 0 ? lightBrownPaint : darkBrownPaint;
                int left = col * tileSize;
                int top = row * tileSize;
                int right = left + tileSize;
                int bottom = top + tileSize;
                canvas.drawRect(left, top, right, bottom, paint);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            int width = getWidth();
            int height = getHeight();
            int tileSize = Math.min(width, height) / BOARD_SIZE;

            int col = (int) (event.getX() / tileSize);
            int row = (int) (event.getY() / tileSize);

            Toast.makeText(getContext(), "Clicked: (" + row + ", " + col + ")", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onTouchEvent(event);
    }
}