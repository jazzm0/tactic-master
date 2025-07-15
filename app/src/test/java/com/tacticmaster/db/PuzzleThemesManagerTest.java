package com.tacticmaster.db;

import static com.tacticmaster.db.PuzzleTable.COLUMN_SOLVED;
import static com.tacticmaster.db.PuzzleTable.COLUMN_THEMES;
import static com.tacticmaster.db.PuzzleTable.PUZZLE_TABLE_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Set;

public class PuzzleThemesManagerTest {

    @Mock
    private DatabaseHelper dbHelper;

    @Mock
    PuzzleThemesManager.PuzzleThemesListener puzzleThemesListener;

    @Mock
    private SQLiteDatabase db;

    @Mock
    private Cursor cursor;

    private PuzzleThemesManager puzzleThemesManager;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(dbHelper.openDatabase()).thenReturn(db);
        puzzleThemesManager = new PuzzleThemesManager(new DatabaseAccessor(dbHelper), puzzleThemesListener);
    }

    @Test
    public void testGetPuzzleThemes_emptyResult() {
        when(db.rawQuery(anyString(), any())).thenReturn(cursor);
        when(cursor.getColumnIndex(COLUMN_THEMES)).thenReturn(-1);
        when(cursor.moveToNext()).thenReturn(false);

        Set<String> themes = puzzleThemesManager.getPuzzleThemes();
        assertTrue(themes.isEmpty());
    }

    @Test
    public void testGetPuzzleThemes_singleTheme() {
        when(db.rawQuery(anyString(), any())).thenReturn(cursor);
        when(cursor.getColumnIndex(COLUMN_THEMES)).thenReturn(0);
        when(cursor.moveToNext()).thenReturn(true, false);
        when(cursor.getString(0)).thenReturn("strategy");

        Set<String> themes = puzzleThemesManager.getPuzzleThemes();
        assertEquals(1, themes.size());
        assertTrue(themes.contains("strategy"));
    }

    @Test
    public void testGetPuzzleThemes_multipleThemes() {
        when(db.rawQuery(anyString(), any())).thenReturn(cursor);
        when(cursor.getColumnIndex(COLUMN_THEMES)).thenReturn(0);
        when(cursor.moveToNext()).thenReturn(true, true, false);
        when(cursor.getString(0)).thenReturn("strategy tactics", "endgame");

        Set<String> themes = puzzleThemesManager.getPuzzleThemes();
        assertEquals(3, themes.size());
        assertTrue(themes.contains("strategy"));
        assertTrue(themes.contains("tactics"));
        assertTrue(themes.contains("endgame"));
    }

    @Test
    public void testGetPuzzleThemes_emptyAndNullThemes() {
        when(db.rawQuery(anyString(), any())).thenReturn(cursor);
        when(cursor.getColumnIndex(COLUMN_THEMES)).thenReturn(0);
        when(cursor.moveToNext()).thenReturn(true).thenReturn(true).thenReturn(true).thenReturn(false);
        when(cursor.getString(0)).thenReturn("").thenReturn(null).thenReturn("  ");

        Set<String> themes = puzzleThemesManager.getPuzzleThemes();
        assertTrue(themes.isEmpty());
    }

    @Test
    public void testGetPuzzleThemes_queryConstruction() {
        String expectedQuery = "SELECT DISTINCT " + COLUMN_THEMES + " FROM " + PUZZLE_TABLE_NAME +
                " WHERE " + COLUMN_THEMES + " IS NOT NULL AND " +
                COLUMN_THEMES + " != '' AND " + COLUMN_SOLVED + " = 0";
        when(db.rawQuery(expectedQuery, null)).thenReturn(cursor);
        when(cursor.getColumnIndex(COLUMN_THEMES)).thenReturn(0);
        when(cursor.moveToNext()).thenReturn(false);

        puzzleThemesManager.getPuzzleThemes();
        verify(db, times(1)).rawQuery(expectedQuery, null);
    }

    @Test
    public void testGetPuzzleThemes_usesCachedThemes() {

        when(db.rawQuery(anyString(), any())).thenReturn(cursor);
        when(cursor.getColumnIndex(COLUMN_THEMES)).thenReturn(0);
        when(cursor.moveToNext()).thenReturn(true, false);
        when(cursor.getString(0)).thenReturn("strategy");

        Set<String> themes = puzzleThemesManager.getPuzzleThemes();
        assertEquals(1, themes.size());
        assertTrue(themes.contains("strategy"));
        verify(db, times(1)).rawQuery(anyString(), any());

        themes = puzzleThemesManager.getPuzzleThemes();
        assertEquals(1, themes.size());
        assertTrue(themes.contains("strategy"));
        verify(db, times(1)).rawQuery(anyString(), any());
    }
}