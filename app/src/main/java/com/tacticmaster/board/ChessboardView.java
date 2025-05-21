package com.tacticmaster.board;

import static java.util.Objects.isNull;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.tacticmaster.R;
import com.tacticmaster.puzzle.Puzzle;

import java.util.concurrent.atomic.AtomicBoolean;

public class ChessboardView extends View {

    public interface PuzzleFinishedListener {
        void onPuzzleSolved(Puzzle puzzle);

        void onPuzzleNotSolved(Puzzle puzzle);
    }

    private static final int NEXT_PUZZLE_DELAY = 3000;
    private Puzzle puzzle;
    private Chessboard chessboard;
    private final ChessboardBitmapManager bitmapManager;
    private static final int BOARD_SIZE = 8;
    private Paint lightBrownPaint;
    private Paint darkBrownPaint;
    private Paint bitmapPaint;
    private Paint selectionPaint;
    private ImageView playerTurnIcon;
    private HintPathView hintPathView;
    private int selectedRow = -1;
    private int selectedCol = -1;
    private Paint textPaint;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private PuzzleFinishedListener puzzleFinishedListener;
    private boolean puzzleSolved = false;
    private final AtomicBoolean isHintFirstClick = new AtomicBoolean(false);
    private float shakeOffset = 0;
    private int hintMoveRow = -1;
    private int hintMoveColumn = -1;

    public ChessboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.bitmapManager = new ChessboardBitmapManager(context);
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
    }

    public void resetHintFirstClick() {
        isHintFirstClick.set(false);
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
            invalidate();
        });

        animator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                shakeOffset = 0;
                hintMoveRow = -1;
                hintMoveColumn = -1;
                invalidate();
            }
        });

        animator.start();
    }

    private void post(Runnable runnable, long delay) {
        handler.removeCallbacksAndMessages(null);
        handler.postDelayed(runnable, delay);
    }

    private void updatePlayerTurnIcon(boolean isWhiteTurn) {
        if (isWhiteTurn) {
            playerTurnIcon.setImageResource(R.drawable.ic_white_turn);
        } else {
            playerTurnIcon.setImageResource(R.drawable.ic_black_turn);
        }
    }

    private float getTileSize() {
        return (float) Math.min(getWidth(), getHeight()) / BOARD_SIZE;
    }

    private Bitmap loadBitmap(int resId, String pieceName) {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), resId);
        if (isNull(bitmap)) {
            throw new IllegalStateException("Failed to load bitmap for " + pieceName + " (resource ID: " + resId + ")");
        }
        return bitmap;
    }

    int getHintMoveRow() {
        return hintMoveRow;
    }

    int getHintMoveColumn() {
        return hintMoveColumn;
    }

    public void setPlayerTurnIcon(ImageView playerTurnIcon) {
        this.playerTurnIcon = playerTurnIcon;
    }

    public void setHintPathView(HintPathView hintPathView) {
        this.hintPathView = hintPathView;
    }

    public void setPuzzle(Puzzle puzzle) {
        this.puzzle = puzzle;
        this.chessboard = new Chessboard(puzzle);
        this.selectedRow = -1;
        this.selectedCol = -1;
        this.puzzleSolved = false;
        resetHintFirstClick();

        invalidate();
        post(() -> {
            this.chessboard.makeFirstMove();
            invalidate();
        }, 2000);
    }

    public void setPuzzleSolvedListener(PuzzleFinishedListener listener) {
        this.puzzleFinishedListener = listener;
    }

    public Puzzle getPuzzle() {
        return puzzle;
    }

    public Chessboard getChessboard() {
        return chessboard;
    }

    public int getSelectedCol() {
        return selectedCol;
    }

    public int getSelectedRow() {
        return selectedRow;
    }

    public void puzzleHintClicked() {
        if (isNull(hintPathView) || isNull(chessboard)) return;

        var move = chessboard.getNextMove();

        if (!isNull(move) && chessboard.isPlayersTurn()) {
            var fromRow = move[0] + 1;
            var fromCol = move[1] + 1;
            var toRow = move[2] + 1;
            var toCol = move[3] + 1;
            var tileSize = getTileSize();
            var halfTileSize = tileSize / 2;

            if (!isHintFirstClick.get()) {
                isHintFirstClick.set(true);
                shakePiece(move[0], move[1]);
            } else {
                hintPathView.setVisibility(VISIBLE);
                hintPathView.drawAnimatedHintPath(
                        (fromCol * tileSize) - halfTileSize,
                        ((fromRow * tileSize) - halfTileSize),
                        (toCol * tileSize) - halfTileSize,
                        ((toRow * tileSize) - halfTileSize)
                );
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

            if (row < 0 || row >= BOARD_SIZE || col < 0 || col >= BOARD_SIZE) {
                return true;
            }

            char piece = chessboard.getPieceAt(row, col);
            boolean isWhiteToMove = chessboard.isWhiteToMove();

            if (selectedRow == -1 && selectedCol == -1) {
                if (piece != ' ' && ((isWhiteToMove && Character.isUpperCase(piece)) || (!isWhiteToMove && Character.isLowerCase(piece)))) {
                    selectedRow = row;
                    selectedCol = col;
                }
            } else if (chessboard.isFirstMoveDone()) {
                if (piece != ' ' && chessboard.isOwnPiece(piece)) {
                    selectedRow = row;
                    selectedCol = col;
                } else {
                    if (chessboard.isCorrectMove(selectedRow, selectedCol, row, col)) {

                        if (chessboard.movePiece(selectedRow, selectedCol, row, col)) {
                            selectedRow = -1;
                            selectedCol = -1;

                            post(() -> {
                                chessboard.makeNextMove();
                                invalidate();
                            }, 1300);
                        }
                    } else {
                        Toast.makeText(getContext(), R.string.wrong_solution, Toast.LENGTH_SHORT).show();
                        puzzleFinishedListener.onPuzzleNotSolved(this.puzzle);
                    }
                }
            }
            invalidate();
            performClick();
            return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        int tileSize = (int) ((Math.min(w, h) * 0.95f / BOARD_SIZE));

        bitmapManager.onSizeChanged(tileSize);
        invalidate();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        int height = getHeight();
        var tileSize = getTileSize();

        boolean isWhiteToMove = chessboard.isWhiteToMove();
        updatePlayerTurnIcon(isWhiteToMove);

        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                var colorChoice = (col + row) % 2 == 0;
                Paint paint = colorChoice ? lightBrownPaint : darkBrownPaint;
                var left = col * tileSize;
                var top = row * tileSize;
                var right = left + tileSize;
                var bottom = top + tileSize;
                canvas.drawRect(left, top, right, bottom, paint);
            }
        }

        if (selectedRow != -1 && selectedCol != -1) {
            var left = selectedCol * tileSize;
            var top = selectedRow * tileSize;
            var right = left + tileSize;
            var bottom = top + tileSize;
            canvas.drawRect(left, top, right, bottom, selectionPaint);
        }

        for (int col = 0; col < BOARD_SIZE; col++) {
            String label = isWhiteToMove ? String.valueOf((char) ('a' + col)) : String.valueOf((char) ('h' - col));
            float x = col * tileSize + (tileSize / 2 - textPaint.measureText(label) / 2) * 1.9f;
            float y = height - 10;
            canvas.drawText(label, x, y, textPaint);
        }

        for (int row = 0; row < BOARD_SIZE; row++) {
            String label = isWhiteToMove ? String.valueOf(BOARD_SIZE - row) : String.valueOf(row + 1);
            float x = 10;
            float y = row * tileSize + (tileSize / 2 + textPaint.getTextSize() / 2) * .4f;
            canvas.drawText(label, x, y, textPaint);
        }

        if (!isNull(chessboard)) {
            for (int row = 0; row < BOARD_SIZE; row++) {
                for (int col = 0; col < BOARD_SIZE; col++) {
                    char piece = chessboard.getPieceAt(row, col);
                    if (piece != ' ') {
                        Bitmap pieceBitmap = bitmapManager.getPieceBitmap(piece);
                        if (!isNull(pieceBitmap)) {
                            float left = col * tileSize + 3.5f;
                            float top = row * tileSize;

                            if (row == hintMoveRow && col == hintMoveColumn) {
                                left += shakeOffset;
                            }

                            canvas.drawBitmap(pieceBitmap, left, top, bitmapPaint);
                        }
                    }
                }
            }
            if (!isNull(chessboard) && chessboard.solved() && !isNull(puzzleFinishedListener) && !puzzleSolved) {
                puzzleSolved = true;
                Toast.makeText(getContext(), R.string.correct_solution, Toast.LENGTH_SHORT).show();
                post(() -> {
                    synchronized (ChessboardView.this) {
                        puzzleFinishedListener.onPuzzleSolved(this.puzzle);
                    }
                }, NEXT_PUZZLE_DELAY);
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        bitmapManager.recycleBitmaps();
    }
}