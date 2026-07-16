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
        this(context, defaultPieceSet(context));
    }

    public ChessboardPieceManager(Context context, String pieceSet) {
        this.context = requireNonNull(context);
        loadBitmaps(requireNonNull(pieceSet));
    }

    /**
     * Default piece set when none is stored: the first folder discovered under
     * {@code assets/pieces/} (alphabetical). Empty string if none exist.
     */
    public static String defaultPieceSet(Context context) {
        String[] sets = availablePieceSets(context);
        return sets.length > 0 ? sets[0] : "";
    }

    private void loadBitmaps(String pieceSet) {
        bitmaps.put("whiteKing", loadPiece(context, pieceSet, "wk"));
        bitmaps.put("blackKing", loadPiece(context, pieceSet, "bk"));
        bitmaps.put("whiteQueen", loadPiece(context, pieceSet, "wq"));
        bitmaps.put("blackQueen", loadPiece(context, pieceSet, "bq"));
        bitmaps.put("whiteRook", loadPiece(context, pieceSet, "wr"));
        bitmaps.put("blackRook", loadPiece(context, pieceSet, "br"));
        bitmaps.put("whiteBishop", loadPiece(context, pieceSet, "wb"));
        bitmaps.put("blackBishop", loadPiece(context, pieceSet, "bb"));
        bitmaps.put("whiteKnight", loadPiece(context, pieceSet, "wn"));
        bitmaps.put("blackKnight", loadPiece(context, pieceSet, "bn"));
        bitmaps.put("whitePawn", loadPiece(context, pieceSet, "wp"));
        bitmaps.put("blackPawn", loadPiece(context, pieceSet, "bp"));
    }

    /**
     * Lists the piece sets bundled under {@code assets/pieces/} — one subfolder
     * per set. Used by the settings UI to offer a choice.
     */
    public static String[] availablePieceSets(Context context) {
        try {
            String[] dirs = context.getAssets().list(PIECES_ASSET_DIR);
            return dirs != null ? dirs : new String[0];
        } catch (IOException e) {
            return new String[0];
        }
    }

    /**
     * Loads a single piece (e.g. {@code "bn"} for the black knight) from a set,
     * rendering SVG or decoding PNG as appropriate. Shared with the settings
     * preview so both paths use identical rendering.
     */
    public static Bitmap loadPiece(Context context, String pieceSet, String pieceName) {
        String dir = PIECES_ASSET_DIR + "/" + pieceSet;
        if (assetExists(context, dir, pieceName + ".svg")) {
            return loadSvgBitmap(context, dir + "/" + pieceName + ".svg");
        }
        return loadPngBitmap(context, dir + "/" + pieceName + ".png");
    }

    private static boolean assetExists(Context context, String dir, String fileName) {
        try {
            String[] files = context.getAssets().list(dir);
            return files != null && Arrays.asList(files).contains(fileName);
        } catch (IOException e) {
            return false;
        }
    }

    private static Bitmap loadPngBitmap(Context context, String assetPath) {
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

    private static Bitmap loadSvgBitmap(Context context, String assetPath) {
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
        recycleAll(bitmaps.values());
        recycleAll(scaledBitmaps.values());
        bitmaps.clear();
        scaledBitmaps.clear();
    }

    private static void recycleAll(java.util.Collection<Bitmap> toRecycle) {
        for (Bitmap bitmap : toRecycle) {
            if (!isNull(bitmap) && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
        }
    }
}