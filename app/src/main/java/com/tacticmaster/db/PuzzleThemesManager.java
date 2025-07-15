package com.tacticmaster.db;

import static com.tacticmaster.db.PuzzleTable.COLUMN_SOLVED;
import static com.tacticmaster.db.PuzzleTable.COLUMN_THEMES;
import static com.tacticmaster.db.PuzzleTable.PUZZLE_TABLE_NAME;
import static java.util.Objects.isNull;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.tacticmaster.puzzle.PuzzleManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PuzzleThemesManager {

    private final Set<String> allThemes = ConcurrentHashMap.newKeySet();
    private final DatabaseAccessor databaseAccessor;
    private final PuzzleFilter puzzleFilter;
    private final ScheduledExecutorService scheduler;
    private static final long UPDATE_INTERVAL_SECONDS = 60;
    private final Set<String> selectedThemes = new HashSet<>();

    public PuzzleThemesManager(DatabaseAccessor databaseAccessor, boolean withPeriodicUpdate) {
        this.databaseAccessor = databaseAccessor;
        this.puzzleFilter = new PuzzleFilter();
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        if (withPeriodicUpdate) {
            startPeriodicUpdate();
        }
    }

    private void startPeriodicUpdate() {
        scheduler.scheduleWithFixedDelay(this::updateThemes, 0, UPDATE_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    private void updateThemes() {
        String query = "SELECT DISTINCT " + COLUMN_THEMES + " FROM " + PUZZLE_TABLE_NAME + " WHERE " + COLUMN_THEMES + " IS NOT NULL AND " + COLUMN_THEMES + " != '' AND " + COLUMN_SOLVED + " = 0";

        try (SQLiteDatabase db = databaseAccessor.getDbHelper().openDatabase(); Cursor cursor = db.rawQuery(query, null)) {
            Set<String> newThemes = ConcurrentHashMap.newKeySet();
            int themesIndex = cursor.getColumnIndex(COLUMN_THEMES);
            while (cursor.moveToNext()) {
                if (themesIndex >= 0) {
                    String themes = cursor.getString(themesIndex);
                    if (!isNull(themes) && !themes.isEmpty()) {
                        for (String theme : themes.split(" ")) {
                            if (!theme.isEmpty()) {
                                newThemes.add(theme);
                            }
                        }
                    }
                }
            }
            allThemes.clear();
            allThemes.addAll(newThemes);
        }
    }

    private void setDialogButtonColors(Context context, AlertDialog dialog) {
        int color = context.getResources().getColor(android.R.color.darker_gray, null);
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(color);
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(color);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(color);
    }

    Set<String> getPuzzleThemes() {
        if (allThemes.isEmpty()) {
            updateThemes();
        }
        return allThemes;
    }

    public void setThemes(Context context, PuzzleManager puzzleManager, MaterialButton filterButton, MaterialAutoCompleteTextView filterDropdown, Runnable callback) {
        var themesList = new ArrayList<>(puzzleFilter.getThemeGroups(getPuzzleThemes()).keySet());
        var adapter = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, themesList);

        filterDropdown.setAdapter(adapter);
        filterDropdown.setDropDownHeight(0);

        filterButton.setOnClickListener(v -> {
            boolean[] checkedItems = new boolean[themesList.size()];

            for (int i = 0; i < themesList.size(); i++) {
                checkedItems[i] = selectedThemes.contains(themesList.get(i));
            }

            var builder = new MaterialAlertDialogBuilder(context).setMultiChoiceItems(themesList.toArray(new String[0]), checkedItems, (dialog, which, checked) -> {
                        String item = themesList.get(which);
                        if (checked) {
                            selectedThemes.add(item);
                        } else {
                            selectedThemes.remove(item);
                        }
                    })
                    .setPositiveButton("Done", (dialog, which) -> {
                        Set<String> allThemesInGroup = new HashSet<>();
                        selectedThemes.forEach(theme -> {
                            var themeGroup = puzzleFilter.getThemeGroups(getPuzzleThemes()).get(theme);
                            if (!isNull(themeGroup)) {
                                allThemesInGroup.addAll(themeGroup);
                            }
                        });

                        puzzleManager.updatePuzzleThemes(allThemesInGroup);
                        callback.run();
                    })
                    .setNeutralButton("Clear All", (dialog, which) -> {
                        selectedThemes.clear();
                        filterButton.setText("");
                        puzzleManager.updatePuzzleThemes(selectedThemes);
                        callback.run();
                    })
                    .setNegativeButton("Cancel", null);

            AlertDialog dialog = builder.create();
            dialog.setOnShowListener(d -> setDialogButtonColors(context, dialog));
            dialog.show();
        });
    }
}
