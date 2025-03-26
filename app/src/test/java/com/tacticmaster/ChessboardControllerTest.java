package com.tacticmaster;

import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anySet;
import static org.mockito.Mockito.atLeastOnce;
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

public class ChessboardControllerTest {

    @Mock
    private DatabaseAccessor databaseAccessor;

    @Mock
    private ChessboardView chessboardView;

    @Mock
    private PuzzleTextViews puzzleTextViews;

    @InjectMocks
    private ChessboardController chessboardController;

    private Puzzle puzzle;
    private List<Puzzle> puzzles;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(databaseAccessor.getPlayerRating()).thenReturn(2333);
        String fen = "1rb2rk1/q5P1/4p2p/3p3p/3P1P2/2P5/2QK3P/3R2R1 b - - 0 29";
        String moves = "f8f7 c2h7 g8h7 g7g8q";
        this.puzzle = new Puzzle("1", fen, moves, 1049, 80, 85, 208, "opening", "url", "tags");
        chessboardController = new ChessboardController(databaseAccessor, chessboardView, puzzleTextViews);
        this.puzzles = new ArrayList<>();
        this.puzzles.add(puzzle);
    }

    @Test
    public void testLoadNextPuzzles() {
        when(databaseAccessor.getPuzzlesWithinRange(anyInt(), anyInt(), anySet())).thenReturn(puzzles);
        when(databaseAccessor.getAllPuzzleCount()).thenReturn(256);
        when(databaseAccessor.getSolvedPuzzleCount()).thenReturn(5);
        when(databaseAccessor.getPlayerRating()).thenReturn(2333);

        chessboardController.loadNextPuzzles();

        verify(chessboardView).setPuzzle(puzzle);
        verify(puzzleTextViews).setPuzzleId(puzzle.puzzleId());
        verify(puzzleTextViews).setPuzzleRating(puzzle.rating());
        verify(puzzleTextViews).setPuzzlesSolved(5, 256);
        verify(puzzleTextViews).setPuzzleThemes(puzzle.themes());
        verify(puzzleTextViews).setPuzzleMoves(puzzle.moves());
        verify(puzzleTextViews).setPuzzlePopularity(puzzle.popularity());
        verify(puzzleTextViews).setPuzzleNbPlays(puzzle.nbPlays());
        verify(puzzleTextViews).setPlayerRating(2333);
    }

    @Test
    public void testOnPuzzleSolved() {
        var newPuzzles = new ArrayList<Puzzle>();
        newPuzzles.add(new Puzzle("2", "fen", "moves", 1000, 80, 85, 208, "opening", "url", "tags"));
        newPuzzles.add(new Puzzle("3", "fen", "moves", 1000, 80, 85, 208, "opening", "url", "tags"));

        when(databaseAccessor.getPuzzlesWithinRange(anyInt(), anyInt(), anySet()))
                .thenReturn(puzzles)
                .thenReturn(newPuzzles);
        chessboardController.loadNextPuzzles();

        chessboardController.onPuzzleSolved(puzzle);

        verify(databaseAccessor).setSolved(puzzle.puzzleId());
        verify(databaseAccessor).storePlayerRating(anyInt());
        verify(puzzleTextViews, atLeastOnce()).setPlayerRating(anyInt());
    }

    @Test
    public void testOnPuzzleNotSolved() {
        when(databaseAccessor.getPuzzlesWithinRange(anyInt(), anyInt(), anySet())).thenReturn(puzzles);
        chessboardController.loadNextPuzzles();

        chessboardController.onPuzzleNotSolved(puzzle);

        verify(databaseAccessor).storePlayerRating(anyInt());
        verify(puzzleTextViews, atLeastOnce()).setPlayerRating(anyInt());
    }
}