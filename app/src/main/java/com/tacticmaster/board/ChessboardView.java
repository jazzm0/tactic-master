package com.tacticmaster.board;

import static com.tacticmaster.board.Chessboard.BOARD_SIZE;
import static java.util.Objects.isNull;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;

import com.tacticmaster.R;
import com.tacticmaster.puzzle.PuzzleGame;
import com.tacticmaster.settings.SettingsManager;
import com.tacticmaster.sound.SoundPlayer;

public class ChessboardView extends View implements PuzzleHintView.ViewChangedListener {

    public interface PuzzleFinishedListener {
        void onPuzzleSolved(PuzzleGame puzzle);

        void onPuzzleNotSolved(PuzzleGame puzzle);

        void onAfterPuzzleFinished(PuzzleGame puzzle);
    }


    private static final int NEXT_PUZZLE_DELAY = 3000;
    private static final int MOVE_DELAY = 1300;
    private static final int FIRST_MOVE_DELAY = 2000;
    private static final int STROKE_WIDTH = 8;
    private static final int LABEL_TEXT_SIZE = 30;
    private static final int LABEL_EDGE_MARGIN = 10;
    private static final float FILE_LABEL_CENTER_FACTOR = 1.9f;
    private static final float RANK_LABEL_CENTER_FACTOR = .4f;

    private static final int RESULT_ANIM_DURATION = 1600;
    private static final float RESULT_FADE_IN_END = 0.18f;
    private static final float RESULT_FADE_OUT_START = 0.75f;
    private static final int RESULT_TINT_MAX_ALPHA = 90;
    private static final int RESULT_ICON_MAX_ALPHA = 255;
    private static final float RESULT_ICON_BOARD_FRACTION = 0.5f;
    private static final int RESULT_SOLVED_COLOR = 0xFF4CAF50;
    private static final int RESULT_UNSOLVED_COLOR = 0xFFE53935;

    private ChessboardPieceManager bitmapManager;
    private final SettingsManager settingsManager;

    private Paint lightBrownPaint, darkBrownPaint, bitmapPaint, selectionPaint, opponentSelectionPaint, textPaint, solvedTintPaint, unsolvedTintPaint;

    private PuzzleGame puzzleGame;
    private Chessboard chessboard;
    private PuzzleHintView puzzleHintView;
    private PuzzleFinishedListener puzzleFinishedListener;
    private ImageView playerTurnIcon;

    private boolean isAnimating = false;
    private float animProgress = 0f;
    private float tileSize = 0f;
    private int animFromRank = -1, animFromFile = -1, animToRank = -1, animToFile = -1;
    private Bitmap animPieceBitmap = null;

    private int selectedFromRank = -1, selectedFromFile = -1, selectedToRank = -1, selectedToFile = -1;
    private int opponentFromRank = -1, opponentFromFile = -1, opponentToRank = -1, opponentToFile = -1;
    private boolean puzzleFinished = false;

    private boolean resultShowing = false;
    private boolean resultSolved = false;
    private float resultAnimProgress = 0f;
    private ValueAnimator resultAnimator;

    private Bitmap solvedIconBitmap, unsolvedIconBitmap;
    private int resultIconSize = -1;

    // Tracked so they can be cancelled when the puzzle changes or the view detaches —
    // otherwise a delayed onAfterPuzzleFinished can fire against a stale puzzle and skip ahead.
    private Runnable pendingAfterPuzzleFinished;
    private Runnable pendingNextMove;
    private Runnable pendingFirstMove;

    public ChessboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.settingsManager = SettingsManager.getInstance(context);
        this.bitmapManager = new ChessboardPieceManager(context, settingsManager.getPieceSet());
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
        lightBrownPaint = createPaint("#D2B48C");
        darkBrownPaint = createPaint("#8B4513");
        bitmapPaint = createBitmapPaint();
        selectionPaint = createSelectionPaint(false);
        opponentSelectionPaint = createSelectionPaint(true);
        textPaint = createTextPaint();
        solvedTintPaint = createTintPaint(RESULT_SOLVED_COLOR);
        unsolvedTintPaint = createTintPaint(RESULT_UNSOLVED_COLOR);
    }

    private Paint createTintPaint(int color) {
        Paint paint = new Paint();
        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);
        return paint;
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
        paint.setTextSize(LABEL_TEXT_SIZE);
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

            canvas.drawText(fileLabel, index * tileSize + (tileSize / 2 - textPaint.measureText(fileLabel) / 2) * FILE_LABEL_CENTER_FACTOR, height - LABEL_EDGE_MARGIN, textPaint);
            canvas.drawText(rankLabel, LABEL_EDGE_MARGIN, index * tileSize + (tileSize / 2 + textPaint.getTextSize() / 2) * RANK_LABEL_CENTER_FACTOR, textPaint);
        }
    }

    private void drawPieces(Canvas canvas) {
        float tileSize = getTileSize();

        for (int rank = 0; rank < BOARD_SIZE; rank++) {
            for (int file = 0; file < BOARD_SIZE; file++) {
                var currentPiece = chessboard.getPiece(rank, file);
                if (Chessboard.NONE_PIECE == currentPiece) continue;

                if (isAnimating && rank == animFromRank && file == animFromFile) {
                    continue;
                }

                Bitmap pieceBitmap = bitmapManager.getPieceBitmap(currentPiece);
                if (isNull(pieceBitmap)) continue;

                float left = file * tileSize + puzzleHintView.getShakeOffset(rank, file);
                float top = rank * tileSize;
                canvas.drawBitmap(pieceBitmap, left, top, bitmapPaint);
            }
        }

        if (isAnimating && !isNull(animPieceBitmap)) {

            float fromLeft = animFromFile * tileSize + puzzleHintView.getShakeOffset(animFromRank, animFromFile);
            float fromTop = animFromRank * tileSize;
            float toLeft = animToFile * tileSize + puzzleHintView.getShakeOffset(animToRank, animToFile);
            float toTop = animToRank * tileSize;

            float curLeft = fromLeft + (toLeft - fromLeft) * animProgress;
            float curTop = fromTop + (toTop - fromTop) * animProgress;

            canvas.drawBitmap(animPieceBitmap, curLeft, curTop, bitmapPaint);
        }
    }

    private void animateMove(String nextMove) {
        int[] coords = chessboard.transformFenMove(nextMove);
        animFromRank = coords[0];
        animFromFile = coords[1];
        animToRank = coords[2];
        animToFile = coords[3];

        char movingPiece = chessboard.getPiece(animFromRank, animFromFile);
        animPieceBitmap = bitmapManager.getPieceBitmap(movingPiece);

        boolean animationsEnabled = settingsManager.areAnimationsEnabled();
        int animationDuration = animationsEnabled ? settingsManager.getAnimationSpeed() : 0;

        if (!animationsEnabled || animationDuration == 0) {
            // Skip animation - execute move immediately
            completeMove(nextMove);
            return;
        }

        isAnimating = true;
        animProgress = 0f;

        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(animationDuration);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(a -> {
            animProgress = (float) a.getAnimatedValue();
            postInvalidateOnAnimation();
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                completeMove(nextMove);
            }
        });
        animator.start();
    }

    /**
     * Applies {@code nextMove} to the board and refreshes state: plays the move
     * sound, clears the in-flight animation, and — when it is now the player's
     * turn — records the opponent's move as the highlighted selection. Shared by
     * the animated and skip-animation paths in {@link #animateMove(String)}.
     */
    private void completeMove(String nextMove) {
        boolean isCaptureMove = chessboard.isCaptureMove(nextMove);
        chessboard.doMove(nextMove);
        isAnimating = false;
        animPieceBitmap = null;
        SoundPlayer.getInstance().playMoveSound(getContext(), isCaptureMove);

        if (chessboard.isPlayersTurn()) {
            int[] coords = chessboard.transformFenMove(nextMove);
            opponentFromRank = coords[0];
            opponentFromFile = coords[1];
            opponentToRank = coords[2];
            opponentToFile = coords[3];
        }

        invalidate();
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

    /**
     * Plays the solved/unsolved feedback: a translucent board tint plus a
     * centered check ({@code solved}) or X icon that fades in, holds, then fades
     * out. Runs entirely within {@link #NEXT_PUZZLE_DELAY} so it clears before the
     * next puzzle loads. Cancelled by {@link #cancelPendingCallbacks()} on puzzle
     * change so a stale overlay never bleeds onto a new puzzle.
     */
    private void startResultAnimation(boolean solved) {
        resultSolved = solved;
        resultShowing = true;
        resultAnimProgress = 0f;

        if (!isNull(resultAnimator)) {
            resultAnimator.cancel();
        }
        resultAnimator = ValueAnimator.ofFloat(0f, 1f);
        resultAnimator.setDuration(RESULT_ANIM_DURATION);
        resultAnimator.setInterpolator(new LinearInterpolator());
        resultAnimator.addUpdateListener(a -> {
            resultAnimProgress = (float) a.getAnimatedValue();
            invalidate();
        });
        resultAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                resultShowing = false;
                invalidate();
            }
        });
        resultAnimator.start();
    }

    /**
     * Maps the linear 0..1 progress to an alpha multiplier: ramps up over the
     * fade-in window, holds at full, then ramps down over the fade-out window.
     */
    private float resultFadeFactor() {
        if (resultAnimProgress < RESULT_FADE_IN_END) {
            return resultAnimProgress / RESULT_FADE_IN_END;
        }
        if (resultAnimProgress > RESULT_FADE_OUT_START) {
            return 1f - (resultAnimProgress - RESULT_FADE_OUT_START) / (1f - RESULT_FADE_OUT_START);
        }
        return 1f;
    }

    private void drawResultOverlay(Canvas canvas) {
        if (!resultShowing) {
            return;
        }
        float fade = resultFadeFactor();
        float boardSize = getTileSize() * BOARD_SIZE;

        Paint tintPaint = resultSolved ? solvedTintPaint : unsolvedTintPaint;
        tintPaint.setAlpha((int) (RESULT_TINT_MAX_ALPHA * fade));
        canvas.drawRect(0, 0, boardSize, boardSize, tintPaint);

        Bitmap icon = getResultIcon((int) (boardSize * RESULT_ICON_BOARD_FRACTION));
        if (!isNull(icon)) {
            float left = (boardSize - icon.getWidth()) / 2f;
            float top = (boardSize - icon.getHeight()) / 2f;
            bitmapPaint.setAlpha((int) (RESULT_ICON_MAX_ALPHA * fade));
            canvas.drawBitmap(icon, left, top, bitmapPaint);
            bitmapPaint.setAlpha(255);
        }
    }

    /**
     * Lazily renders (and caches) the current result icon tinted to the
     * solved/unsolved color at {@code size} px. Re-renders when the requested
     * size changes (e.g. board resize/rotation).
     */
    private Bitmap getResultIcon(int size) {
        if (size <= 0) {
            return null;
        }
        if (size != resultIconSize) {
            recycleResultIcons();
            resultIconSize = size;
        }
        if (resultSolved) {
            if (isNull(solvedIconBitmap)) {
                solvedIconBitmap = renderIcon(R.drawable.ic_solved, RESULT_SOLVED_COLOR, size);
            }
            return solvedIconBitmap;
        }
        if (isNull(unsolvedIconBitmap)) {
            unsolvedIconBitmap = renderIcon(R.drawable.ic_unsolved, RESULT_UNSOLVED_COLOR, size);
        }
        return unsolvedIconBitmap;
    }

    private Bitmap renderIcon(@DrawableRes int drawableRes, int tintColor, int size) {
        Drawable drawable = AppCompatResources.getDrawable(getContext(), drawableRes);
        if (isNull(drawable)) {
            return null;
        }
        drawable = drawable.mutate();
        drawable.setColorFilter(tintColor, PorterDuff.Mode.SRC_IN);
        drawable.setBounds(0, 0, size, size);
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        drawable.draw(new Canvas(bitmap));
        return bitmap;
    }

    private void recycleResultIcons() {
        if (!isNull(solvedIconBitmap)) {
            solvedIconBitmap.recycle();
            solvedIconBitmap = null;
        }
        if (!isNull(unsolvedIconBitmap)) {
            unsolvedIconBitmap.recycle();
            unsolvedIconBitmap = null;
        }
    }

    private void onPuzzleSolved(PuzzleGame solvedPuzzle) {
        puzzleFinished = true;
        startResultAnimation(true);
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
        if (!isNull(resultAnimator)) {
            resultAnimator.cancel();
            resultAnimator = null;
        }
        resultShowing = false;
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
            startResultAnimation(false);
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
        if (isAnimating) return;
        var possibleNextMove = puzzleGame.getNextMove();
        if (possibleNextMove.isEmpty()) return;

        if (isNull(nextMove) || nextMove.isEmpty()) {
            nextMove = possibleNextMove;
        }
        animateMove(nextMove);
    }


    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        tileSize = Math.min(width, height) / (float) BOARD_SIZE;
        bitmapManager.onSizeChanged((int) tileSize);
        // Drop cached result icons so they re-render at the new board size.
        recycleResultIcons();
        resultIconSize = -1;
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        drawBoard(canvas);
        drawLabels(canvas);
        drawPieces(canvas);
        drawResultOverlay(canvas);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        cancelPendingCallbacks();
        bitmapManager.recycleBitmaps();
        recycleResultIcons();
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