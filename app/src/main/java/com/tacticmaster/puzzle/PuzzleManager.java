package com.tacticmaster.puzzle;


import static java.util.Objects.isNull;

import com.tacticmaster.db.DatabaseAccessor;
import com.tacticmaster.db.PuzzleThemesDialogHelper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;

public class PuzzleManager implements PuzzleThemesDialogHelper.PuzzleThemesListener {

    private final DatabaseAccessor databaseAccessor;
    private final Set<String> puzzleThemes = new TreeSet<>();
    private final Map<String, PuzzleGame> puzzles = new LinkedHashMap<>();
    private int currentIndex = -1;
    private int rating = 0;

    public PuzzleManager(DatabaseAccessor databaseAccessor) {
        this.databaseAccessor = databaseAccessor;
        this.rating = databaseAccessor.getPlayerRating();
    }

    public void updateRating(int rating) {
        this.rating = rating;
    }

    int getRating() {
        return this.rating;
    }

    public PuzzleGame getCurrentPuzzle() {
        if (currentIndex < 0 || currentIndex >= puzzles.size()) {
            throw new NoSuchElementException("Invalid puzzle index");
        }
        return puzzles.get(getPuzzleIdByIndex(currentIndex));
    }

    public void moveToNextPuzzle() {
        currentIndex++;
        if (currentIndex >= puzzles.size()) {
            try {
                loadNextPuzzles();
            } catch (NoSuchElementException e) {
                currentIndex--;
                throw e;
            }
        }
    }

    @Override
    public void onThemesUpdated(Set<String> themes) {
        puzzleThemes.clear();
        if (!isNull(themes) && !themes.isEmpty()) {
            puzzleThemes.addAll(themes);
        }
        currentIndex = puzzles.size();
        moveToNextPuzzle();
    }

    public void moveToPreviousPuzzle() {
        if (puzzles.isEmpty()) {
            loadNextPuzzles();
        }
        currentIndex = (currentIndex - 1 + puzzles.size()) % puzzles.size();
    }

    public void loadPuzzleById(String puzzleId) {
        if (!puzzles.containsKey(puzzleId)) {
            Puzzle nextPuzzle = databaseAccessor.getPuzzleById(puzzleId);
            puzzles.put(nextPuzzle.puzzleId(), new PuzzleGame(nextPuzzle));
        }
        currentIndex = getPuzzleIndexById(puzzleId);
    }

    private void loadNextPuzzles() throws NoSuchElementException {
        var lowestRating = rating - 50;
        var highestRating = rating + 50;
        List<Puzzle> nextPuzzles = new ArrayList<>();
        while (nextPuzzles.isEmpty() && lowestRating > 0) {
            nextPuzzles = databaseAccessor.getPuzzlesWithinRange(lowestRating, highestRating, puzzles.keySet(), puzzleThemes);
            lowestRating -= 50;
            highestRating += 50;
        }
        if (nextPuzzles.isEmpty()) {
            throw new NoSuchElementException("No more unsolved puzzles available");
        }
        nextPuzzles.forEach(puzzle -> puzzles.put(puzzle.puzzleId(), new PuzzleGame(puzzle)));
    }

    private String getPuzzleIdByIndex(int index) {
        if (currentIndex < 0 || currentIndex >= puzzles.size()) {
            return null;
        }
        return puzzles.keySet().toArray(new String[0])[index];
    }

    private int getPuzzleIndexById(String puzzleId) {
        var index = 0;
        for (String id : puzzles.keySet()) {
            if (id.equals(puzzleId)) {
                return index;
            }
            index++;
        }
        return -1;
    }
}
