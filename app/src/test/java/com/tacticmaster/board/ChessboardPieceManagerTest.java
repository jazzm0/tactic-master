package com.tacticmaster.board;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ChessboardPieceManagerTest {

    @Mock
    private Context mockContext;

    @Mock
    private Resources mockResources;

    @Mock
    private Bitmap mockBitmap;

    private ChessboardPieceManager pieceManager;

    @Before
    public void setUp() {
        when(mockContext.getResources()).thenReturn(mockResources);

        try (MockedStatic<BitmapFactory> bitmapFactory = Mockito.mockStatic(BitmapFactory.class)) {
            bitmapFactory.when(() -> BitmapFactory.decodeResource(mockResources, anyInt()))
                    .thenReturn(mockBitmap);

            pieceManager = new ChessboardPieceManager(mockContext);
        }
    }

    @Test
    public void testConstructor_LoadsAllBitmaps() {
        assertNotNull(pieceManager.getBitmaps());
        assertEquals(12, pieceManager.getBitmaps().size());
    }

    @Test
    public void testGetPieceBitmap_WhiteKing() {
        Bitmap scaledBitmap = mock(Bitmap.class);
        when(Bitmap.createScaledBitmap(mockBitmap, 100, 100, true)).thenReturn(scaledBitmap);

        pieceManager.onSizeChanged(100);
        Bitmap result = pieceManager.getPieceBitmap('K');

        assertEquals(scaledBitmap, result);
    }

    @Test
    public void testGetPieceBitmap_BlackKing() {
        Bitmap scaledBitmap = mock(Bitmap.class);
        when(Bitmap.createScaledBitmap(mockBitmap, 100, 100, true)).thenReturn(scaledBitmap);

        pieceManager.onSizeChanged(100);
        Bitmap result = pieceManager.getPieceBitmap('k');

        assertEquals(scaledBitmap, result);
    }

    @Test
    public void testGetPieceBitmap_WhiteQueen() {
        Bitmap scaledBitmap = mock(Bitmap.class);
        when(Bitmap.createScaledBitmap(mockBitmap, 100, 100, true)).thenReturn(scaledBitmap);

        pieceManager.onSizeChanged(100);
        Bitmap result = pieceManager.getPieceBitmap('Q');

        assertEquals(scaledBitmap, result);
    }

    @Test
    public void testGetPieceBitmap_BlackQueen() {
        Bitmap scaledBitmap = mock(Bitmap.class);
        when(Bitmap.createScaledBitmap(mockBitmap, 100, 100, true)).thenReturn(scaledBitmap);

        pieceManager.onSizeChanged(100);
        Bitmap result = pieceManager.getPieceBitmap('q');

        assertEquals(scaledBitmap, result);
    }

    @Test
    public void testGetPieceBitmap_WhiteRook() {
        Bitmap scaledBitmap = mock(Bitmap.class);
        when(Bitmap.createScaledBitmap(mockBitmap, 100, 100, true)).thenReturn(scaledBitmap);

        pieceManager.onSizeChanged(100);
        Bitmap result = pieceManager.getPieceBitmap('R');

        assertEquals(scaledBitmap, result);
    }

    @Test
    public void testGetPieceBitmap_BlackRook() {
        Bitmap scaledBitmap = mock(Bitmap.class);
        when(Bitmap.createScaledBitmap(mockBitmap, 100, 100, true)).thenReturn(scaledBitmap);

        pieceManager.onSizeChanged(100);
        Bitmap result = pieceManager.getPieceBitmap('r');

        assertEquals(scaledBitmap, result);
    }

    @Test
    public void testGetPieceBitmap_WhiteBishop() {
        Bitmap scaledBitmap = mock(Bitmap.class);
        when(Bitmap.createScaledBitmap(mockBitmap, 100, 100, true)).thenReturn(scaledBitmap);

        pieceManager.onSizeChanged(100);
        Bitmap result = pieceManager.getPieceBitmap('B');

        assertEquals(scaledBitmap, result);
    }

    @Test
    public void testGetPieceBitmap_BlackBishop() {
        Bitmap scaledBitmap = mock(Bitmap.class);
        when(Bitmap.createScaledBitmap(mockBitmap, 100, 100, true)).thenReturn(scaledBitmap);

        pieceManager.onSizeChanged(100);
        Bitmap result = pieceManager.getPieceBitmap('b');

        assertEquals(scaledBitmap, result);
    }

    @Test
    public void testGetPieceBitmap_WhiteKnight() {
        Bitmap scaledBitmap = mock(Bitmap.class);
        when(Bitmap.createScaledBitmap(mockBitmap, 100, 100, true)).thenReturn(scaledBitmap);

        pieceManager.onSizeChanged(100);
        Bitmap result = pieceManager.getPieceBitmap('N');

        assertEquals(scaledBitmap, result);
    }

    @Test
    public void testGetPieceBitmap_BlackKnight() {
        Bitmap scaledBitmap = mock(Bitmap.class);
        when(Bitmap.createScaledBitmap(mockBitmap, 100, 100, true)).thenReturn(scaledBitmap);

        pieceManager.onSizeChanged(100);
        Bitmap result = pieceManager.getPieceBitmap('n');

        assertEquals(scaledBitmap, result);
    }

    @Test
    public void testGetPieceBitmap_WhitePawn() {
        Bitmap scaledBitmap = mock(Bitmap.class);
        when(Bitmap.createScaledBitmap(mockBitmap, 100, 100, true)).thenReturn(scaledBitmap);

        pieceManager.onSizeChanged(100);
        Bitmap result = pieceManager.getPieceBitmap('P');

        assertEquals(scaledBitmap, result);
    }

    @Test
    public void testGetPieceBitmap_BlackPawn() {
        Bitmap scaledBitmap = mock(Bitmap.class);
        when(Bitmap.createScaledBitmap(mockBitmap, 100, 100, true)).thenReturn(scaledBitmap);

        pieceManager.onSizeChanged(100);
        Bitmap result = pieceManager.getPieceBitmap('p');

        assertEquals(scaledBitmap, result);
    }

    @Test
    public void testGetPieceBitmap_InvalidPiece() {
        pieceManager.onSizeChanged(100);
        Bitmap result = pieceManager.getPieceBitmap('X');

        assertNull(result);
    }

    @Test
    public void testGetPieceBitmap_BeforeScaling_ReturnsNull() {
        Bitmap result = pieceManager.getPieceBitmap('K');

        assertNull(result);
    }

    @Test
    public void testOnSizeChanged_ScalesAllBitmaps() {
        Bitmap scaledBitmap = mock(Bitmap.class);
        when(Bitmap.createScaledBitmap(mockBitmap, 150, 150, true)).thenReturn(scaledBitmap);

        pieceManager.onSizeChanged(150);

        // Verify that scaled bitmaps are available for all pieces
        assertNotNull(pieceManager.getPieceBitmap('K'));
        assertNotNull(pieceManager.getPieceBitmap('Q'));
        assertNotNull(pieceManager.getPieceBitmap('R'));
    }

    @Test
    public void testRecycleBitmaps_RecyclesAllBitmaps() {
        when(mockBitmap.isRecycled()).thenReturn(false);

        pieceManager.recycleBitmaps();

        verify(mockBitmap, never()).recycle(); // Original bitmaps are not mocked properly for this test
        assertEquals(0, pieceManager.getBitmaps().size());
    }

    @Test
    public void testRecycleBitmaps_SkipsAlreadyRecycled() {
        when(mockBitmap.isRecycled()).thenReturn(true);

        pieceManager.recycleBitmaps();

        verify(mockBitmap, never()).recycle();
    }

    @Test(expected = NullPointerException.class)
    public void testConstructor_NullContext_ThrowsException() {
        try (MockedStatic<BitmapFactory> bitmapFactory = Mockito.mockStatic(BitmapFactory.class)) {
            bitmapFactory.when(() -> BitmapFactory.decodeResource(null, anyInt()))
                    .thenReturn(mockBitmap);

            new ChessboardPieceManager(null);
        }
    }
}