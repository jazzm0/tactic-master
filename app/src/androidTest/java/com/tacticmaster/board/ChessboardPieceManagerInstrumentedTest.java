package com.tacticmaster.board;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ChessboardPieceManagerInstrumentedTest {

    private ChessboardPieceManager manager;

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        manager = new ChessboardPieceManager(context);
    }

    @Test
    public void testLoadBitmaps() {
        assertNotNull(manager.getBitmaps());
        assertEquals(12, manager.getBitmaps().size());
    }

    @Test
    public void testOnSizeChanged() {
        manager.onSizeChanged(64);
        Bitmap whiteKingBitmap = manager.getPieceBitmap('K');
        assertNotNull(whiteKingBitmap);
        assertEquals(64, whiteKingBitmap.getWidth());
        assertEquals(64, whiteKingBitmap.getHeight());
    }
}