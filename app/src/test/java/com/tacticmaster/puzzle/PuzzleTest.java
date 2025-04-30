package com.tacticmaster.puzzle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.TreeSet;

public class PuzzleTest {

    @Test
    public void testCompareTo() {
        Puzzle puzzle1 = new Puzzle("1", "fen1", "moves1", 1500);
        Puzzle puzzle2 = new Puzzle("2", "fen2", "moves2", 1600);
        Puzzle puzzle3 = new Puzzle("3", "fen3", "moves3", 1400);

        assertTrue(puzzle1.compareTo(puzzle2) < 0);

        assertTrue(puzzle2.compareTo(puzzle3) > 0);

        assertTrue(puzzle1.compareTo(puzzle3) > 0);

        assertEquals(0, puzzle1.compareTo(new Puzzle("1", "fen1", "moves1", 1500)));
    }

    @Test
    public void treeSet() {
        var puzzleSet = new java.util.TreeSet<Puzzle>();
        puzzleSet.add(new Puzzle("1", "fen1", "moves1", 1500));
        puzzleSet.add(new Puzzle("2", "fen2", "moves2", 1600));
        puzzleSet.add(new Puzzle("3", "fen3", "moves3", 1400));

        assertEquals(1400, puzzleSet.first().rating());
        assertEquals(1600, puzzleSet.last().rating());

        puzzleSet.pollLast();

        assertEquals(1400, puzzleSet.first().rating());
        assertEquals(1500, puzzleSet.last().rating());
    }

    @Test
    public void testSameRatingDifferentId() {
        var puzzleSet = new TreeSet<Puzzle>();
        puzzleSet.add(new Puzzle("1", "fen1", "moves1", 1500));
        puzzleSet.add(new Puzzle("2", "fen2", "moves2", 1500));

        assertEquals(2, puzzleSet.size());
    }
}