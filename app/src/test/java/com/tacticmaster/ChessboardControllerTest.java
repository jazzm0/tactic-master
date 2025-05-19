package com.tacticmaster;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anySet;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tacticmaster.board.ChessboardView;
import com.tacticmaster.db.DatabaseAccessor;
import com.tacticmaster.puzzle.Puzzle;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ChessboardControllerTest {

    @Mock
    private DatabaseAccessor databaseAccessor;

    @Mock
    private ChessboardView chessboardView;

    @Mock
    private PuzzleTextViews puzzleTextViews;

    @Mock
    private Random randomNumberGenerator;

    @InjectMocks
    private ChessboardController chessboardController;

    private Puzzle puzzle;
    private List<Puzzle> puzzles;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(databaseAccessor.getPlayerRating()).thenReturn(2333);
        when(databaseAccessor.getPlayerAutoplay()).thenReturn(false);
        String fen = "1rb2rk1/q5P1/4p2p/3p3p/3P1P2/2P5/2QK3P/3R2R1 b - - 0 29";
        String moves = "f8f7 c2h7 g8h7 g7g8q";
        this.puzzle = new Puzzle("1", fen, moves, 1049);
        this.puzzles = new ArrayList<>();
        this.puzzles.add(puzzle);
        this.puzzles.add(new Puzzle("1", "fen1", "moves1", 1500));
        this.puzzles.add(new Puzzle("2", "fen2", "moves2", 1600));
        this.puzzles.add(new Puzzle("3", "fen3", "moves3", 1400));
        chessboardController = new ChessboardController(databaseAccessor, chessboardView, puzzleTextViews, randomNumberGenerator);

    }

    @Test
    public void testLoadNextPuzzles() {
        when(databaseAccessor.getPuzzlesWithinRange(anyInt(), anyInt(), anySet())).thenReturn(puzzles);
        when(databaseAccessor.getAllPuzzleCount()).thenReturn(256);
        when(databaseAccessor.getSolvedPuzzleCount()).thenReturn(5);
        when(databaseAccessor.getPlayerRating()).thenReturn(2333);

        chessboardController.loadNextPuzzle();

        verify(chessboardView).setPuzzle(puzzle);
        verify(puzzleTextViews).setPuzzleRating(puzzle.rating());
        verify(puzzleTextViews).setPuzzlesSolved(5, 256);
        verify(puzzleTextViews).setPlayerRating(2333);
    }

    @Test
    public void testLoadPreviousPuzzle() {
        // Mock the database to return a list of puzzles
        when(databaseAccessor.getPuzzlesWithinRange(anyInt(), anyInt(), anySet())).thenReturn(puzzles);

        // Load the first puzzle
        chessboardController.loadNextPuzzle();
        verify(chessboardView).setPuzzle(puzzles.get(0));

        // Load the second puzzle
        chessboardController.loadNextPuzzle();
        verify(chessboardView).setPuzzle(puzzles.get(3));

        // Go back to the previous puzzle
        chessboardController.loadPreviousPuzzle();
        verify(chessboardView, atLeastOnce()).setPuzzle(puzzles.get(0));

        // Wrap around to the last puzzle when going back from the first puzzle
        chessboardController.loadPreviousPuzzle();
        verify(chessboardView, atLeastOnce()).setPuzzle(puzzles.get(puzzles.size() - 1));
    }

    @Test
    public void testOnPuzzleSolved() {
        var newPuzzles = new ArrayList<Puzzle>();
        newPuzzles.add(new Puzzle("2", "fen", "moves", 1000));
        newPuzzles.add(new Puzzle("3", "fen", "moves", 1000));

        when(databaseAccessor.getPuzzlesWithinRange(anyInt(), anyInt(), anySet()))
                .thenReturn(puzzles)
                .thenReturn(newPuzzles);

        chessboardController.loadNextPuzzle();

        chessboardController.onPuzzleSolved(puzzle);

        verify(databaseAccessor).setSolved(puzzle.puzzleId());
        verify(databaseAccessor).storePlayerRating(anyInt());
        verify(puzzleTextViews, atLeastOnce()).setPlayerRating(anyInt());

        // check that chessboardController.loadNextPuzzle() was not executed again (because autoplay=false)
        verify(chessboardView, times(1)).setPuzzle(any());

        chessboardController.setAutoplay(true);
        chessboardController.onPuzzleSolved(puzzle);
        // check that chessboardController.loadNextPuzzle() was executed once more (because autoplay=true)
        verify(chessboardView, times(2)).setPuzzle(any());
    }

    @Test
    public void testOnPuzzleNotSolved() {
        when(databaseAccessor.getPuzzlesWithinRange(anyInt(), anyInt(), anySet())).thenReturn(puzzles);
        chessboardController.loadNextPuzzle();

        chessboardController.onPuzzleNotSolved(puzzle);

        verify(databaseAccessor).storePlayerRating(anyInt());
        verify(puzzleTextViews, atLeastOnce()).setPlayerRating(anyInt());
    }

}