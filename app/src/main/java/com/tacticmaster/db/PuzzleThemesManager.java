package com.tacticmaster.db;

import static com.tacticmaster.db.PuzzleTable.COLUMN_SOLVED;
import static com.tacticmaster.db.PuzzleTable.COLUMN_THEMES;
import static com.tacticmaster.db.PuzzleTable.PUZZLE_TABLE_NAME;
import static java.util.Objects.isNull;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PuzzleThemesManager {
    private final Set<String> allThemes = ConcurrentHashMap.newKeySet();
    private final DatabaseHelper dbHelper;
    private final ScheduledExecutorService scheduler;
    private static final long UPDATE_INTERVAL_SECONDS = 60;

    public PuzzleThemesManager(DatabaseHelper dbHelper, boolean withPeriodicUpdate) {
        this.dbHelper = dbHelper;
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

        try (SQLiteDatabase db = dbHelper.openDatabase(); Cursor cursor = db.rawQuery(query, null)) {
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

    public Set<String> getPuzzleThemes() {
        if (allThemes.isEmpty()) {
            updateThemes();
        }
        return allThemes;
    }
}
