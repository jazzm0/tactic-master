package com.tacticmaster.board;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.view.animation.LinearInterpolator;

class ChessboardAnimator {

    interface Callbacks {
        void onMoveCompleted(String move, boolean isCapture, boolean isPlayersTurn, int[] coords);

        void onAnimationFrame();
    }

    interface AnimatorStarter {
        void start(int duration, Runnable onFrame, Runnable onEnd);
    }

    private final Chessboard chessboard;
    private final ChessboardPieceManager pieceManager;
    private final Callbacks callbacks;
    private final int animationDuration;
    private final AnimatorStarter animatorStarter;

    private boolean isAnimating = false;
    private float animProgress = 0f;
    private int animFromRank = -1, animFromFile = -1, animToRank = -1, animToFile = -1;
    private char animPiece = 0;
    private Bitmap animPieceBitmap = null;

    ChessboardAnimator(Chessboard chessboard, ChessboardPieceManager pieceManager,
                       Callbacks callbacks, int animationDuration) {
        this(chessboard, pieceManager, callbacks, animationDuration, null);
    }

    ChessboardAnimator(Chessboard chessboard, ChessboardPieceManager pieceManager,
                       Callbacks callbacks, int animationDuration, AnimatorStarter animatorStarter) {
        this.chessboard = chessboard;
        this.pieceManager = pieceManager;
        this.callbacks = callbacks;
        this.animationDuration = animationDuration;
        this.animatorStarter = animatorStarter;
    }

    void startMove(String move) {
        if (isAnimating) return;

        int[] coords = chessboard.transformFenMove(move);
        animFromRank = coords[0];
        animFromFile = coords[1];
        animToRank = coords[2];
        animToFile = coords[3];

        char movingPiece = chessboard.getPiece(animFromRank, animFromFile);
        animPiece = movingPiece;
        animPieceBitmap = pieceManager.getPieceBitmap(movingPiece);

        if (animationDuration == 0) {
            completeMove(move);
            return;
        }

        isAnimating = true;
        animProgress = 0f;
        if (animatorStarter != null) {
            animatorStarter.start(animationDuration, callbacks::onAnimationFrame, () -> completeMove(move));
        } else {
            startValueAnimator(move);
        }
    }

    private void startValueAnimator(String move) {
        ValueAnimator va = ValueAnimator.ofFloat(0f, 1f);
        va.setDuration(animationDuration);
        va.setInterpolator(new LinearInterpolator());
        va.addUpdateListener(a -> {
            animProgress = (float) a.getAnimatedValue();
            callbacks.onAnimationFrame();
        });
        va.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                completeMove(move);
            }
        });
        va.start();
    }

    private void completeMove(String move) {
        boolean isCapture = chessboard.isCaptureMove(move);
        chessboard.doMove(move);
        isAnimating = false;
        animPieceBitmap = null;
        int[] coords = chessboard.transformFenMove(move);
        callbacks.onMoveCompleted(move, isCapture, chessboard.isPlayersTurn(), coords);
    }

    boolean isAnimating() {
        return isAnimating;
    }

    float getAnimProgress() {
        return animProgress;
    }

    int getAnimFromRank() {
        return animFromRank;
    }

    int getAnimFromFile() {
        return animFromFile;
    }

    int getAnimToRank() {
        return animToRank;
    }

    int getAnimToFile() {
        return animToFile;
    }

    char getAnimPiece() {
        return animPiece;
    }

    Bitmap getAnimPieceBitmap() {
        return animPieceBitmap;
    }
}
