package com.tacticmaster.puzzle;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anySet;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tacticmaster.db.DatabaseAccessor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.NoSuchElementException;

class PuzzleManagerTest {

    private PuzzleManager puzzleManager;
    private DatabaseAccessor databaseAccessor;

    @BeforeEach
    void setUp() {
        databaseAccessor = mock(DatabaseAccessor.class);
        when(databaseAccessor.getPlayerRating()).thenReturn(1200);
        puzzleManager = new PuzzleManager(databaseAccessor);
    }

    @Test
    void testGetCurrentPuzzleThrowsExceptionWhenIndexInvalid() {
        assertThrows(NoSuchElementException.class, puzzleManager::getCurrentPuzzle);
    }

    @Test
    void testMoveToNextPuzzleLoadsNewPuzzles() {
        when(databaseAccessor.getPuzzlesWithinRange(anyInt(), anyInt(), anySet(), anySet()))
                .thenReturn(List.of(new Puzzle("1", "fen", "moves", 1000)));

        puzzleManager.moveToNextPuzzle();
        assertNotNull(puzzleManager.getCurrentPuzzle());
        assertEquals("1", puzzleManager.getCurrentPuzzle().getPuzzleId());
    }

    @Test
    void testMoveToNextPuzzleThrowsExceptionWhenNoPuzzlesAvailable() {
        when(databaseAccessor.getPuzzlesWithinRange(anyInt(), anyInt(), anySet(), anySet()))
                .thenReturn(List.of());

        assertThrows(NoSuchElementException.class, puzzleManager::moveToNextPuzzle);
    }

    @Test
    void testMoveToPreviousPuzzleWrapsAround() {
        when(databaseAccessor.getPuzzlesWithinRange(anyInt(), anyInt(), anySet(), anySet()))
                .thenReturn(List.of(new Puzzle("1", "fen", "moves", 1000), new Puzzle("2", "fen", "moves", 1100)));

        puzzleManager.moveToNextPuzzle();
        puzzleManager.moveToNextPuzzle();
        puzzleManager.moveToPreviousPuzzle();

        assertEquals("1", puzzleManager.getCurrentPuzzle().getPuzzleId());
    }

    @Test
    void testMoveToPreviousPuzzleLoadsPuzzles() {
        assertThrows(NoSuchElementException.class, () -> puzzleManager.moveToPreviousPuzzle());
        verify(databaseAccessor, times(23)).getPuzzlesWithinRange(anyInt(), anyInt(), anySet(), anySet());

        when(databaseAccessor.getPuzzlesWithinRange(anyInt(), anyInt(), anySet(), anySet()))
                .thenReturn(List.of(new Puzzle("1", "fen", "moves", 1000)));

        puzzleManager.moveToPreviousPuzzle();

        assertEquals("1", puzzleManager.getCurrentPuzzle().getPuzzleId());
    }

    @Test
    void testLoadPuzzleByIdAddsPuzzleAndUpdatesIndex() {
        when(databaseAccessor.getPuzzleById("1"))
                .thenReturn(new Puzzle("1", "fen", "moves", 1000));

        puzzleManager.loadPuzzleById("1");
        assertNotNull(puzzleManager.getCurrentPuzzle());
        assertEquals("1", puzzleManager.getCurrentPuzzle().getPuzzleId());
    }

    @Test
    void testLoadPuzzleByIdDoesNotDuplicatePuzzle() {
        when(databaseAccessor.getPuzzleById("1"))
                .thenReturn(new Puzzle("1", "fen", "moves", 1000));

        puzzleManager.loadPuzzleById("1");
        puzzleManager.loadPuzzleById("1");

        assertEquals(1, puzzleManager.getCurrentPuzzle().getPuzzleId().length());
    }

    @Test
    void testLoadPuzzleByIdThrowsException() {
        when(databaseAccessor.getPuzzleById(anyString())).thenThrow(NoSuchElementException.class);
        assertThrows(NoSuchElementException.class, () -> puzzleManager.loadPuzzleById("invalid_id"));
    }

    @Test
    void testUpdateRatingChangesRating() {
        puzzleManager.updateRating(1300);
        assertEquals(1300, puzzleManager.getRating());
    }
}