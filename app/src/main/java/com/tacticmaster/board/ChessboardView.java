package com.tacticmaster.board;

import static java.util.Objects.isNull;

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

public class ChessboardView extends View {

    public interface PuzzleFinishedListener {
        void onPuzzleSolved(Puzzle puzzle);

        void onPuzzleNotSolved(Puzzle puzzle);
    }

    private static final int NEXT_PUZZLE_DELAY = 3000;
    private Puzzle puzzle;
    private Chessboard chessboard;
    private static final int BOARD_SIZE = 8;
    private Paint lightBrownPaint;
    private Paint darkBrownPaint;
    private Paint bitmapPaint;
    private Paint selectionPaint;
    private Bitmap whiteKing, blackKing, whiteQueen, blackQueen, whiteRook, blackRook, whiteBishop, blackBishop, whiteKnight, blackKnight, whitePawn, blackPawn;
    private Bitmap scaledWhiteKing, scaledBlackKing, scaledWhiteQueen, scaledBlackQueen, scaledWhiteRook, scaledBlackRook, scaledWhiteBishop, scaledBlackBishop, scaledWhiteKnight, scaledBlackKnight, scaledWhitePawn, scaledBlackPawn;
    private ImageView playerTurnIcon;
    private HintPathView hintPathView;
    private int selectedRow = -1;
    private int selectedCol = -1;
    private Paint textPaint;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private PuzzleFinishedListener puzzleFinishedListener;
    private boolean puzzleSolved = false;

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

    private void updatePlayerTurnIcon(boolean isWhiteTurn) {
        if (isWhiteTurn) {
            playerTurnIcon.setImageResource(R.drawable.ic_white_turn);
        } else {
            playerTurnIcon.setImageResource(R.drawable.ic_black_turn);
        }
    }

    public void setPlayerTurnIcon(ImageView playerTurnIcon) {
        this.playerTurnIcon = playerTurnIcon;
    }

    public void setArrowView(HintPathView hintPathView) {
        this.hintPathView = hintPathView;
    }

    public void setPuzzle(Puzzle puzzle) {
        this.puzzle = puzzle;
        this.chessboard = new Chessboard(puzzle);
        this.selectedRow = -1;
        this.selectedCol = -1;
        this.puzzleSolved = false;

        invalidate();
        handler.postDelayed(() -> {
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
        var move = chessboard.getNextMove();
        if (!isNull(move)) {
            int fromRow = move[0] + 2;
            int fromCol = move[1] + 1;
            int toRow = move[2] + 2;
            int toCol = move[3] + 1;
            var tileSize = getTileSize();
            var halfTileSize = getTileSize() / 2;

            hintPathView.setVisibility(VISIBLE);
            hintPathView.drawAnimatedArrow((fromCol * tileSize) - halfTileSize, fromRow * tileSize * 1.02f, toCol * tileSize - halfTileSize, toRow * tileSize * 1.02f);
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

                            handler.postDelayed(() -> {
                                chessboard.makeNextMove();
                                invalidate();
                            }, 1300);
                        }
                    } else {
                        Toast.makeText(getContext(), "Wrong solution", Toast.LENGTH_SHORT).show();

                        handler.postDelayed(() -> {
                            puzzleFinishedListener.onPuzzleNotSolved(this.puzzle);
                        }, NEXT_PUZZLE_DELAY);
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
                if (!isWhiteToMove)
                    colorChoice = !colorChoice;
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
            String label = String.valueOf((char) ('a' + col));
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
                        Bitmap pieceBitmap = getPieceBitmap(piece);
                        if (!isNull(pieceBitmap)) {
                            float left = col * tileSize + 3.5f;
                            float top = row * tileSize;
                            canvas.drawBitmap(pieceBitmap, left, top, bitmapPaint);
                        }
                    }
                }
            }
            if (chessboard.solved() && !isNull(puzzleFinishedListener) && !puzzleSolved) {
                puzzleSolved = true;
                handler.postDelayed(() -> puzzleFinishedListener.onPuzzleSolved(this.puzzle), NEXT_PUZZLE_DELAY);
            }
        }
    }

    private float getTileSize() {
        return (float) Math.min(getWidth(), getHeight()) / BOARD_SIZE;
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
}