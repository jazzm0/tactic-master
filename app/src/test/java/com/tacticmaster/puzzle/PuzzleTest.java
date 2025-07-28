package com.tacticmaster.puzzle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PuzzleTest {

    @Test
    void testPuzzleConstructorWithAllParameters() {
        String puzzleId = "ABC123";
        String fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
        String moves = "e2e4 e7e5";
        int rating = 1500;
        String themes = "endgame,tactics";
        boolean solved = true;

        Puzzle puzzle = new Puzzle(puzzleId, fen, moves, rating, themes, solved);

        assertEquals(puzzleId, puzzle.puzzleId());
        assertEquals(fen, puzzle.fen());
        assertEquals(moves, puzzle.moves());
        assertEquals(rating, puzzle.rating());
        assertEquals(themes, puzzle.themes());
        assertTrue(puzzle.solved());
    }

    @Test
    void testPuzzleConstructorWithMinimalParameters() {
        String puzzleId = "XYZ789";
        String fen = "8/8/8/8/8/8/8/8 w - - 0 1";
        String moves = "a1a2";
        int rating = 1200;

        Puzzle puzzle = new Puzzle(puzzleId, fen, moves, rating);

        assertEquals(puzzleId, puzzle.puzzleId());
        assertEquals(fen, puzzle.fen());
        assertEquals(moves, puzzle.moves());
        assertEquals(rating, puzzle.rating());
        assertEquals("", puzzle.themes());
        assertFalse(puzzle.solved());
    }

    @Test
    void testPuzzleEquality() {
        Puzzle puzzle1 = new Puzzle("123", "fen1", "moves1", 1500, "theme1", false);
        Puzzle puzzle2 = new Puzzle("123", "fen1", "moves1", 1500, "theme1", false);

        assertEquals(puzzle1, puzzle2);
        assertEquals(puzzle1.hashCode(), puzzle2.hashCode());
    }

    @Test
    void testPuzzleInequality() {
        Puzzle puzzle1 = new Puzzle("123", "fen1", "moves1", 1500, "theme1", false);
        Puzzle puzzle2 = new Puzzle("456", "fen1", "moves1", 1500, "theme1", false);

        assertNotEquals(puzzle1, puzzle2);
    }

    @Test
    void testPuzzleWithDifferentRatings() {
        Puzzle puzzle1 = new Puzzle("123", "fen1", "moves1", 1500, "theme1", false);
        Puzzle puzzle2 = new Puzzle("123", "fen1", "moves1", 1600, "theme1", false);

        assertNotEquals(puzzle1, puzzle2);
    }

    @Test
    void testPuzzleWithDifferentSolvedStatus() {
        Puzzle puzzle1 = new Puzzle("123", "fen1", "moves1", 1500, "theme1", false);
        Puzzle puzzle2 = new Puzzle("123", "fen1", "moves1", 1500, "theme1", true);

        assertNotEquals(puzzle1, puzzle2);
    }

    @Test
    void testPuzzleWithEmptyStrings() {
        Puzzle puzzle = new Puzzle("", "", "", 0, "", false);

        assertEquals("", puzzle.puzzleId());
        assertEquals("", puzzle.fen());
        assertEquals("", puzzle.moves());
        assertEquals(0, puzzle.rating());
        assertEquals("", puzzle.themes());
        assertFalse(puzzle.solved());
    }

    @Test
    void testPuzzleWithNegativeRating() {
        Puzzle puzzle = new Puzzle("123", "fen", "moves", -100);

        assertEquals(-100, puzzle.rating());
    }

    @Test
    void testPuzzleWithVeryHighRating() {
        Puzzle puzzle = new Puzzle("123", "fen", "moves", 3000);

        assertEquals(3000, puzzle.rating());
    }

    @Test
    void testPuzzleToString() {
        Puzzle puzzle = new Puzzle("123", "fen", "moves", 1500, "tactics", true);
        String toString = puzzle.toString();

        assertTrue(toString.contains("123"));
        assertTrue(toString.contains("fen"));
        assertTrue(toString.contains("moves"));
        assertTrue(toString.contains("1500"));
        assertTrue(toString.contains("tactics"));
        assertTrue(toString.contains("true"));
    }
}
