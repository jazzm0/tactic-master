package com.tacticmaster.board;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.tacticmaster.R;

public class ChessboardPieceManager {

    private Bitmap whiteKing, blackKing, whiteQueen, blackQueen, whiteRook, blackRook, whiteBishop, blackBishop, whiteKnight, blackKnight, whitePawn, blackPawn;
    private Bitmap scaledWhiteKing, scaledBlackKing, scaledWhiteQueen, scaledBlackQueen, scaledWhiteRook, scaledBlackRook, scaledWhiteBishop, scaledBlackBishop, scaledWhiteKnight, scaledBlackKnight, scaledWhitePawn, scaledBlackPawn;
    private final Context context;

    public ChessboardPieceManager(Context context) {
        this.context = requireNonNull(context);
        whiteKing = loadBitmap(R.drawable.wk, "whiteKing");
        blackKing = loadBitmap(R.drawable.bk, "blackKing");
        whiteQueen = loadBitmap(R.drawable.wq, "whiteQueen");
        blackQueen = loadBitmap(R.drawable.bq, "blackQueen");
        whiteRook = loadBitmap(R.drawable.wr, "whiteRook");
        blackRook = loadBitmap(R.drawable.br, "blackRook");
        whiteBishop = loadBitmap(R.drawable.wb, "whiteBishop");
        blackBishop = loadBitmap(R.drawable.bb, "blackBishop");
        whiteKnight = loadBitmap(R.drawable.wn, "whiteKnight");
        blackKnight = loadBitmap(R.drawable.bn, "blackKnight");
        whitePawn = loadBitmap(R.drawable.wp, "whitePawn");
        blackPawn = loadBitmap(R.drawable.bp, "blackPawn");
    }

    private Bitmap loadBitmap(int resId, String pieceName) {
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resId);
        if (isNull(bitmap)) {
            throw new IllegalStateException("Failed to load bitmap for " + pieceName + " (resource ID: " + resId + ")");
        }
        return bitmap;
    }

    public Bitmap getPieceBitmap(char piece) {
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

    public void onSizeChanged(int tileSize) {
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
    }

    public void recycleBitmaps() {
        for (Bitmap bitmap : new Bitmap[]{whiteKing, blackKing, whiteQueen, blackQueen, whiteRook, blackRook,
                whiteBishop, blackBishop, whiteKnight, blackKnight, whitePawn, blackPawn,
                scaledWhiteKing, scaledBlackKing, scaledWhiteQueen, scaledBlackQueen, scaledWhiteRook, scaledBlackRook,
                scaledWhiteBishop, scaledBlackBishop, scaledWhiteKnight, scaledBlackKnight, scaledWhitePawn, scaledBlackPawn}) {
            if (!isNull(bitmap) && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
        }
        whiteKing = blackKing = whiteQueen = blackQueen
                = whiteRook = blackRook = whiteBishop
                = blackBishop = whiteKnight = blackKnight
                = whitePawn = blackPawn = scaledWhiteKing
                = scaledBlackKing = scaledWhiteQueen = scaledBlackQueen
                = scaledWhiteRook = scaledBlackRook = scaledWhiteBishop
                = scaledBlackBishop = scaledWhiteKnight = scaledBlackKnight
                = scaledWhitePawn = scaledBlackPawn = null;
    }
}
