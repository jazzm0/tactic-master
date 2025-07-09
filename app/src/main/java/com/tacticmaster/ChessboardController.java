package com.tacticmaster;

import static java.util.Objects.isNull;

import android.content.Intent;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.tacticmaster.board.ChessboardView;
import com.tacticmaster.db.DatabaseAccessor;
import com.tacticmaster.db.PuzzleFilter;
import com.tacticmaster.puzzle.PuzzleGame;
import com.tacticmaster.puzzle.PuzzleManager;
import com.tacticmaster.rating.EloRatingCalculator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;

public class ChessboardController implements ChessboardView.PuzzleFinishedListener {

    private final DatabaseAccessor databaseAccessor;
    private final ChessboardView chessboardView;
    private final PuzzleTextViews puzzleTextViews;
    private final Set<String> selectedThemes = new HashSet<>();
    private final PuzzleManager puzzleManager;
    private final PuzzleFilter puzzleFilter;
    private int playerRating;
    private boolean autoplay;

    public ChessboardController(
            DatabaseAccessor databaseAccessor,
            ChessboardView chessboardView,
            PuzzleTextViews puzzleTextViews) {
        this.databaseAccessor = databaseAccessor;
        this.puzzleManager = new PuzzleManager(databaseAccessor, databaseAccessor.getPlayerRating());
        this.chessboardView = chessboardView;
        this.puzzleTextViews = puzzleTextViews;
        this.chessboardView.setPuzzleSolvedListener(this);
        this.puzzleFilter = new PuzzleFilter(databaseAccessor);
        this.playerRating = databaseAccessor.getPlayerRating();
        this.autoplay = databaseAccessor.getPlayerAutoplay();
    }

    private void updatePlayerRating(int opponentRating, double result) {
        var newRating = EloRatingCalculator.calculateNewRating(playerRating, opponentRating, result);
        databaseAccessor.storePlayerRating(newRating);
        puzzleTextViews.updatePlayerRating(playerRating, newRating);
        this.playerRating = newRating;
        puzzleManager.updateRating(newRating);
    }

    private void setDialogButtonColors(AlertDialog dialog) {
        int color = chessboardView.getContext().getResources().getColor(android.R.color.darker_gray, null);
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(color);
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(color);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(color);
    }

    public void setThemes(Set<String> themes) {
        var themesList = new ArrayList<>(themes);
        var adapter = new ArrayAdapter<>(chessboardView.getContext(), android.R.layout.simple_dropdown_item_1line, themesList);
        puzzleTextViews.getFilterDropdown().setAdapter(adapter);
        puzzleTextViews.getFilterDropdown().setDropDownHeight(0);

        puzzleTextViews.getFilterButton().setOnClickListener(v -> {
            boolean[] checkedItems = new boolean[themesList.size()];

            for (int i = 0; i < themesList.size(); i++) {
                checkedItems[i] = selectedThemes.contains(themesList.get(i));
            }

            var builder = new MaterialAlertDialogBuilder(chessboardView.getContext()).setMultiChoiceItems(themesList.toArray(new String[0]), checkedItems, (dialog, which, checked) -> {
                        String item = themesList.get(which);
                        if (checked) {
                            selectedThemes.add(item);
                        } else {
                            selectedThemes.remove(item);
                        }
                    })
                    .setPositiveButton("Done", (dialog, which) -> {
                        Set<String> allThemesInGroup = new HashSet<>();
                        selectedThemes.stream().forEach(theme -> {
                            var themeGroup = puzzleFilter.getThemeGroups().get(theme);
                            if (!isNull(themeGroup)) {
                                allThemesInGroup.addAll(themeGroup);
                            }
                        });

                        puzzleManager.updatePuzzleThemes(allThemesInGroup);
                        renderPuzzle();
                    })
                    .setNeutralButton("Clear All", (dialog, which) -> {
                        selectedThemes.clear();
                        puzzleTextViews.getFilterDropdown().setText("");
                        puzzleManager.updatePuzzleThemes(selectedThemes);
                        renderPuzzle();
                    })
                    .setNegativeButton("Cancel", null);

            AlertDialog dialog = builder.create();
            dialog.setOnShowListener(d -> setDialogButtonColors(dialog));
            dialog.show();
        });
    }

    public void renderPuzzle() {
        var puzzle = puzzleManager.getCurrentPuzzle();
        chessboardView.setPuzzle(puzzle);

        puzzleTextViews.setPuzzleId(puzzle.getPuzzleId());
        puzzleTextViews.setPuzzleRating(puzzle.rating());
        puzzleTextViews.setPuzzlesSolvedCount(databaseAccessor.getSolvedPuzzleCount(), databaseAccessor.getAllPuzzleCount());
        puzzleTextViews.setPlayerRating(playerRating);
        puzzleTextViews.setPuzzleSolved(puzzle.solved());
        setThemes(puzzleFilter.getThemeGroups().keySet());
    }

    public void loadPreviousPuzzle() {
        puzzleManager.moveToPreviousPuzzle();
        renderPuzzle();
    }

    public void loadNextPuzzle() {
        try {
            puzzleManager.moveToNextPuzzle();
            renderPuzzle();
        } catch (NoSuchElementException e) {
            chessboardView.makeText(R.string.no_more_puzzles);
        }
    }

    public void loadPuzzleById(String puzzleId) {
        try {
            puzzleManager.loadPuzzleById(puzzleId);
            renderPuzzle();
        } catch (NoSuchElementException e) {
            chessboardView.makeText(R.string.invalid_puzzle_id);
        }
    }

    public void puzzleIdLinkClicked() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "https://lichess.org/training/" + puzzleManager.getCurrentPuzzle().getPuzzleId());
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, null);
        chessboardView.getContext().startActivity(shareIntent);
    }

    public void puzzleHintClicked() {
        chessboardView.puzzleHintClicked();
    }

    public void setAutoplay(boolean isChecked) {
        this.autoplay = isChecked;
        databaseAccessor.storePlayerAutoplay(isChecked);
    }

    public boolean getAutoplay() {
        return this.autoplay;
    }

    @Override
    public void onPuzzleSolved(PuzzleGame puzzle) {
        if (databaseAccessor.wasNotSolved(puzzle.getPuzzleId())) {
            databaseAccessor.setSolved(puzzle.getPuzzleId());
            updatePlayerRating(puzzle.rating(), 1.0);
            puzzle.setSolved(true);
            puzzleTextViews.setPuzzleSolved(true);
        }
    }

    @Override
    public void onPuzzleNotSolved(PuzzleGame puzzle) {
        if (databaseAccessor.wasNotSolved(puzzle.getPuzzleId())) {
            updatePlayerRating(puzzle.rating(), 0.0);
        }
    }

    @Override
    public void onAfterPuzzleFinished(PuzzleGame puzzle) {
        if (this.autoplay) {
            loadNextPuzzle();
        }
    }
}