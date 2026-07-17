package com.tacticmaster.board;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Unit tests for the piece-loading logic. The class loads pieces from
 * {@code assets/pieces/<set>/} via {@link AssetManager} and decodes PNGs with
 * {@link BitmapFactory#decodeStream}; both are mocked here so the tests run on
 * the JVM without a device. SVG rendering and real bitmap scaling are exercised
 * by the instrumented test instead.
 */
public class ChessboardPieceManagerTest {

    private static final String[] PIECE_FILES = {
            "wk.png", "bk.png", "wq.png", "bq.png", "wr.png", "br.png",
            "wb.png", "bb.png", "wn.png", "bn.png", "wp.png", "bp.png"
    };

    private Context mockContext;
    private AssetManager mockAssets;
    private Bitmap mockBitmap;
    private MockedStatic<BitmapFactory> bitmapFactory;
    private ChessboardPieceManager pieceManager;

    @BeforeEach
    public void setUp() throws IOException {
        mockContext = mock(Context.class);
        mockAssets = mock(AssetManager.class);
        mockBitmap = mock(Bitmap.class);

        when(mockContext.getAssets()).thenReturn(mockAssets);
        // One set "classic" with 12 PNG pieces (no .svg -> PNG decode path).
        when(mockAssets.list("pieces")).thenReturn(new String[]{"classic"});
        when(mockAssets.list("pieces/classic")).thenReturn(PIECE_FILES);
        when(mockAssets.open(any())).thenReturn(new ByteArrayInputStream(new byte[]{1}));

        bitmapFactory = Mockito.mockStatic(BitmapFactory.class);
        bitmapFactory.when(() -> BitmapFactory.decodeStream(any())).thenReturn(mockBitmap);

        pieceManager = new ChessboardPieceManager(mockContext);
    }

    @AfterEach
    public void tearDown() {
        bitmapFactory.close();
    }

    @Test
    public void testConstructor_LoadsAllBitmaps() {
        assertNotNull(pieceManager.getBitmaps());
        assertEquals(12, pieceManager.getBitmaps().size());
    }

    @Test
    public void testAvailablePieceSets_ReturnsAssetFolders() {
        assertEquals("classic", ChessboardPieceManager.availablePieceSets(mockContext)[0]);
    }

    @Test
    public void testDefaultPieceSet_IsFirstFolder() {
        assertEquals("classic", ChessboardPieceManager.defaultPieceSet(mockContext));
    }

    @Test
    public void testDefaultPieceSet_EmptyWhenNoFolders() throws IOException {
        when(mockAssets.list("pieces")).thenReturn(new String[0]);
        assertEquals("", ChessboardPieceManager.defaultPieceSet(mockContext));
    }

    @Test
    public void testGetPieceBitmap_BeforeScaling_ReturnsNull() {
        assertNull(pieceManager.getPieceBitmap('K'));
    }

    @Test
    public void testRecycleBitmaps_SkipsAlreadyRecycled() {
        when(mockBitmap.isRecycled()).thenReturn(true);

        pieceManager.recycleBitmaps();

        verify(mockBitmap, never()).recycle();
        assertEquals(0, pieceManager.getBitmaps().size());
    }

    @Test
    public void testConstructor_NullContext_Throws() {
        assertThrows(NullPointerException.class, () -> new ChessboardPieceManager(null, "classic"));
    }
}
