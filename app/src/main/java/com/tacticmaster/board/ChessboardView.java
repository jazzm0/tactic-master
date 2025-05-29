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

import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;
import com.tacticmaster.R;
import com.tacticmaster.puzzle.Puzzle;
import com.tacticmaster.puzzle.PuzzleGame;

import java.util.Arrays;

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
    private PuzzleGame puzzleGame;
    private final Chessboard chessboard;
    private PuzzleHintView puzzleHintView;
    private PuzzleFinishedListener puzzleFinishedListener;
    private ImageView playerTurnIcon;

    private boolean boardFlipped = false;

    private Side playerSide;
    private int selectedRow = -1, selectedColumn = -1;
    private boolean puzzleFinished = false;

    public ChessboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.bitmapManager = new ChessboardPieceManager(context);
        this.chessboard = new Chessboard();
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
        if (selectedRow != -1 && selectedColumn != -1 && !puzzleFinished) {
            float left = selectedColumn * tileSize;
            float top = selectedRow * tileSize;
            canvas.drawRect(left, top, left + tileSize, top + tileSize, selectionPaint);
        }
    }

    private void drawLabels(Canvas canvas) {
        float tileSize = getTileSize();
        int height = getHeight();
        for (int index = 0; index < BOARD_SIZE; index++) {
            String columnLabel = boardFlipped ? String.valueOf((char) ('h' - index)) : String.valueOf((char) ('a' + index));
            String rowLabel = boardFlipped ? String.valueOf(index + 1) : String.valueOf(BOARD_SIZE - index);

            canvas.drawText(columnLabel, index * tileSize + (tileSize / 2 - textPaint.measureText(columnLabel) / 2) * 1.9f, height - 10, textPaint);
            canvas.drawText(rowLabel, 10, index * tileSize + (tileSize / 2 + textPaint.getTextSize() / 2) * .4f, textPaint);
        }
    }

    private void drawPieces(Canvas canvas) {
        float tileSize = getTileSize();
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int column = 0; column < BOARD_SIZE; column++) {
                Piece currentPiece = chessboard.getPiece(squareAt(row, column));
                if (!Piece.NONE.equals(currentPiece)) {
                    Bitmap pieceBitmap = bitmapManager.getPieceBitmap(currentPiece.getFenSymbol().charAt(0));
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

    private void onPuzzleSolved() {
        puzzleFinished = true;
        makeText(R.string.correct_solution);
        Puzzle currentPuzzle = puzzle;
        postDelayed(() -> puzzleFinishedListener.onPuzzleSolved(currentPuzzle), NEXT_PUZZLE_DELAY);
    }

    private float getTileSize() {
        return Math.min(getWidth(), getHeight()) / (float) BOARD_SIZE;
    }

    private void updatePlayerTurnIcon() {
        if (playerSide == Side.WHITE) {
            playerTurnIcon.setImageResource(R.drawable.ic_white_turn);
        } else {
            playerTurnIcon.setImageResource(R.drawable.ic_black_turn);
        }
    }

    private void selectPiece(int row, int column) {
        selectedRow = row;
        selectedColumn = column;
    }

    private void unselectPiece() {
        selectedRow = -1;
        selectedColumn = -1;
    }

    private void proposeMove(int row, int column) {
        Move proposedMove = new Move(squareAt(selectedRow, selectedColumn), squareAt(row, column));
        if (chessboard.isPromotionMove(proposedMove)) {
            PromotionDialog.show(getContext(), bitmapManager, playerSide == Side.WHITE, getTileSize(), piece -> {
                Move proposedPromotionMove = new Move(squareAt(selectedRow, selectedColumn), squareAt(row, column), Piece.fromFenSymbol(Character.toString(piece)));
                handleMove(proposedPromotionMove);
            });
        } else {
            handleMove(proposedMove);
        }
    }

    private void handleMove(Move proposedMove) {
        unselectPiece();
        if (!chessboard.isMoveLeadingToMate(proposedMove) && !puzzleGame.isCorrectNextMove(proposedMove.toString())) {
            makeText(R.string.wrong_solution);
            postDelayed(() -> puzzleFinishedListener.onPuzzleNotSolved(this.puzzle), NEXT_PUZZLE_DELAY);
        } else {
            doNextMove();
            if (puzzleGame.isSolutionFound()) {
                onPuzzleSolved();
            } else {
                postDelayed(this::doNextMove, 1300);
            }
        }
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

    int getSelectedColumn() {
        return selectedColumn;
    }

    int getSelectedRow() {
        return selectedRow;
    }

    public void puzzleHintClicked() {
        if (!isNull(chessboard) && chessboard.getSideToMove() == playerSide) {
            puzzleHintView.showHint(transformMove(puzzleGame.getNextMove(false)), getTileSize());
        }
    }

    public void setPuzzleHintView(PuzzleHintView puzzleHintView) {
        this.puzzleHintView = puzzleHintView;
        puzzleHintView.setHintPathListener(this);
    }

    public void setPlayerTurnIcon(ImageView playerTurnIcon) {
        this.playerTurnIcon = playerTurnIcon;
    }

    public void setPuzzle(Puzzle puzzle) {
        puzzleFinished = false;
        this.puzzle = puzzle;
        this.puzzleGame = new PuzzleGame(puzzle);
        puzzleGame.reset();
        chessboard.loadFromFen(puzzleGame.fen());
        playerSide = chessboard.getSideToMove() == Side.WHITE ? Side.BLACK : Side.WHITE;
        boardFlipped = playerSide == Side.BLACK;
        updatePlayerTurnIcon();
        unselectPiece();
        puzzleHintView.resetHintFirstClick();
        invalidate();
        postDelayed(this::doFirstMove, 2000);
    }

    public void doFirstMove() {
        if (!puzzleGame.isStarted() && chessboard.getSideToMove() != playerSide) {
            doNextMove();
        }
    }

    public void doNextMove() {
        chessboard.doMove(new Move(puzzleGame.getNextMove(), chessboard.getSideToMove()));
        invalidate();
    }

    public void setPuzzleSolvedListener(PuzzleFinishedListener listener) {
        this.puzzleFinishedListener = listener;
    }

    private Square squareAt(int row, int column) {
        return Square.squareAt(transformFlippedCoordinate(BOARD_SIZE - row - 1) * BOARD_SIZE + transformFlippedCoordinate(column));
    }

    private int transformFlippedCoordinate(int i) {
        return boardFlipped ? 7 - i : i;
    }

    private int[] transformMove(String fenMove) {
        Move move = new Move(fenMove, chessboard.getSideToMove());
        int[] moveCoordinates = new int[]{BOARD_SIZE - move.getFrom().getRank().ordinal() - 1, move.getFrom().getFile().ordinal(), BOARD_SIZE - move.getTo().getRank().ordinal() - 1, move.getTo().getFile().ordinal()};
        return boardFlipped ? Arrays.stream(moveCoordinates).map(this::transformFlippedCoordinate).toArray() : moveCoordinates;
    }

    @Override
    public void onViewChanged() {
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN && puzzleGame.isStarted() && !puzzleFinished) {
            int tileSize = (int) getTileSize();

            int column = (int) (event.getX() / tileSize);
            int row = (int) (event.getY() / tileSize);

            if (row < 0 || row >= BOARD_SIZE || column < 0 || column >= BOARD_SIZE) {
                return true;
            }

            Piece piece = chessboard.getPiece(squareAt(row, column));

            if (!Piece.NONE.equals(piece) && piece.getPieceSide() == playerSide) {
                selectPiece(row, column);
            } else if (selectedRow != -1 && selectedColumn != -1) {
                proposeMove(row, column);
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