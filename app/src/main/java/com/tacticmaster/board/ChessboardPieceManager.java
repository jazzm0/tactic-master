package com.tacticmaster.board;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;

import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGParseException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ChessboardPieceManager {

    /**
     * The piece set used when none is specified. Corresponds to the folder
     * {@code assets/pieces/classic/}.
     */
    public static final String DEFAULT_PIECE_SET = "classic";
    public static final String LICHESS_PIECE_SET = "lichess";

    private static final String PIECES_ASSET_DIR = "pieces";

    /**
     * Base size (px) that SVG pieces are rendered at. Larger than any expected
     * tile size so that scaling down in {@link #onSizeChanged(int)} stays crisp.
     */
    private static final int SVG_RENDER_SIZE = 512;

    private final Map<String, Bitmap> bitmaps = new HashMap<>();
    private final Map<String, Bitmap> scaledBitmaps = new HashMap<>();
    private final Context context;

    public ChessboardPieceManager(Context context) {
        this(context, LICHESS_PIECE_SET);
    }

    public ChessboardPieceManager(Context context, String pieceSet) {
        this.context = requireNonNull(context);
        loadBitmaps(requireNonNull(pieceSet));
    }

    private void loadBitmaps(String pieceSet) {
        bitmaps.put("whiteKing", loadBitmap(pieceSet, "wk"));
        bitmaps.put("blackKing", loadBitmap(pieceSet, "bk"));
        bitmaps.put("whiteQueen", loadBitmap(pieceSet, "wq"));
        bitmaps.put("blackQueen", loadBitmap(pieceSet, "bq"));
        bitmaps.put("whiteRook", loadBitmap(pieceSet, "wr"));
        bitmaps.put("blackRook", loadBitmap(pieceSet, "br"));
        bitmaps.put("whiteBishop", loadBitmap(pieceSet, "wb"));
        bitmaps.put("blackBishop", loadBitmap(pieceSet, "bb"));
        bitmaps.put("whiteKnight", loadBitmap(pieceSet, "wn"));
        bitmaps.put("blackKnight", loadBitmap(pieceSet, "bn"));
        bitmaps.put("whitePawn", loadBitmap(pieceSet, "wp"));
        bitmaps.put("blackPawn", loadBitmap(pieceSet, "bp"));
    }

    private Bitmap loadBitmap(String pieceSet, String pieceName) {
        String dir = PIECES_ASSET_DIR + "/" + pieceSet;
        if (assetExists(dir, pieceName + ".svg")) {
            return loadSvgBitmap(dir + "/" + pieceName + ".svg");
        }
        return loadPngBitmap(dir + "/" + pieceName + ".png");
    }

    private boolean assetExists(String dir, String fileName) {
        try {
            String[] files = context.getAssets().list(dir);
            return files != null && Arrays.asList(files).contains(fileName);
        } catch (IOException e) {
            return false;
        }
    }

    private Bitmap loadPngBitmap(String assetPath) {
        try (InputStream inputStream = context.getAssets().open(assetPath)) {
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            if (isNull(bitmap)) {
                throw new IllegalStateException("Failed to decode bitmap (asset: " + assetPath + ")");
            }
            return bitmap;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load bitmap (asset: " + assetPath + ")", e);
        }
    }

    private Bitmap loadSvgBitmap(String assetPath) {
        try (InputStream inputStream = context.getAssets().open(assetPath)) {
            SVG svg = SVG.getFromInputStream(inputStream);

            // Determine the artwork's intrinsic size. Prefer the viewBox, then the
            // declared width/height; fall back to the render size as a last resort.
            android.graphics.RectF viewBox = svg.getDocumentViewBox();
            float intrinsicW = !isNull(viewBox) ? viewBox.width() : svg.getDocumentWidth();
            float intrinsicH = !isNull(viewBox) ? viewBox.height() : svg.getDocumentHeight();
            if (intrinsicW <= 0 || intrinsicH <= 0) {
                intrinsicW = intrinsicH = SVG_RENDER_SIZE;
            }

            Bitmap bitmap = Bitmap.createBitmap(SVG_RENDER_SIZE, SVG_RENDER_SIZE, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            // Scale the canvas so the intrinsic-sized artwork fills the whole bitmap.
            canvas.scale(SVG_RENDER_SIZE / intrinsicW, SVG_RENDER_SIZE / intrinsicH);
            svg.renderToCanvas(canvas);
            return bitmap;
        } catch (IOException | SVGParseException e) {
            throw new IllegalStateException("Failed to render SVG (asset: " + assetPath + ")", e);
        }
    }

    Map<String, Bitmap> getBitmaps() {
        return bitmaps;
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