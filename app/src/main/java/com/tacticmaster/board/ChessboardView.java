package com.tacticmaster.board;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.tacticmaster.puzzle.Puzzle;

public class ChessboardView extends View {

    private Puzzle puzzle;
    private Chessboard chessboard;
    private static final int BOARD_SIZE = 8;
    private Paint lightBrownPaint;
    private Paint darkBrownPaint;
    private Paint textPaint;

    public ChessboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        lightBrownPaint = new Paint();
        lightBrownPaint.setColor(Color.parseColor("#D2B48C")); // Light brown color
        darkBrownPaint = new Paint();
        darkBrownPaint.setColor(Color.parseColor("#8B4513")); // Darker brown color
        textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(30);
        textPaint.setAntiAlias(true);
    }

    public void setPuzzle(Puzzle puzzle) {
        this.puzzle = puzzle;
        this.chessboard = new Chessboard(puzzle);
        invalidate(); // Request a redraw
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        invalidate(); // Request a redraw when the size changes
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();
        int tileSize = Math.min(width, height) / BOARD_SIZE;

        // Draw the chessboard
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

        // Draw the column labels (a-h)
        for (int col = 0; col < BOARD_SIZE; col++) {
            String label = String.valueOf((char) ('a' + col));
            float x = col * tileSize + (tileSize / 2 - textPaint.measureText(label) / 2) * 1.8f;
            float y = height - 10;
            canvas.drawText(label, x, y, textPaint);
        }

        // Draw the row labels (1-8)
        for (int row = 0; row < BOARD_SIZE; row++) {
            String label = String.valueOf(BOARD_SIZE - row);
            float x = 10;
            float y = row * tileSize + (tileSize / 2 + textPaint.getTextSize() / 2) * .4f;
            canvas.drawText(label, x, y, textPaint);
        }

        if (chessboard != null) {
            for (int row = 0; row < BOARD_SIZE; row++) {
                for (int col = 0; col < BOARD_SIZE; col++) {
                    char piece = chessboard.getBoard()[row][col];
                    if (piece != ' ') {
                        float x = col * tileSize + (tileSize / 2 - textPaint.measureText(String.valueOf(piece)) / 2);
                        float y = row * tileSize + (tileSize / 2 + textPaint.getTextSize() / 2);
                        canvas.drawText(String.valueOf(piece), x, y, textPaint);
                    }
                }
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