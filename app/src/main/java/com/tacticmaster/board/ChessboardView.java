package com.tacticmaster.board;

import static java.util.Objects.isNull;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.tacticmaster.R;
import com.tacticmaster.puzzle.Puzzle;

public class ChessboardView extends View implements PuzzleHintView.ViewChangedListener {

    public interface PuzzleFinishedListener {
        void onPuzzleSolved(Puzzle puzzle);

        void onPuzzleNotSolved(Puzzle puzzle);
    }

    private static final int BOARD_SIZE = 8;
    private static final int NEXT_PUZZLE_DELAY = 3000;

    private final ChessboardPieceManager bitmapManager;

    private Paint lightBrownPaint, darkBrownPaint, bitmapPaint, selectionPaint, textPaint;
    private Puzzle puzzle;
    private Chessboard chessboard;
    private PuzzleHintView puzzleHintView;
    private PuzzleFinishedListener puzzleFinishedListener;
    private ImageView playerTurnIcon;

    private int selectedRow = -1, selectedColumn = -1;
    private boolean puzzleSolved = false;

    public ChessboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.bitmapManager = new ChessboardPieceManager(context);
        initPaints();
    }

    private void initPaints() {
        lightBrownPaint = createPaint("#D2B48C");
        darkBrownPaint = createPaint("#8B4513");
        bitmapPaint = createBitmapPaint();
        selectionPaint = createSelectionPaint();
        textPaint = createTextPaint();
    }

    private Paint createPaint(String color) {
        Paint paint = new Paint();
        paint.setColor(Color.parseColor(color));
        return paint;
    }

    private Paint createBitmapPaint() {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setDither(true);
        return paint;
    }

    private Paint createSelectionPaint() {
        Paint paint = new Paint();
        paint.setColor(Color.YELLOW);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
        return paint;
    }

    private Paint createTextPaint() {
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(30);
        paint.setAntiAlias(true);
        return paint;
    }

    private void drawBoard(Canvas canvas) {
        float tileSize = getTileSize();
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int column = 0; column < BOARD_SIZE; column++) {
                Paint paint = (row + column) % 2 == 0 ? lightBrownPaint : darkBrownPaint;
                canvas.drawRect(column * tileSize, row * tileSize, (column + 1) * tileSize, (row + 1) * tileSize, paint);
            }
        }
        if (selectedRow != -1 && selectedColumn != -1 && !puzzleSolved) {
            float left = selectedColumn * tileSize;
            float top = selectedRow * tileSize;
            canvas.drawRect(left, top, left + tileSize, top + tileSize, selectionPaint);
        }
    }

    private void drawLabels(Canvas canvas) {
        float tileSize = getTileSize();
        int height = getHeight();
        boolean isWhiteToMove = chessboard.isWhiteToMove();
        for (int index = 0; index < BOARD_SIZE; index++) {
            String columnLabel = isWhiteToMove ? String.valueOf((char) ('a' + index)) : String.valueOf((char) ('h' - index));
            String rowLabel = isWhiteToMove ? String.valueOf(BOARD_SIZE - index) : String.valueOf(index + 1);

            canvas.drawText(columnLabel, index * tileSize + (tileSize / 2 - textPaint.measureText(columnLabel) / 2) * 1.9f, height - 10, textPaint);
            canvas.drawText(rowLabel, 10, index * tileSize + (tileSize / 2 + textPaint.getTextSize() / 2) * .4f, textPaint);
        }
    }

    private void drawPieces(Canvas canvas) {
        float tileSize = getTileSize();
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int column = 0; column < BOARD_SIZE; column++) {
                char piece = chessboard.getPieceAt(row, column);
                if (piece != ' ') {
                    Bitmap pieceBitmap = bitmapManager.getPieceBitmap(piece);
                    if (!isNull(pieceBitmap)) {
                        float left = column * tileSize + puzzleHintView.getShakeOffset(row, column);
                        float top = row * tileSize;
                        canvas.drawBitmap(pieceBitmap, left, top, bitmapPaint);
                    }
                }
            }
        }
    }

    public void makeText(int resourceId) {
        Toast toast = new Toast(getContext());
        View customView = View.inflate(getContext(), R.layout.custom_toast_layout, null);

        TextView textView = customView.findViewById(R.id.toast_text);
        textView.setText(getContext().getString(resourceId));

        toast.setView(customView);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.show();
    }

    private void checkPuzzleSolved() {
        if (chessboard.solved() && !puzzleSolved && !isNull(puzzleFinishedListener)) {
            puzzleSolved = true;
            makeText(R.string.correct_solution);
            postDelayed(() -> puzzleFinishedListener.onPuzzleSolved(this.puzzle), NEXT_PUZZLE_DELAY);
        }
    }

    private float getTileSize() {
        return Math.min(getWidth(), getHeight()) / (float) BOARD_SIZE;
    }

    private void updatePlayerTurnIcon(boolean isWhiteTurn) {
        if (isWhiteTurn) {
            playerTurnIcon.setImageResource(R.drawable.ic_white_turn);
        } else {
            playerTurnIcon.setImageResource(R.drawable.ic_black_turn);
        }
    }

    private void selectPiece(int row, int column, char piece) {
        if (piece != ' ' && chessboard.isOwnPiece(piece) && !chessboard.solved()) {
            selectedRow = row;
            selectedColumn = column;
        }
    }

    private void unselectPiece() {
        selectedRow = -1;
        selectedColumn = -1;
    }

    private void handleMove(int row, int column) {
        if (chessboard.isCorrectMove(selectedRow, selectedColumn, row, column)) {

            if (chessboard.isPromotionMove(selectedRow, selectedColumn, row, column)) {
                PromotionDialog.show(getContext(), bitmapManager, chessboard.isWhiteToMove(), piece -> {
                    if (!chessboard.isCorrectPromotionPiece(piece)) {
                        onFailedMove();
                    } else {
                        invalidate();
                        onCorrectMove(row, column);
                    }
                });
                return;
            }
            onCorrectMove(row, column);
        } else {
            onFailedMove();
        }
    }

    private void onCorrectMove(int row, int column) {
        if (chessboard.movePiece(selectedRow, selectedColumn, row, column)) {
            unselectPiece();

            postDelayed(() -> {
                chessboard.makeNextMove();
                invalidate();
            }, 1300);
        }
    }

    private void onFailedMove() {
        makeText(R.string.wrong_solution);
        postDelayed(() -> puzzleFinishedListener.onPuzzleNotSolved(this.puzzle), NEXT_PUZZLE_DELAY);
        resetSelection();
        invalidate();
    }

    private void resetSelection() {
        unselectPiece();
        puzzleSolved = false;
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        bitmapManager.onSizeChanged((int) getTileSize());
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        drawBoard(canvas);
        drawLabels(canvas);
        drawPieces(canvas);
        checkPuzzleSolved();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        bitmapManager.recycleBitmaps();
    }

    int getSelectedColumn() {
        return selectedColumn;
    }

    int getSelectedRow() {
        return selectedRow;
    }

    Puzzle getPuzzle() {
        return puzzle;
    }

    Chessboard getChessboard() {
        return chessboard;
    }

    public void puzzleHintClicked() {
        puzzleHintView.puzzleHintClicked(chessboard, getTileSize());
    }

    public void setPuzzleHintView(PuzzleHintView puzzleHintView) {
        this.puzzleHintView = puzzleHintView;
        puzzleHintView.setHintPathListener(this);
    }

    public void setPlayerTurnIcon(ImageView playerTurnIcon) {
        this.playerTurnIcon = playerTurnIcon;
    }

    public void setPuzzle(Puzzle puzzle) {
        this.puzzle = puzzle;
        this.chessboard = new Chessboard(puzzle);
        resetSelection();
        puzzleHintView.resetHintFirstClick();
        updatePlayerTurnIcon(chessboard.isWhiteToMove());
        invalidate();
        postDelayed(() -> {
            chessboard.makeFirstMove();
            invalidate();
        }, 2000);
    }

    public void setPuzzleSolvedListener(PuzzleFinishedListener listener) {
        this.puzzleFinishedListener = listener;
    }

    @Override
    public void onViewChanged() {
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN && chessboard.isFirstMoveDone()) {
            int tileSize = (int) getTileSize();

            int column = (int) (event.getX() / tileSize);
            int row = (int) (event.getY() / tileSize);

            if (row < 0 || row >= BOARD_SIZE || column < 0 || column >= BOARD_SIZE) {
                return true;
            }

            char piece = chessboard.getPieceAt(row, column);

            if (selectedRow == -1 && selectedColumn == -1 || piece != ' ' && chessboard.isOwnPiece(piece)) {
                selectPiece(row, column, piece);
            } else {
                handleMove(row, column);
            }
        }
        invalidate();
        performClick();
        return true;
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }
}