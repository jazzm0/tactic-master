package com.tacticmaster.board;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.tacticmaster.R;

import java.util.HashMap;
import java.util.Map;

public class ChessboardPieceManager {

    private final Map<String, Bitmap> bitmaps = new HashMap<>();
    private final Map<String, Bitmap> scaledBitmaps = new HashMap<>();
    private final Context context;

    public ChessboardPieceManager(Context context) {
        this.context = requireNonNull(context);
        loadBitmaps();
    }

    private void loadBitmaps() {
        bitmaps.put("whiteKing", loadBitmap(R.drawable.wk));
        bitmaps.put("blackKing", loadBitmap(R.drawable.bk));
        bitmaps.put("whiteQueen", loadBitmap(R.drawable.wq));
        bitmaps.put("blackQueen", loadBitmap(R.drawable.bq));
        bitmaps.put("whiteRook", loadBitmap(R.drawable.wr));
        bitmaps.put("blackRook", loadBitmap(R.drawable.br));
        bitmaps.put("whiteBishop", loadBitmap(R.drawable.wb));
        bitmaps.put("blackBishop", loadBitmap(R.drawable.bb));
        bitmaps.put("whiteKnight", loadBitmap(R.drawable.wn));
        bitmaps.put("blackKnight", loadBitmap(R.drawable.bn));
        bitmaps.put("whitePawn", loadBitmap(R.drawable.wp));
        bitmaps.put("blackPawn", loadBitmap(R.drawable.bp));
    }

    private Bitmap loadBitmap(int resId) {
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resId);
        if (isNull(bitmap)) {
            throw new IllegalStateException("Failed to load bitmap (resource ID: " + resId + ")");
        }
        return bitmap;
    }

    public Bitmap getPieceBitmap(char piece) {
        return switch (piece) {
            case 'K' -> scaledBitmaps.get("whiteKing");
            case 'k' -> scaledBitmaps.get("blackKing");
            case 'Q' -> scaledBitmaps.get("whiteQueen");
            case 'q' -> scaledBitmaps.get("blackQueen");
            case 'R' -> scaledBitmaps.get("whiteRook");
            case 'r' -> scaledBitmaps.get("blackRook");
            case 'B' -> scaledBitmaps.get("whiteBishop");
            case 'b' -> scaledBitmaps.get("blackBishop");
            case 'N' -> scaledBitmaps.get("whiteKnight");
            case 'n' -> scaledBitmaps.get("blackKnight");
            case 'P' -> scaledBitmaps.get("whitePawn");
            case 'p' -> scaledBitmaps.get("blackPawn");
            default -> null;
        };
    }

    public void onSizeChanged(int tileSize) {
        scaledBitmaps.clear();
        for (Map.Entry<String, Bitmap> entry : bitmaps.entrySet()) {
            scaledBitmaps.put(entry.getKey(), Bitmap.createScaledBitmap(entry.getValue(), tileSize, tileSize, true));
        }
    }

    public void recycleBitmaps() {
        for (Bitmap bitmap : bitmaps.values()) {
            if (!isNull(bitmap) && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
        }
        for (Bitmap bitmap : scaledBitmaps.values()) {
            if (!isNull(bitmap) && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
        }
        bitmaps.clear();
        scaledBitmaps.clear();
    }
}