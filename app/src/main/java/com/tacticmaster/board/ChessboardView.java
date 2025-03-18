package com.tacticmaster.board;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.tacticmaster.R;
import com.tacticmaster.puzzle.Puzzle;

public class ChessboardView extends View {

    private Puzzle puzzle;
    private Chessboard chessboard;
    private static final int BOARD_SIZE = 8;
    private Paint lightBrownPaint;
    private Paint darkBrownPaint;
    private Paint textPaint;
    private Paint bitmapPaint;
    private Paint selectionPaint;
    private Bitmap whiteKing, blackKing, whiteQueen, blackQueen, whiteRook, blackRook, whiteBishop, blackBishop, whiteKnight, blackKnight, whitePawn, blackPawn;
    private Bitmap scaledWhiteKing, scaledBlackKing, scaledWhiteQueen, scaledBlackQueen, scaledWhiteRook, scaledBlackRook, scaledWhiteBishop, scaledBlackBishop, scaledWhiteKnight, scaledBlackKnight, scaledWhitePawn, scaledBlackPawn;
    private int selectedRow = -1;
    private int selectedCol = -1;

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

        bitmapPaint = new Paint();
        bitmapPaint.setAntiAlias(true);
        bitmapPaint.setFilterBitmap(true);
        bitmapPaint.setDither(true);

        selectionPaint = new Paint();
        selectionPaint.setColor(Color.YELLOW);
        selectionPaint.setStyle(Paint.Style.STROKE);
        selectionPaint.setStrokeWidth(5);

        // Load piece images
        whiteKing = BitmapFactory.decodeResource(getResources(), R.drawable.wk);
        blackKing = BitmapFactory.decodeResource(getResources(), R.drawable.bk);
        whiteQueen = BitmapFactory.decodeResource(getResources(), R.drawable.wq);
        blackQueen = BitmapFactory.decodeResource(getResources(), R.drawable.bq);
        whiteRook = BitmapFactory.decodeResource(getResources(), R.drawable.wr);
        blackRook = BitmapFactory.decodeResource(getResources(), R.drawable.br);
        whiteBishop = BitmapFactory.decodeResource(getResources(), R.drawable.wb);
        blackBishop = BitmapFactory.decodeResource(getResources(), R.drawable.bb);
        whiteKnight = BitmapFactory.decodeResource(getResources(), R.drawable.wn);
        blackKnight = BitmapFactory.decodeResource(getResources(), R.drawable.bn);
        whitePawn = BitmapFactory.decodeResource(getResources(), R.drawable.wp);
        blackPawn = BitmapFactory.decodeResource(getResources(), R.drawable.bp);
    }

    public void setPuzzle(Puzzle puzzle) {
        this.puzzle = puzzle;
        this.chessboard = new Chessboard(puzzle);
        invalidate(); // Request a redraw
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        int tileSize = Math.min(w, h) / BOARD_SIZE;

        // Scale piece images to fit the tiles
        scaledWhiteKing = Bitmap.createScaledBitmap(whiteKing, tileSize, tileSize, true);
        scaledBlackKing = Bitmap.createScaledBitmap(blackKing, tileSize, tileSize, true);
        scaledWhiteQueen = Bitmap.createScaledBitmap(whiteQueen, tileSize, tileSize, true);
        scaledBlackQueen = Bitmap.createScaledBitmap(blackQueen, tileSize, tileSize, true);
        scaledWhiteRook = Bitmap.createScaledBitmap(whiteRook, tileSize, tileSize, true);
        scaledBlackRook = Bitmap.createScaledBitmap(blackRook, tileSize, tileSize, true);
        scaledWhiteBishop = Bitmap.createScaledBitmap(whiteBishop, tileSize, tileSize, true);
        scaledBlackBishop = Bitmap.createScaledBitmap(blackBishop, tileSize, tileSize, true);
        scaledWhiteKnight = Bitmap.createScaledBitmap(whiteKnight, tileSize, tileSize, true);
        scaledBlackKnight = Bitmap.createScaledBitmap(blackKnight, tileSize, tileSize, true);
        scaledWhitePawn = Bitmap.createScaledBitmap(whitePawn, tileSize, tileSize, true);
        scaledBlackPawn = Bitmap.createScaledBitmap(blackPawn, tileSize, tileSize, true);

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

        // Draw the selection rectangle
        if (selectedRow != -1 && selectedCol != -1) {
            int left = selectedCol * tileSize;
            int top = selectedRow * tileSize;
            int right = left + tileSize;
            int bottom = top + tileSize;
            canvas.drawRect(left, top, right, bottom, selectionPaint);
        }

        // Draw the pieces
        if (chessboard != null) {
            for (int row = 0; row < BOARD_SIZE; row++) {
                for (int col = 0; col < BOARD_SIZE; col++) {
                    char piece = chessboard.getBoard()[row][col];
                    if (piece != ' ') {
                        Bitmap pieceBitmap = getPieceBitmap(piece);
                        if (pieceBitmap != null) {
                            float left = col * tileSize;
                            float top = row * tileSize;
                            canvas.drawBitmap(pieceBitmap, left, top, bitmapPaint);
                        }
                    }
                }
            }
        }
    }

    private Bitmap getPieceBitmap(char piece) {
        return switch (piece) {
            case 'K' -> scaledWhiteKing;
            case 'k' -> scaledBlackKing;
            case 'Q' -> scaledWhiteQueen;
            case 'q' -> scaledBlackQueen;
            case 'R' -> scaledWhiteRook;
            case 'r' -> scaledBlackRook;
            case 'B' -> scaledWhiteBishop;
            case 'b' -> scaledBlackBishop;
            case 'N' -> scaledWhiteKnight;
            case 'n' -> scaledBlackKnight;
            case 'P' -> scaledWhitePawn;
            case 'p' -> scaledBlackPawn;
            default -> null;
        };
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            int width = getWidth();
            int height = getHeight();
            int tileSize = Math.min(width, height) / BOARD_SIZE;

            int col = (int) (event.getX() / tileSize);
            int row = (int) (event.getY() / tileSize);

            if (chessboard.getBoard()[row][col] != ' ') {
                selectedRow = row;
                selectedCol = col;
                invalidate(); // Request a redraw
            }

            Toast.makeText(getContext(), "Clicked: (" + row + ", " + col + ")", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onTouchEvent(event);
    }
}