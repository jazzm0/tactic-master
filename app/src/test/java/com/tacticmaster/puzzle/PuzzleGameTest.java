package com.tacticmaster.puzzle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.TreeSet;

public class PuzzleGameTest {

    @Test
    public void testCompareTo() {
        PuzzleGame puzzleGame1 = new PuzzleGame("1", "fen1", "moves1", 1500);
        PuzzleGame puzzleGame2 = new PuzzleGame("2", "fen2", "moves2", 1600);
        PuzzleGame puzzleGame3 = new PuzzleGame("3", "fen3", "moves3", 1400);

        assertTrue(puzzleGame1.compareTo(puzzleGame2) < 0);

        assertTrue(puzzleGame2.compareTo(puzzleGame3) > 0);

        assertTrue(puzzleGame1.compareTo(puzzleGame3) > 0);

        assertEquals(0, puzzleGame1.compareTo(new PuzzleGame("1", "fen1", "moves1", 1500)));
    }

    @Test
    public void treeSet() {
        var puzzleGameSet = new TreeSet<PuzzleGame>();
        puzzleGameSet.add(new PuzzleGame("1", "fen1", "moves1", 1500));
        puzzleGameSet.add(new PuzzleGame("2", "fen2", "moves2", 1600));
        puzzleGameSet.add(new PuzzleGame("3", "fen3", "moves3", 1400));

        assertEquals(1400, puzzleGameSet.first().rating());
        assertEquals(1600, puzzleGameSet.last().rating());

        puzzleGameSet.pollLast();

        assertEquals(1400, puzzleGameSet.first().rating());
        assertEquals(1500, puzzleGameSet.last().rating());
    }

    @Test
    public void testSameRatingDifferentId() {
        var puzzleGameSet = new TreeSet<PuzzleGame>();
        puzzleGameSet.add(new PuzzleGame("1", "fen1", "moves1", 1500));
        puzzleGameSet.add(new PuzzleGame("2", "fen2", "moves2", 1500));

        assertEquals(2, puzzleGameSet.size());
    }

    @Test
    public void testMoves() {
        PuzzleGame puzzleGame1 = new PuzzleGame("1", "fen1", "m1 m2 m3 m4 m5", 1500);

        assertFalse(puzzleGame1.isStarted());
        assertFalse(puzzleGame1.isSolutionFound());
        assertTrue(puzzleGame1.isCorrectNextMove("m1"));
        assertEquals("m1", puzzleGame1.getNextMove(false));
        assertFalse(puzzleGame1.isStarted());
        assertTrue(puzzleGame1.isCorrectNextMove("m1"));
        assertEquals("m1", puzzleGame1.getNextMove());
        assertTrue(puzzleGame1.isStarted());
        assertTrue(puzzleGame1.isCorrectNextMove("m2"));

        puzzleGame1.getNextMove();
        puzzleGame1.getNextMove();
        puzzleGame1.getNextMove();

        assertFalse(puzzleGame1.isSolutionFound());
        puzzleGame1.getNextMove();
        assertTrue(puzzleGame1.isStarted());
        assertTrue(puzzleGame1.isSolutionFound());

        assertEquals("", puzzleGame1.getNextMove());
        assertEquals("", puzzleGame1.getNextMove());
    }

    @Test
    public void testIsSolutionFoundWhenSolved() {
        PuzzleGame puzzleGame = new PuzzleGame("1", "fen1", "m1 m2 m3", 1500);
        puzzleGame.setSolved(true);
        assertTrue(puzzleGame.isSolutionFound());
    }

    @Test
    public void testIsSolutionFoundWhenAllMovesCompleted() {
        PuzzleGame puzzleGame = new PuzzleGame("1", "fen1", "m1 m2 m3", 1500);
        puzzleGame.getNextMove();
        puzzleGame.getNextMove();
        puzzleGame.getNextMove();
        assertTrue(puzzleGame.isSolutionFound());
    }

    @Test
    public void testIsSolutionFoundWhenNotSolved() {
        PuzzleGame puzzleGame = new PuzzleGame("1", "fen1", "m1 m2 m3", 1500);
        assertFalse(puzzleGame.isSolutionFound());
    }

    @Test
    public void testIsSolutionFoundWhenPartiallyCompleted() {
        PuzzleGame puzzleGame = new PuzzleGame("1", "fen1", "m1 m2 m3", 1500);
        puzzleGame.getNextMove();
        assertFalse(puzzleGame.isSolutionFound());
    }
}