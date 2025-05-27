package com.tacticmaster.db;

import static com.tacticmaster.db.PlayerTable.COLUMN_AUTOPLAY_ENABLED;
import static com.tacticmaster.db.PlayerTable.COLUMN_PLAYER_ID;
import static com.tacticmaster.db.PlayerTable.PLAYER_TABLE_NAME;
import static com.tacticmaster.db.PuzzleTable.COLUMN_PUZZLE_ID;
import static com.tacticmaster.db.PuzzleTable.COLUMN_RATING;
import static com.tacticmaster.db.PuzzleTable.COLUMN_SOLVED;
import static com.tacticmaster.db.PuzzleTable.PUZZLE_TABLE_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.tacticmaster.puzzle.Puzzle;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

class DatabaseAccessorTest {

    @Mock
    private DatabaseHelper mockDbHelper;

    @Mock
    private SQLiteDatabase mockDatabase;

    @Mock
    private Cursor mockCursor;

    private DatabaseAccessor databaseAccessor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mockDbHelper.openDatabase()).thenReturn(mockDatabase);
        databaseAccessor = new DatabaseAccessor(mockDbHelper);
    }

    @Test
    void testSetSolved() {
        String puzzleId = "12345";
        databaseAccessor.setSolved(puzzleId);
        verify(mockDatabase).execSQL("UPDATE " + PUZZLE_TABLE_NAME + " SET " + COLUMN_SOLVED + " = 1 WHERE " + COLUMN_PUZZLE_ID + " = ?", new String[]{puzzleId});
    }

    @Test
    void testGetSolvedPuzzleCount() {
        when(mockDatabase.rawQuery("SELECT COUNT(*) FROM " + PUZZLE_TABLE_NAME + " WHERE " + COLUMN_SOLVED + " = 1", null)).thenReturn(mockCursor);
        when(mockCursor.moveToFirst()).thenReturn(true);
        when(mockCursor.getInt(0)).thenReturn(5);

        int count = databaseAccessor.getSolvedPuzzleCount();
        assertEquals(5, count);
    }

    @Test
    void testGetAllPuzzleCount() {
        when(mockDatabase.rawQuery("SELECT COUNT(*) FROM " + PUZZLE_TABLE_NAME, null)).thenReturn(mockCursor);
        when(mockCursor.moveToFirst()).thenReturn(true);
        when(mockCursor.getInt(0)).thenReturn(10);

        int count = databaseAccessor.getAllPuzzleCount();
        assertEquals(10, count);
    }

    @Test
    void testGetPuzzlesWithinRange() {
        Set<String> excludedIds = new HashSet<>();
        excludedIds.add("123");
        excludedIds.add("456");

        String expectedQuery = "SELECT * FROM " + PUZZLE_TABLE_NAME +
                " WHERE " + COLUMN_RATING + " >= 1600 AND "
                + COLUMN_RATING + " <= 1800 AND "
                + COLUMN_SOLVED + " = 0 AND "
                + COLUMN_PUZZLE_ID + " NOT IN ('123','456') GROUP BY " + COLUMN_RATING + " LIMIT 5";

        when(mockDatabase.rawQuery(expectedQuery, null)).thenReturn(mockCursor);

        when(mockCursor.moveToNext()).thenReturn(true, true, true, false); // 3 rows
        when(mockCursor.getColumnIndex(COLUMN_PUZZLE_ID)).thenReturn(0);
        when(mockCursor.getColumnIndex(PuzzleTable.COLUMN_FEN)).thenReturn(1);
        when(mockCursor.getColumnIndex(PuzzleTable.COLUMN_MOVES)).thenReturn(2);
        when(mockCursor.getColumnIndex(COLUMN_RATING)).thenReturn(3);

        when(mockCursor.getString(0)).thenReturn("puzzle1", "puzzle2", "puzzle3");
        when(mockCursor.getString(1)).thenReturn("fen1", "fen2", "fen3");
        when(mockCursor.getString(2)).thenReturn("moves1", "moves2", "moves3");
        when(mockCursor.getInt(3)).thenReturn(1700, 1750, 1800);
        when(mockCursor.getInt(4)).thenReturn(50, 40, 30);
        when(mockCursor.getInt(5)).thenReturn(100, 200, 300);
        when(mockCursor.getInt(6)).thenReturn(10, 20, 30);
        when(mockCursor.getString(7)).thenReturn("theme1", "theme2", "theme3");
        when(mockCursor.getString(8)).thenReturn("url1", "url2", "url3");
        when(mockCursor.getString(9)).thenReturn("opening1", "opening2", "opening3");

        List<Puzzle> puzzles = databaseAccessor.getPuzzlesWithinRange(1600, 1800, excludedIds);

        assertEquals(3, puzzles.size());
        assertEquals("puzzle1", puzzles.get(0).puzzleId());
        assertEquals("puzzle2", puzzles.get(1).puzzleId());
        assertEquals("puzzle3", puzzles.get(2).puzzleId());
    }

    @Test
    void testGetPuzzleById() {
        String puzzleId = "1";
        when(mockDatabase.rawQuery("SELECT * FROM " + PUZZLE_TABLE_NAME +
                " WHERE " + COLUMN_PUZZLE_ID + " = ?", new String[]{puzzleId})).thenReturn(mockCursor);
        when(mockCursor.moveToNext()).thenReturn(true, false); // 1 row
        when(mockCursor.getColumnIndex(COLUMN_PUZZLE_ID)).thenReturn(0);
        when(mockCursor.getColumnIndex(PuzzleTable.COLUMN_FEN)).thenReturn(1);
        when(mockCursor.getColumnIndex(PuzzleTable.COLUMN_MOVES)).thenReturn(2);
        when(mockCursor.getColumnIndex(COLUMN_RATING)).thenReturn(3);
        when(mockCursor.getString(0)).thenReturn(puzzleId);
        when(mockCursor.getString(1)).thenReturn("fen1");
        when(mockCursor.getString(2)).thenReturn("moves1");
        when(mockCursor.getInt(3)).thenReturn(1700);
        when(mockCursor.getInt(4)).thenReturn(50);
        when(mockCursor.getInt(5)).thenReturn(100);
        when(mockCursor.getInt(6)).thenReturn(10);
        when(mockCursor.getString(7)).thenReturn("theme1");
        when(mockCursor.getString(8)).thenReturn("url1");
        when(mockCursor.getString(9)).thenReturn("opening1");

        Puzzle puzzle = databaseAccessor.getPuzzleById(puzzleId);

        assertEquals(puzzleId, puzzle.puzzleId());

        when(mockDatabase.rawQuery("SELECT * FROM " + PUZZLE_TABLE_NAME +
                " WHERE " + COLUMN_PUZZLE_ID + " = ?", new String[]{"2"})).thenReturn(mockCursor);
        when(mockCursor.moveToNext()).thenReturn(false); // 0 rows

        assertThrows(NoSuchElementException.class, () -> databaseAccessor.getPuzzleById("2"));
    }

    @Test
    void testStorePlayerRating() {
        int rating = 2000;
        databaseAccessor.storePlayerRating(rating);

        verify(mockDatabase).update(eq("player_table"), any(ContentValues.class), eq("PlayerId = 1"), isNull());
    }

    @Test
    void testGetPlayerRating() {
        when(mockDatabase.rawQuery("SELECT PlayerRating FROM player_table", null)).thenReturn(mockCursor);
        when(mockCursor.moveToFirst()).thenReturn(true);
        when(mockCursor.getInt(0)).thenReturn(1500);

        int rating = databaseAccessor.getPlayerRating();
        assertEquals(1500, rating);
    }

    @Test
    void testWasNotSolved() {
        String puzzleId = "12345";

        when(mockDatabase.rawQuery("SELECT " + COLUMN_SOLVED + " FROM " + PUZZLE_TABLE_NAME + " WHERE " + COLUMN_PUZZLE_ID + " = ?", new String[]{puzzleId}))
                .thenReturn(mockCursor);
        when(mockCursor.moveToFirst()).thenReturn(true);
        when(mockCursor.getInt(0)).thenReturn(0);

        boolean result = databaseAccessor.wasNotSolved(puzzleId);
        assertTrue(result);

        when(mockCursor.getInt(0)).thenReturn(1);

        result = databaseAccessor.wasNotSolved(puzzleId);
        assertFalse(result);

        when(mockCursor.moveToFirst()).thenReturn(false);

        result = databaseAccessor.wasNotSolved(puzzleId);
        assertTrue(result);
    }

    @Test
    void testStorePlayerAutoplay() {
        databaseAccessor.storePlayerAutoplay(true);

        verify(mockDatabase).update(eq(PLAYER_TABLE_NAME), any(), eq(COLUMN_PLAYER_ID + " = 1"), isNull());
    }

    @Test
    void testGetPlayerAutoplay() {
        when(mockDatabase.rawQuery("SELECT " + COLUMN_AUTOPLAY_ENABLED + " FROM " + PLAYER_TABLE_NAME, null)).thenReturn(mockCursor);
        when(mockCursor.moveToFirst()).thenReturn(true);
        when(mockCursor.getInt(0)).thenReturn(1);

        boolean result = databaseAccessor.getPlayerAutoplay();
        assertTrue(result);

        when(mockCursor.getInt(0)).thenReturn(0);

        result = databaseAccessor.getPlayerAutoplay();
        assertFalse(result);

        when(mockCursor.moveToFirst()).thenReturn(false);

        result = databaseAccessor.getPlayerAutoplay();
        assertTrue(result);
    }
}