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
        Bitmap bitmap = assetExists(context, dir, pieceName + ".svg")
                ? loadSvgBitmap(context, dir + "/" + pieceName + ".svg")
                : loadPngBitmap(context, dir + "/" + pieceName + ".png");
        return applyScaleFactor(bitmap, readScaleFactor(context, dir));
    }

    /**
     * Optional per-set scale: if {@code pieces/<set>/scalefactor.txt} exists and
     * holds a single positive number (e.g. {@code 0.8}), pieces of that set are
     * shrunk by that factor and centered on a transparent bitmap of the original
     * size. Missing/invalid file or non-positive value means no scaling (1.0).
     */
    private static float readScaleFactor(Context context, String dir) {
        if (!assetExists(context, dir, "scalefactor.txt")) {
            return 1f;
        }
        try (InputStream in = context.getAssets().open(dir + "/scalefactor.txt");
             java.util.Scanner scanner = new java.util.Scanner(in)) {
            float factor = Float.parseFloat(scanner.next().trim());
            return factor > 0 ? factor : 1f;
        } catch (IOException | java.util.NoSuchElementException | NumberFormatException e) {
            return 1f;
        }
    }

    /**
     * Returns a copy of {@code source} shrunk by {@code scaleFactor} and centered
     * on a transparent bitmap of the same dimensions, so the piece keeps its tile
     * footprint. Returns {@code source} unchanged when the factor is 1.
     */
    private static Bitmap applyScaleFactor(Bitmap source, float scaleFactor) {
        if (scaleFactor == 1f) {
            return source;
        }
        int w = source.getWidth();
        int h = source.getHeight();
        Bitmap scaled = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(scaled);
        canvas.translate(w * (1f - scaleFactor) / 2f, h * (1f - scaleFactor) / 2f);
        canvas.scale(scaleFactor, scaleFactor);
        canvas.drawBitmap(source, 0, 0, null);
        return scaled;
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

            // Normalize every SVG to a single coordinate source before rendering.
            // Sets declare their size differently — some with width/height only
            // (e.g. 45x45, no viewBox), some with a viewBox only (e.g. 0 0 4096
            // 4096, no width/height). Mixing those with the renderer's own mapping
            // scaled one set correctly while shrinking the other. So: guarantee a
            // viewBox (synthesized from width/height, or the render size as a last
            // resort), then force the document width/height to the render size so
            // the artwork always maps 1:1 onto the target bitmap.
            if (isNull(svg.getDocumentViewBox())) {
                float w = svg.getDocumentWidth();
                float h = svg.getDocumentHeight();
                if (w <= 0 || h <= 0) {
                    w = h = SVG_RENDER_SIZE;
                }
                svg.setDocumentViewBox(0, 0, w, h);
            }
            svg.setDocumentWidth(SVG_RENDER_SIZE);
            svg.setDocumentHeight(SVG_RENDER_SIZE);

            Bitmap bitmap = Bitmap.createBitmap(SVG_RENDER_SIZE, SVG_RENDER_SIZE, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
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