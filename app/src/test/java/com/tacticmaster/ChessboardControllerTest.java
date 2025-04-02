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
        String fen = "1rb2rk1/q5P1/4p2p/3p3p/3P1P2/2P5/2QK3P/3R2R1 b - - 0 29";
        String moves = "f8f7 c2h7 g8h7 g7g8q";
        this.puzzle = new Puzzle("1", fen, moves, 1049, 80, 85, 208, "opening", "url", "tags");
        this.puzzles = new ArrayList<>();
        this.puzzles.add(puzzle);
        this.puzzles.add(new Puzzle("1", "fen1", "moves1", 1500, 100, 10, 1000, "themes1", "url1", "opening1"));
        this.puzzles.add(new Puzzle("2", "fen2", "moves2", 1600, 100, 10, 1000, "themes2", "url2", "opening2"));
        this.puzzles.add(new Puzzle("3", "fen3", "moves3", 1400, 100, 10, 1000, "themes3", "url3", "opening3"));
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
        chessboardController.loadNextPuzzle();

        chessboardController.onPuzzleSolved(puzzle);

        verify(databaseAccessor).setSolved(puzzle.puzzleId());
        verify(databaseAccessor).storePlayerRating(anyInt());
        verify(puzzleTextViews, atLeastOnce()).setPlayerRating(anyInt());
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