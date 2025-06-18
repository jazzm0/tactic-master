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

        void onAfterPuzzleFinished(PuzzleGame puzzle);
    }

    public static final int BOARD_SIZE = 8;
    private static final int NEXT_PUZZLE_DELAY = 3000;
    private static final int MOVE_DELAY = 1300;
    private static final int STROKE_WIDTH = 8;

    private final ChessboardPieceManager bitmapManager;

    private Paint lightBrownPaint, darkBrownPaint, bitmapPaint, selectionPaint, opponentSelectionPaint, textPaint;

    private PuzzleGame puzzleGame;
    private Chessboard chessboard;
    private PuzzleHintView puzzleHintView;
    private PuzzleFinishedListener puzzleFinishedListener;
    private ImageView playerTurnIcon;

    private int selectedFromRank = -1, selectedFromFile = -1, selectedToRank = -1, selectedToFile = -1;
    private int opponentFromRank = -1, opponentFromFile = -1, opponentToRank = -1, opponentToFile = -1;
    private boolean puzzleFinished = false;

    public ChessboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.bitmapManager = new ChessboardPieceManager(context);
    }

    private void initPaints() {
        lightBrownPaint = createPaint("#D2B48C");
        darkBrownPaint = createPaint("#8B4513");
        bitmapPaint = createBitmapPaint();
        selectionPaint = createSelectionPaint(false);
        opponentSelectionPaint = createSelectionPaint(true);
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

    private Paint createSelectionPaint(boolean isOpponent) {
        Paint paint = new Paint();
        if (chessboard.isPlayerWhite() && !isOpponent || !chessboard.isPlayerWhite() && isOpponent) {
            paint.setColor(Color.WHITE);
        } else {
            paint.setColor(Color.BLACK);
        }
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(STROKE_WIDTH);
        return paint;
    }

    private Paint createTextPaint() {
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(30);
        paint.setAntiAlias(true);
        return paint;
    }

    private void drawRectangle(Canvas canvas, int rank, int file, Paint paint) {
        if (rank == -1 || file == -1 || puzzleFinished) {
            return;
        }
        float tileSize = getTileSize();
        float halfStroke = STROKE_WIDTH / 2f;

        float left = file * tileSize + halfStroke;
        float top = rank * tileSize + halfStroke;
        canvas.drawRect(left, top, left + tileSize - STROKE_WIDTH, top + tileSize - STROKE_WIDTH, paint);
    }

    private void drawSelection(int fromRank, int fromFile, int toRank, int toFile, Canvas canvas, Paint paint) {
        drawRectangle(canvas, fromRank, fromFile, paint);
        drawRectangle(canvas, toRank, toFile, paint);
    }

    private void drawBoard(Canvas canvas) {
        float tileSize = getTileSize();
        for (int rank = 0; rank < BOARD_SIZE; rank++) {
            for (int file = 0; file < BOARD_SIZE; file++) {
                Paint paint = (rank + file) % 2 == 0 ? lightBrownPaint : darkBrownPaint;
                canvas.drawRect(file * tileSize, rank * tileSize, (file + 1) * tileSize, (rank + 1) * tileSize, paint);
            }
        }

        drawSelection(selectedFromRank, selectedFromFile, selectedToRank, selectedToFile, canvas, selectionPaint);
        drawSelection(opponentFromRank, opponentFromFile, opponentToRank, opponentToFile, canvas, opponentSelectionPaint);
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

    private void onPuzzleSolved(PuzzleGame solvedPuzzle) {
        puzzleFinished = true;
        makeText(R.string.correct_solution);
        puzzleFinishedListener.onPuzzleSolved(solvedPuzzle);
        postDelayed(() -> puzzleFinishedListener.onAfterPuzzleFinished(solvedPuzzle), NEXT_PUZZLE_DELAY);
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
        selectedFromRank = rank;
        selectedFromFile = file;
    }

    private void selectTargetSquare(int rank, int file) {
        selectedToRank = rank;
        selectedToFile = file;
    }

    private void removeSelection() {
        selectedFromRank = -1;
        selectedFromFile = -1;
        opponentFromRank = -1;
        opponentFromFile = -1;
        opponentToRank = -1;
        opponentToFile = -1;
        removeTargetSelection();
    }

    private void removeTargetSelection() {
        selectedToRank = -1;
        selectedToFile = -1;
        invalidate();
    }

    private void proposeMove(int rank, int file) {
        var proposedMove = chessboard.getProposedMove(selectedFromRank, selectedFromFile, rank, file);
        selectTargetSquare(rank, file);
        if (chessboard.isPromotionMove(selectedFromRank, selectedFromFile, rank, file)) {
            PromotionDialog.show(getContext(), bitmapManager, chessboard.isPlayerWhite(), getTileSize(), piece -> {
                var proposedPromotionMove = chessboard.getPromotionMove(selectedFromRank, selectedFromFile, rank, file, piece);
                handleMove(proposedPromotionMove);
            });
        } else {
            handleMove(proposedMove);
        }
    }

    private void handleMove(String move) {
        if (!chessboard.isMoveLegal(move)) {
            removeTargetSelection();
            return;
        }

        if (!chessboard.isMoveLeadingToMate(move) && !puzzleGame.isCorrectNextMove(move)) {
            makeText(R.string.wrong_solution);
            puzzleFinishedListener.onPuzzleNotSolved(puzzleGame);
            postDelayed(() -> puzzleFinishedListener.onAfterPuzzleFinished(puzzleGame), NEXT_PUZZLE_DELAY);
        } else {
            if (chessboard.isMoveLeadingToMate(move)) {
                doNextMove(move);
            } else {
                doNextMove(null);
            }
            if (puzzleGame.isSolutionFound()) {
                onPuzzleSolved(puzzleGame);
            } else {
                postDelayed(() -> this.doNextMove(null), MOVE_DELAY);
            }
        }
    }

    private void doNextMove(String nextMove) {
        var possibleNextMove = puzzleGame.getNextMove();
        if (isNull(nextMove) || nextMove.isEmpty()) {
            nextMove = possibleNextMove;
        }
        chessboard.doMove(nextMove);
        if (chessboard.isPlayersTurn()) {
            var moveCoordinates = chessboard.transformFenMove(nextMove);
            opponentFromRank = moveCoordinates[0];
            opponentFromFile = moveCoordinates[1];
            opponentToRank = moveCoordinates[2];
            opponentToFile = moveCoordinates[3];
        }
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

    int getSelectedFromFile() {
        return selectedFromFile;
    }

    int getSelectedFromRank() {
        return selectedFromRank;
    }

    void doFirstMove() {
        if (!puzzleGame.isStarted() && !chessboard.isPlayersTurn()) {
            doNextMove(null);
        }
    }

    public void puzzleHintClicked() {
        if (!isNull(chessboard) && chessboard.isPlayersTurn()) {
            puzzleHintView.showHint(chessboard.transformFenMove(puzzleGame.getNextMove(false)), getTileSize());
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
        initPaints();
        updatePlayerTurnIcon();
        removeSelection();
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
        if (event.getAction() != MotionEvent.ACTION_DOWN || !puzzleGame.isStarted() || puzzleFinished) {
            return false;
        }

        int tileSize = (int) getTileSize();
        int file = (int) (event.getX() / tileSize);
        int rank = (int) (event.getY() / tileSize);

        if (rank < 0 || rank >= BOARD_SIZE || file < 0 || file >= BOARD_SIZE) {
            return true;
        }

        var piece = chessboard.getPiece(rank, file);

        if (Chessboard.NONE_PIECE != piece && chessboard.isOwnPiece(piece)) {
            removeSelection();
            selectPiece(rank, file);
        } else if (selectedFromRank != -1 && selectedFromFile != -1) {
            proposeMove(rank, file);
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