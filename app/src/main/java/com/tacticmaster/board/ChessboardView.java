package com.tacticmaster.board;

import static com.tacticmaster.board.Chessboard.BOARD_SIZE;
import static java.util.Objects.isNull;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.tacticmaster.R;
import com.tacticmaster.puzzle.PuzzleGame;
import com.tacticmaster.settings.SettingsManager;
import com.tacticmaster.sound.SoundPlayer;

public class ChessboardView extends View implements PuzzleHintView.ViewChangedListener, ChessboardAnimator.Callbacks {

    public interface PuzzleFinishedListener {
        void onPuzzleSolved(PuzzleGame puzzle);

        void onPuzzleNotSolved(PuzzleGame puzzle);

        void onAfterPuzzleFinished(PuzzleGame puzzle);
    }


    private static final int NEXT_PUZZLE_DELAY = 3000;
    private static final int MOVE_DELAY = 1300;
    private static final int FIRST_MOVE_DELAY = 2000;
    private static final int LABEL_EDGE_MARGIN = 10;
    private static final float FILE_LABEL_CENTER_FACTOR = 1.9f;
    private static final float RANK_LABEL_CENTER_FACTOR = .4f;

    private ChessboardPieceManager bitmapManager;
    private final SettingsManager settingsManager;
    private ChessboardAnimator animator;

    private Paint lightBrownPaint, darkBrownPaint, bitmapPaint, shadowPaint, extrusionPaint, selectionPaint, opponentSelectionPaint, textPaint;
    private Paint bevelHighlightPaint, bevelShadowPaint;
    private float shadowOffset;
    private float bevelStroke;
    private float extrusionOffset;

    private PuzzleGame puzzleGame;
    private Chessboard chessboard;
    private PuzzleHintView puzzleHintView;
    private PuzzleFinishedListener puzzleFinishedListener;
    private ImageView playerTurnIcon;

    private float tileSize = 0f;

    private int selectedFromRank = -1, selectedFromFile = -1, selectedToRank = -1, selectedToFile = -1;
    private int opponentFromRank = -1, opponentFromFile = -1, opponentToRank = -1, opponentToFile = -1;
    private boolean puzzleFinished = false;

    private final PuzzleResultOverlay resultOverlay;

    // Tracked so they can be cancelled when the puzzle changes or the view detaches —
    // otherwise a delayed onAfterPuzzleFinished can fire against a stale puzzle and skip ahead.
    private Runnable pendingAfterPuzzleFinished;
    private Runnable pendingNextMove;
    private Runnable pendingFirstMove;

    public ChessboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.settingsManager = SettingsManager.getInstance(context);
        this.bitmapManager = new ChessboardPieceManager(context, settingsManager.getPieceSet());
        this.resultOverlay = new PuzzleResultOverlay(context, this::invalidate);
    }

    /**
     * Rebuilds the piece bitmaps from the given set and redraws. Called when the
     * user changes the piece set in settings. Recycles the previous bitmaps and
     * re-scales to the current tile size so the board updates immediately.
     */
    public void reloadPieces(String pieceSet) {
        ChessboardPieceManager previous = bitmapManager;
        bitmapManager = new ChessboardPieceManager(getContext(), pieceSet);
        int tileSize = (int) getTileSize();
        if (tileSize > 0) {
            bitmapManager.onSizeChanged(tileSize);
        }
        if (!isNull(previous)) {
            previous.recycleBitmaps();
        }
        invalidate();
    }

    private void initPaints() {
        lightBrownPaint = ChessboardPaintFactory.createSquarePaint("#D2B48C");
        darkBrownPaint = ChessboardPaintFactory.createSquarePaint("#8B4513");
        bitmapPaint = ChessboardPaintFactory.createBitmapPaint();
        extrusionPaint = ChessboardPaintFactory.createExtrusionPaint();
        bevelHighlightPaint = ChessboardPaintFactory.createBevelHighlightPaint();
        bevelShadowPaint = ChessboardPaintFactory.createBevelShadowPaint();
        selectionPaint = ChessboardPaintFactory.createSelectionPaint(chessboard.isPlayerWhite(), false);
        opponentSelectionPaint = ChessboardPaintFactory.createSelectionPaint(chessboard.isPlayerWhite(), true);
        textPaint = ChessboardPaintFactory.createTextPaint();

        // The factory leaves the bevel paints at stroke width 0. onSizeChanged sets
        // the real width, but setPuzzle re-runs initPaints on every puzzle without a
        // resize, so reapply the current stroke here to keep the bevel from collapsing
        // to a 1px hairline. Before the first layout bevelStroke is 0 and onSizeChanged
        // will set it.
        if (bevelStroke > 0) {
            bevelHighlightPaint.setStrokeWidth(bevelStroke);
            bevelShadowPaint.setStrokeWidth(bevelStroke);
        }
    }

    private void drawRectangle(Canvas canvas, int rank, int file, Paint paint) {
        if (rank == -1 || file == -1 || puzzleFinished) {
            return;
        }
        float tileSize = getTileSize();
        float halfStroke = ChessboardPaintFactory.STROKE_WIDTH / 2f;

        float left = file * tileSize + halfStroke;
        float top = rank * tileSize + halfStroke;
        canvas.drawRect(left, top, left + tileSize - ChessboardPaintFactory.STROKE_WIDTH, top + tileSize - ChessboardPaintFactory.STROKE_WIDTH, paint);
    }

    private void drawSelection(int fromRank, int fromFile, int toRank, int toFile, Canvas canvas, Paint paint) {
        drawRectangle(canvas, fromRank, fromFile, paint);
        drawRectangle(canvas, toRank, toFile, paint);
    }

    private void drawBoard(Canvas canvas) {
        float tileSize = getTileSize();
        float h = bevelStroke / 2f;
        for (int rank = 0; rank < BOARD_SIZE; rank++) {
            for (int file = 0; file < BOARD_SIZE; file++) {
                float l = file * tileSize;
                float t = rank * tileSize;
                float r = l + tileSize;
                float b = t + tileSize;

                Paint squarePaint = (rank + file) % 2 == 0 ? lightBrownPaint : darkBrownPaint;
                canvas.drawRect(l, t, r, b, squarePaint);

                // top and left edges — highlight
                canvas.drawLine(l, t + h, r, t + h, bevelHighlightPaint);
                canvas.drawLine(l + h, t, l + h, b, bevelHighlightPaint);
                // bottom and right edges — shadow
                canvas.drawLine(l, b - h, r, b - h, bevelShadowPaint);
                canvas.drawLine(r - h, t, r - h, b, bevelShadowPaint);
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

            canvas.drawText(fileLabel, index * tileSize + (tileSize / 2 - textPaint.measureText(fileLabel) / 2) * FILE_LABEL_CENTER_FACTOR, height - LABEL_EDGE_MARGIN, textPaint);
            canvas.drawText(rankLabel, LABEL_EDGE_MARGIN, index * tileSize + (tileSize / 2 + textPaint.getTextSize() / 2) * RANK_LABEL_CENTER_FACTOR, textPaint);
        }
    }

    private void drawPieceWithShadow(Canvas canvas, char piece, Bitmap bitmap, float left, float top) {
        Bitmap alpha = bitmapManager.getAlphaBitmap(piece);
        if (!isNull(alpha)) {
            for (int i = ChessboardPaintFactory.EXTRUSION_LAYERS; i >= 1; i--) {
                float d = extrusionOffset * i;
                canvas.drawBitmap(alpha, left + d * 0.5f, top + d, extrusionPaint);
            }
        }
        canvas.drawBitmap(bitmap, left + shadowOffset, top + shadowOffset, shadowPaint);
        canvas.drawBitmap(bitmap, left, top, bitmapPaint);
    }

    private void drawPieces(Canvas canvas) {
        float tileSize = getTileSize();

        for (int rank = 0; rank < BOARD_SIZE; rank++) {
            for (int file = 0; file < BOARD_SIZE; file++) {
                var currentPiece = chessboard.getPiece(rank, file);
                if (Chessboard.NONE_PIECE == currentPiece) continue;

                if (animator.isAnimating() && rank == animator.getAnimFromRank() && file == animator.getAnimFromFile()) {
                    continue;
                }

                Bitmap pieceBitmap = bitmapManager.getPieceBitmap(currentPiece);
                if (isNull(pieceBitmap)) continue;

                float left = file * tileSize + puzzleHintView.getShakeOffset(rank, file);
                float top = rank * tileSize;
                drawPieceWithShadow(canvas, currentPiece, pieceBitmap, left, top);
            }
        }

        if (animator.isAnimating() && !isNull(animator.getAnimPieceBitmap())) {

            float fromLeft = animator.getAnimFromFile() * tileSize + puzzleHintView.getShakeOffset(animator.getAnimFromRank(), animator.getAnimFromFile());
            float fromTop = animator.getAnimFromRank() * tileSize;
            float toLeft = animator.getAnimToFile() * tileSize + puzzleHintView.getShakeOffset(animator.getAnimToRank(), animator.getAnimToFile());
            float toTop = animator.getAnimToRank() * tileSize;

            float curLeft = fromLeft + (toLeft - fromLeft) * animator.getAnimProgress();
            float curTop = fromTop + (toTop - fromTop) * animator.getAnimProgress();

            drawPieceWithShadow(canvas, animator.getAnimPiece(), animator.getAnimPieceBitmap(), curLeft, curTop);
        }
    }

    @Override
    public void onMoveCompleted(String move, boolean isCapture, boolean isPlayersTurn, int[] coords) {
        SoundPlayer.getInstance().playMoveSound(getContext(), isCapture);
        if (isPlayersTurn) {
            opponentFromRank = coords[0];
            opponentFromFile = coords[1];
            opponentToRank = coords[2];
            opponentToFile = coords[3];
        }
        invalidate();
    }

    @Override
    public void onAnimationFrame() {
        postInvalidateOnAnimation();
    }


    /**
     * Shows a brief centered Toast for transient controller messages such as
     * "no more puzzles" or "invalid puzzle id".
     */
    public void makeText(int resourceId) {
        var toast = Toast.makeText(getContext(), resourceId, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    private void onPuzzleSolved(PuzzleGame solvedPuzzle) {
        puzzleFinished = true;
        resultOverlay.show(true);
        puzzleFinishedListener.onPuzzleSolved(solvedPuzzle);
        scheduleAfterPuzzleFinished(solvedPuzzle);
    }

    private void scheduleAfterPuzzleFinished(PuzzleGame puzzle) {
        if (!isNull(pendingAfterPuzzleFinished)) {
            removeCallbacks(pendingAfterPuzzleFinished);
        }
        pendingAfterPuzzleFinished = () -> puzzleFinishedListener.onAfterPuzzleFinished(puzzle);
        postDelayed(pendingAfterPuzzleFinished, NEXT_PUZZLE_DELAY);
    }

    private void cancelPendingCallbacks() {
        if (!isNull(pendingAfterPuzzleFinished)) {
            removeCallbacks(pendingAfterPuzzleFinished);
            pendingAfterPuzzleFinished = null;
        }
        if (!isNull(pendingNextMove)) {
            removeCallbacks(pendingNextMove);
            pendingNextMove = null;
        }
        if (!isNull(pendingFirstMove)) {
            removeCallbacks(pendingFirstMove);
            pendingFirstMove = null;
        }
        resultOverlay.cancel();
    }

    private float getTileSize() {
        return tileSize;
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

        boolean leadsToMate = chessboard.isMoveLeadingToMate(move);
        if (!leadsToMate && !puzzleGame.isCorrectNextMove(move)) {
            resultOverlay.show(false);
            puzzleFinishedListener.onPuzzleNotSolved(puzzleGame);
            scheduleAfterPuzzleFinished(puzzleGame);
        } else {
            doNextMove(leadsToMate ? move : null);
            if (puzzleGame.isSolutionFound()) {
                onPuzzleSolved(puzzleGame);
            } else {
                if (!isNull(pendingNextMove)) {
                    removeCallbacks(pendingNextMove);
                }
                pendingNextMove = () -> this.doNextMove(null);
                postDelayed(pendingNextMove, MOVE_DELAY);
            }
        }
    }

    private void doNextMove(String nextMove) {
        if (animator.isAnimating()) return;
        var possibleNextMove = puzzleGame.getNextMove();
        if (possibleNextMove.isEmpty()) return;

        if (isNull(nextMove) || nextMove.isEmpty()) {
            nextMove = possibleNextMove;
        }
        animator.startMove(nextMove);
    }


    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        tileSize = Math.min(width, height) / (float) BOARD_SIZE;
        shadowPaint = ChessboardPaintFactory.createShadowPaint(tileSize);
        shadowOffset = tileSize * ChessboardPaintFactory.SHADOW_OFFSET_RATIO;
        bevelStroke = tileSize * ChessboardPaintFactory.BEVEL_RATIO;
        extrusionOffset = tileSize * ChessboardPaintFactory.EXTRUSION_OFFSET_RATIO;
        bevelHighlightPaint.setStrokeWidth(bevelStroke);
        bevelShadowPaint.setStrokeWidth(bevelStroke);
        bitmapManager.onSizeChanged((int) tileSize);
        resultOverlay.onSizeChanged();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        drawBoard(canvas);
        drawLabels(canvas);
        drawPieces(canvas);
        resultOverlay.draw(canvas, getTileSize() * BOARD_SIZE);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        cancelPendingCallbacks();
        bitmapManager.recycleBitmaps();
        resultOverlay.recycle();
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
        cancelPendingCallbacks();
        puzzleFinished = false;
        this.puzzleGame = puzzle;
        puzzleGame.reset();
        this.chessboard = new Chessboard(puzzleGame.fen());
        initPaints();
        int duration = settingsManager.areAnimationsEnabled() ? settingsManager.getAnimationSpeed() : 0;
        animator = new ChessboardAnimator(chessboard, bitmapManager, this, duration);
        updatePlayerTurnIcon();
        removeSelection();
        puzzleHintView.resetHintFirstClick();
        invalidate();
        pendingFirstMove = this::doFirstMove;
        postDelayed(pendingFirstMove, FIRST_MOVE_DELAY);
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