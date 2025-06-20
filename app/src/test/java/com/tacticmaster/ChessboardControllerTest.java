package com.tacticmaster;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.Intent;

import com.tacticmaster.board.ChessboardView;
import com.tacticmaster.db.DatabaseAccessor;
import com.tacticmaster.puzzle.Puzzle;
import com.tacticmaster.puzzle.PuzzleGame;

import org.junit.jupiter.api.Assertions;
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

    private PuzzleGame puzzleGame;
    private Puzzle puzzleRecord;
    private List<PuzzleGame> puzzleGames;
    private List<Puzzle> puzzleRecords;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(databaseAccessor.getPlayerRating()).thenReturn(2333);
        when(databaseAccessor.getPlayerAutoplay()).thenReturn(false);
        String fen = "1rb2rk1/q5P1/4p2p/3p3p/3P1P2/2P5/2QK3P/3R2R1 b - - 0 29";
        String moves = "f8f7 c2h7 g8h7 g7g8q";
        this.puzzleGame = new PuzzleGame("1", fen, moves, 1049);
        this.puzzleRecord = new Puzzle("1", fen, moves, 1049);
        this.puzzleGames = new ArrayList<>();
        this.puzzleRecords = new ArrayList<>();
        this.puzzleRecords.add(this.puzzleRecord);
        this.puzzleGames.add(this.puzzleGame);
        this.puzzleRecords.add(new Puzzle("2", "fen1", "moves1", 1500));
        this.puzzleGames.add(new PuzzleGame("2", "fen1", "moves1", 1500));
        this.puzzleRecords.add(new Puzzle("3", "fen2", "moves2", 1600));
        this.puzzleGames.add(new PuzzleGame("3", "fen2", "moves2", 1600));
        this.puzzleRecords.add(new Puzzle("4", "fen3", "moves3", 1400));
        this.puzzleGames.add(new PuzzleGame("4", "fen3", "moves3", 1400));
        chessboardController = new ChessboardController(databaseAccessor, chessboardView, puzzleTextViews);

    }

    @Test
    public void testLoadNextPuzzles() {
        when(databaseAccessor.getAllPuzzleCount()).thenReturn(256);
        when(databaseAccessor.getSolvedPuzzleCount()).thenReturn(5);
        when(databaseAccessor.getPlayerRating()).thenReturn(2333);
        when(databaseAccessor.getPuzzlesWithinRange(anyInt(), anyInt(), anySet(), anySet())).thenReturn(new ArrayList<>()).thenReturn(new ArrayList<>()).thenReturn(puzzleRecords);

        chessboardController.loadNextPuzzle();

        verify(databaseAccessor, times(3)).getPuzzlesWithinRange(anyInt(), anyInt(), anySet(), anySet());
        verify(chessboardView).setPuzzle(puzzleGame);
        verify(puzzleTextViews).setPuzzleId(puzzleGame.getPuzzleId());
        verify(puzzleTextViews).setPuzzleRating(puzzleGame.rating());
        verify(puzzleTextViews).setPuzzlesSolvedCount(5, 256);
        verify(puzzleTextViews).setPlayerRating(2333);
    }

    @Test
    public void testLoadNextPuzzlesNoUnsolvedLeft() {
        when(databaseAccessor.getAllPuzzleCount()).thenReturn(256);
        when(databaseAccessor.getSolvedPuzzleCount()).thenReturn(5);
        when(databaseAccessor.getPlayerRating()).thenReturn(2333);
        when(databaseAccessor.getPuzzlesWithinRange(anyInt(), anyInt(), anySet(), anySet())).thenReturn(new ArrayList<>());

        chessboardController.loadNextPuzzle();

        // verify(databaseAccessor, atLeast(10)).getPuzzlesWithinRange(anyInt(), anyInt(), anyList());
        verify(chessboardView, never()).setPuzzle(any());
        verify(puzzleTextViews, never()).setPuzzleId(any());
    }

    @Test
    public void testLoadPuzzleById() {
        when(databaseAccessor.getPuzzleById("1")).thenReturn(puzzleRecord);

        // load puzzle that is not already in the loaded puzzles-List
        chessboardController.loadPuzzleById("1");

        verify(chessboardView, times(1)).setPuzzle(puzzleGame);
        verify(puzzleTextViews, times(1)).setPuzzleId(puzzleGame.getPuzzleId());
        verify(puzzleTextViews, times(1)).setPuzzleRating(puzzleGame.rating());

        puzzleGame.getNextMove();
        // load puzzle that is already in the loaded puzzles-List
        chessboardController.loadPuzzleById("1");

        verify(chessboardView, times(2)).setPuzzle(any());
        verify(puzzleTextViews, times(2)).setPuzzleId(puzzleGame.getPuzzleId());
        verify(puzzleTextViews, times(2)).setPuzzleRating(puzzleGame.rating());
    }

    @Test
    public void testLoadPreviousPuzzle() {
        // Mock the database to return a list of puzzles
        when(databaseAccessor.getPuzzlesWithinRange(anyInt(), anyInt(), anySet(), anySet())).thenReturn(puzzleRecords);

        // Load the first puzzle
        chessboardController.loadNextPuzzle();
        verify(chessboardView).setPuzzle(puzzleGames.get(0));

        // Load the second puzzle
        chessboardController.loadNextPuzzle();
        verify(chessboardView).setPuzzle(puzzleGames.get(1));

        // Go back to the previous puzzle
        chessboardController.loadPreviousPuzzle();
        verify(chessboardView, times(2)).setPuzzle(puzzleGames.get(0));

        // Wrap around to the last puzzle when going back from the first puzzle
        chessboardController.loadPreviousPuzzle();
        verify(chessboardView, atLeastOnce()).setPuzzle(puzzleGames.get(puzzleGames.size() - 1));
    }

    @Test
    public void testOnPuzzleSolved() {
        var newPuzzles = new ArrayList<Puzzle>();
        newPuzzles.add(new Puzzle("2", "fen", "moves", 1000));
        newPuzzles.add(new Puzzle("3", "fen", "moves", 1000));

        when(databaseAccessor.getPuzzlesWithinRange(anyInt(), anyInt(), anySet(), anySet()))
                .thenReturn(puzzleRecords)
                .thenReturn(newPuzzles);

        when(databaseAccessor.wasNotSolved(any())).thenReturn(true);

        chessboardController.loadNextPuzzle();

        chessboardController.onPuzzleSolved(puzzleGame);

        verify(databaseAccessor).setSolved(puzzleGame.getPuzzleId());
        verify(databaseAccessor).storePlayerRating(anyInt());
        verify(puzzleTextViews, atLeastOnce()).setPlayerRating(anyInt());

        chessboardController.onAfterPuzzleFinished(puzzleGame);

        // check that chessboardController.loadNextPuzzle() was not executed again (because autoplay=false)
        verify(chessboardView, times(1)).setPuzzle(any());

        chessboardController.setAutoplay(true);
        chessboardController.onPuzzleSolved(puzzleGame);
        chessboardController.onAfterPuzzleFinished(puzzleGame);
        // check that chessboardController.loadNextPuzzle() was executed once more (because autoplay=true)
        verify(chessboardView, times(2)).setPuzzle(any());
    }

    @Test
    public void testOnPuzzleNotSolved() {
        when(databaseAccessor.getPuzzlesWithinRange(anyInt(), anyInt(), anySet(), anySet())).thenReturn(puzzleRecords);
        chessboardController.loadNextPuzzle();
        when(databaseAccessor.wasNotSolved(any())).thenReturn(true);

        chessboardController.onPuzzleNotSolved(puzzleGame);

        verify(databaseAccessor).storePlayerRating(anyInt());
        verify(puzzleTextViews, atLeastOnce()).setPlayerRating(anyInt());
    }

    @Test
    public void testOnPuzzleSolvedUpdatesSolvedState() {
        when(databaseAccessor.getPuzzlesWithinRange(anyInt(), anyInt(), anySet(), anySet())).thenReturn(puzzleRecords);
        chessboardController.loadNextPuzzle();
        Assertions.assertFalse(puzzleGame.solved());
        when(databaseAccessor.wasNotSolved(puzzleGame.getPuzzleId())).thenReturn(true);

        chessboardController.onPuzzleSolved(puzzleGame);

        verify(databaseAccessor).setSolved(puzzleGame.getPuzzleId());
        Assertions.assertTrue(puzzleGame.solved());
    }

    @Test
    public void testLoadPuzzleByIdDisplaysSolvedState() {
        when(databaseAccessor.getPuzzleById("1")).thenReturn(new Puzzle("1", "fen", "moves", 1000, "endgame", true));

        chessboardController.loadPuzzleById("1");

        verify(puzzleTextViews).setPuzzleSolved(true);
    }

    @Test
    public void testPuzzleIdLinkClicked() {
        String puzzleId = "12345";
        when(databaseAccessor.getPuzzleById(puzzleId)).thenReturn(new Puzzle(puzzleId, "fen", "moves", 1000));
        when(chessboardView.getContext()).thenReturn(mock(Context.class));
        chessboardController.loadPuzzleById(puzzleId);

        chessboardController.puzzleIdLinkClicked();

        Intent expectedIntent = new Intent(Intent.ACTION_SEND);
        expectedIntent.putExtra(Intent.EXTRA_TEXT, "https://lichess.org/training/" + puzzleId);
        expectedIntent.setType("text/plain");

        verify(chessboardView.getContext()).startActivity(Intent.createChooser(expectedIntent, null));
    }
}