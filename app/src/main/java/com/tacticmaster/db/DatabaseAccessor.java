package com.tacticmaster.db;

import static com.tacticmaster.db.PlayerTable.COLUMN_PLAYER_RATING;
import static com.tacticmaster.db.PlayerTable.DEFAULT_PLAYER_RATING;
import static com.tacticmaster.db.PlayerTable.PLAYER_TABLE_NAME;
import static com.tacticmaster.db.PuzzleTable.COLUMN_PUZZLE_ID;
import static com.tacticmaster.db.PuzzleTable.COLUMN_RATING;
import static com.tacticmaster.db.PuzzleTable.COLUMN_SOLVED;
import static com.tacticmaster.db.PuzzleTable.PUZZLE_TABLE_NAME;

import android.content.ContentValues;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.tacticmaster.puzzle.Puzzle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DatabaseAccessor {

    private final DatabaseHelper dbHelper;

    public DatabaseAccessor(ContextWrapper context) {
        dbHelper = new DatabaseHelper(context);
        try {
            dbHelper.createDatabase();
        } catch (IOException e) {
            Log.e("The following error occurred: ", e.getMessage());
        }
    }

    public void setSolved(String puzzleId) {
        SQLiteDatabase db = dbHelper.openDatabase();
        db.execSQL("UPDATE " + PUZZLE_TABLE_NAME + " SET solved = 1 WHERE " + COLUMN_PUZZLE_ID + " = '" + puzzleId + "'");
    }

    public int getSolvedPuzzleCount() {
        SQLiteDatabase db = dbHelper.openDatabase();
        try (Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + PUZZLE_TABLE_NAME + " WHERE " + COLUMN_SOLVED + " = 1", null)) {
            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        }
        return 0;
    }

    public int getAllPuzzleCount() {
        SQLiteDatabase db = dbHelper.openDatabase();
        try (Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + PUZZLE_TABLE_NAME, null)) {
            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        }
        return 0;
    }

    public List<Puzzle> getPuzzlesWithinRange(int lowestRating, int highestRating, Set<String> excludedPuzzleIds) {
        SQLiteDatabase db = dbHelper.openDatabase();
        StringBuilder queryBuilder = new StringBuilder("SELECT * FROM " + PUZZLE_TABLE_NAME +
                " WHERE " + COLUMN_RATING + " >= " + lowestRating +
                " AND " + COLUMN_RATING + " <= " + highestRating +
                " AND " + COLUMN_SOLVED + " = 0");

        if (!excludedPuzzleIds.isEmpty()) {
            queryBuilder.append(" AND ").append(COLUMN_PUZZLE_ID).append(" NOT IN (");
            for (String id : excludedPuzzleIds) {
                queryBuilder.append("'").append(id).append("',");
            }
            queryBuilder.setLength(queryBuilder.length() - 1); // Remove the trailing comma
            queryBuilder.append(")");
        }

        queryBuilder.append(" ORDER BY ").append(COLUMN_RATING).append(" ASC LIMIT 5");

        return executeQuery(db, queryBuilder.toString());
    }

    private List<Puzzle> executeQuery(SQLiteDatabase db, String query) {
        List<Puzzle> puzzles = new ArrayList<>();
        try (Cursor cursor = db.rawQuery(query, null)) {
            int puzzleIdIndex = cursor.getColumnIndex(COLUMN_PUZZLE_ID);
            int fenIndex = cursor.getColumnIndex(PuzzleTable.COLUMN_FEN);
            int movesIndex = cursor.getColumnIndex(PuzzleTable.COLUMN_MOVES);
            int ratingIndex = cursor.getColumnIndex(COLUMN_RATING);
            int ratingDeviationIndex = cursor.getColumnIndex(PuzzleTable.COLUMN_RATING_DEVIATION);
            int popularityIndex = cursor.getColumnIndex(PuzzleTable.COLUMN_POPULARITY);
            int nbPlaysIndex = cursor.getColumnIndex(PuzzleTable.COLUMN_NB_PLAYS);
            int themesIndex = cursor.getColumnIndex(PuzzleTable.COLUMN_THEMES);
            int gameUrlIndex = cursor.getColumnIndex(PuzzleTable.COLUMN_GAME_URL);
            int openingTagsIndex = cursor.getColumnIndex(PuzzleTable.COLUMN_OPENING_TAGS);
            while (cursor.moveToNext()) {
                if (puzzleIdIndex >= 0 && fenIndex >= 0 && movesIndex >= 0 && ratingIndex >= 0 &&
                        ratingDeviationIndex >= 0 && popularityIndex >= 0 && nbPlaysIndex >= 0 &&
                        themesIndex >= 0 && gameUrlIndex >= 0 && openingTagsIndex >= 0) {

                    puzzles.add(new Puzzle(
                            cursor.getString(puzzleIdIndex),
                            cursor.getString(fenIndex),
                            cursor.getString(movesIndex),
                            cursor.getInt(ratingIndex),
                            cursor.getInt(ratingDeviationIndex),
                            cursor.getInt(popularityIndex),
                            cursor.getInt(nbPlaysIndex),
                            cursor.getString(themesIndex),
                            cursor.getString(gameUrlIndex),
                            cursor.getString(openingTagsIndex)
                    ));
                }
            }
        }
        return puzzles;
    }

    public void storePlayerRating(int rating) {
        SQLiteDatabase db = dbHelper.openDatabase();
        db.execSQL("DELETE FROM " + PLAYER_TABLE_NAME); // Delete any existing rows
        ContentValues values = new ContentValues();
        values.put(COLUMN_PLAYER_RATING, rating);
        db.insert(PLAYER_TABLE_NAME, null, values);
    }

    public int getPlayerRating() {
        SQLiteDatabase db = dbHelper.openDatabase();
        try (Cursor cursor = db.rawQuery("SELECT * FROM " + PLAYER_TABLE_NAME, null)) {
            if (cursor.moveToFirst()) {
                int ratingIndex = cursor.getColumnIndex(COLUMN_PLAYER_RATING);
                return cursor.getInt(ratingIndex);
            }
        }
        return DEFAULT_PLAYER_RATING;
    }
}