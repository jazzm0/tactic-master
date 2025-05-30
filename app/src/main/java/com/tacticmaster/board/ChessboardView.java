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
import com.tacticmaster.puzzle.PuzzleGame;

public class ChessboardView extends View implements PuzzleHintView.ViewChangedListener {

    public interface PuzzleFinishedListener {
        void onPuzzleSolved(PuzzleGame puzzle);

        void onPuzzleNotSolved(PuzzleGame puzzle);
    }

    public static final int BOARD_SIZE = 8;
    private static final int NEXT_PUZZLE_DELAY = 3000;

    private final ChessboardPieceManager bitmapManager;

    private Paint lightBrownPaint, darkBrownPaint, bitmapPaint, selectionPaint, textPaint;

    private PuzzleGame puzzleGame;
    private Chessboard chessboard;
    private PuzzleHintView puzzleHintView;
    private PuzzleFinishedListener puzzleFinishedListener;
    private ImageView playerTurnIcon;

    private int selectedRank = -1, selectedFile = -1;
    private boolean puzzleFinished = false;

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
        for (int rank = 0; rank < BOARD_SIZE; rank++) {
            for (int file = 0; file < BOARD_SIZE; file++) {
                Paint paint = (rank + file) % 2 == 0 ? lightBrownPaint : darkBrownPaint;
                canvas.drawRect(file * tileSize, rank * tileSize, (file + 1) * tileSize, (rank + 1) * tileSize, paint);
            }
        }
        if (selectedRank != -1 && selectedFile != -1 && !puzzleFinished) {
            float left = selectedFile * tileSize;
            float top = selectedRank * tileSize;
            canvas.drawRect(left, top, left + tileSize, top + tileSize, selectionPaint);
        }
    }

    private void drawLabels(Canvas canvas) {
        float tileSize = getTileSize();
        int height = getHeight();
        for (int index = 0; index < BOARD_SIZE; index++) {
            String fileLabel = !chessboard.isPlayerWhite() ? String.valueOf((char) ('h' - index)) : String.valueOf((char) ('a' + index));
            String rankLabel = !chessboard.isPlayerWhite() ? String.valueOf(index + 1) : String.valueOf(BOARD_SIZE - index);

            canvas.drawText(fileLabel, index * tileSize + (tileSize / 2 - textPaint.measureText(fileLabel) / 2) * 1.9f, height - 10, textPaint);
            canvas.drawText(rankLabel, 10, index * tileSize + (tileSize / 2 + textPaint.getTextSize() / 2) * .4f, textPaint);
        }
    }

    private void drawPieces(Canvas canvas) {
        float tileSize = getTileSize();
        for (int rank = 0; rank < BOARD_SIZE; rank++) {
            for (int file = 0; file < BOARD_SIZE; file++) {
                var currentPiece = chessboard.getPiece(rank, file);
                if (Chessboard.NONE_PIECE != currentPiece) {
                    Bitmap pieceBitmap = bitmapManager.getPieceBitmap(currentPiece);
                    if (!isNull(pieceBitmap)) {
                        float left = file * tileSize + puzzleHintView.getShakeOffset(rank, file);
                        float top = rank * tileSize;
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

    private void onPuzzleSolved(PuzzleGame puzzle) {
        puzzleFinished = true;
        makeText(R.string.correct_solution);
        postDelayed(() -> puzzleFinishedListener.onPuzzleSolved(puzzle), NEXT_PUZZLE_DELAY);
    }

    private float getTileSize() {
        return Math.min(getWidth(), getHeight()) / (float) BOARD_SIZE;
    }

    private void updatePlayerTurnIcon() {
        if (chessboard.isPlayerWhite()) {
            playerTurnIcon.setImageResource(R.drawable.ic_white_turn);
        } else {
            playerTurnIcon.setImageResource(R.drawable.ic_black_turn);
        }
    }

    private void selectPiece(int rank, int file) {
        selectedRank = rank;
        selectedFile = file;
    }

    private void unselectPiece() {
        selectedRank = -1;
        selectedFile = -1;
    }

    private void proposeMove(int rank, int file) {
        var proposedMove = chessboard.getProposedMove(selectedRank, selectedFile, rank, file);
        if (chessboard.isPromotionMove(selectedRank, selectedFile, rank, file)) {
            PromotionDialog.show(getContext(), bitmapManager, chessboard.isPlayerWhite(), getTileSize(), piece -> {
                var proposedPromotionMove = chessboard.getPromotionMove(selectedRank, selectedFile, rank, file, piece);
                handleMove(proposedPromotionMove);
            });
        } else {
            handleMove(proposedMove);
        }
    }

    private void handleMove(String move) {
        unselectPiece();
        if (!chessboard.isMoveLeadingToMate(move) && !puzzleGame.isCorrectNextMove(move)) {
            makeText(R.string.wrong_solution);
            postDelayed(() -> puzzleFinishedListener.onPuzzleNotSolved(puzzleGame), NEXT_PUZZLE_DELAY);
        } else {
            doNextMove();
            if (puzzleGame.isSolutionFound()) {
                onPuzzleSolved(puzzleGame);
            } else {
                postDelayed(this::doNextMove, 1300);
            }
        }
    }

    private void doNextMove() {
        chessboard.doMove(puzzleGame.getNextMove());
        invalidate();
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
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        bitmapManager.recycleBitmaps();
    }

    int getSelectedFile() {
        return selectedFile;
    }

    int getSelectedRank() {
        return selectedRank;
    }

    void doFirstMove() {
        if (!puzzleGame.isStarted() && !chessboard.isPlayersTurn()) {
            doNextMove();
        }
    }

    public void puzzleHintClicked() {
        if (!isNull(chessboard) && chessboard.isPlayersTurn()) {
            puzzleHintView.showHint(chessboard.getHintMove(puzzleGame.getNextMove(false)), getTileSize());
        }
    }

    public void setPuzzleHintView(PuzzleHintView puzzleHintView) {
        this.puzzleHintView = puzzleHintView;
        puzzleHintView.setHintPathListener(this);
    }

    public void setPlayerTurnIcon(ImageView playerTurnIcon) {
        this.playerTurnIcon = playerTurnIcon;
    }

    public void setPuzzle(PuzzleGame puzzle) {
        puzzleFinished = false;
        this.puzzleGame = puzzle;
        puzzleGame.reset();
        this.chessboard = new Chessboard(puzzleGame.fen());
        updatePlayerTurnIcon();
        unselectPiece();
        puzzleHintView.resetHintFirstClick();
        invalidate();
        postDelayed(this::doFirstMove, 2000);
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
        if (event.getAction() == MotionEvent.ACTION_DOWN && puzzleGame.isStarted() && !puzzleFinished) {
            int tileSize = (int) getTileSize();

            int file = (int) (event.getX() / tileSize);
            int rank = (int) (event.getY() / tileSize);

            if (rank < 0 || rank >= BOARD_SIZE || file < 0 || file >= BOARD_SIZE) {
                return true;
            }

            var piece = chessboard.getPiece(rank, file);

            if (Chessboard.NONE_PIECE != piece && chessboard.isOwnPiece(piece)) {
                selectPiece(rank, file);
            } else if (selectedRank != -1 && selectedFile != -1) {
                proposeMove(rank, file);
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