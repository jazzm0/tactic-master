package com.tacticmaster.db;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class PuzzleTableTest {

    @Test
    void testPuzzleTableConstants() {
        assertEquals("lichess_db_puzzle", PuzzleTable.PUZZLE_TABLE_NAME);
        assertEquals("PuzzleId", PuzzleTable.COLUMN_PUZZLE_ID);
        assertEquals("FEN", PuzzleTable.COLUMN_FEN);
        assertEquals("Moves", PuzzleTable.COLUMN_MOVES);
        assertEquals("Rating", PuzzleTable.COLUMN_RATING);
        assertEquals("Solved", PuzzleTable.COLUMN_SOLVED);
        assertEquals("Themes", PuzzleTable.COLUMN_THEMES);
    }
}
