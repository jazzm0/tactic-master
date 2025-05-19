package com.tacticmaster.db;

import static com.tacticmaster.db.PlayerTable.COLUMN_AUTOPLAY_ENABLED;
import static com.tacticmaster.db.PlayerTable.COLUMN_PLAYER_ID;
import static com.tacticmaster.db.PlayerTable.COLUMN_PLAYER_RATING;
import static com.tacticmaster.db.PlayerTable.DEFAULT_PLAYER_RATING;
import static com.tacticmaster.db.PlayerTable.PLAYER_TABLE_NAME;
import static com.tacticmaster.db.PuzzleTable.COLUMN_PUZZLE_ID;
import static com.tacticmaster.db.PuzzleTable.COLUMN_RATING;
import static com.tacticmaster.db.PuzzleTable.COLUMN_SOLVED;
import static com.tacticmaster.db.PuzzleTable.PUZZLE_TABLE_NAME;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.tacticmaster.puzzle.Puzzle;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DatabaseAccessor {

    private final DatabaseHelper dbHelper;

    public DatabaseAccessor(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
        try {
            dbHelper.createDatabase();
        } catch (Error e) {
            Log.e("The following error occurred: ", e.getMessage());
        }
    }

    public void setSolved(String puzzleId) {
        SQLiteDatabase db = dbHelper.openDatabase();
        db.execSQL("UPDATE " + PUZZLE_TABLE_NAME + " SET " + COLUMN_SOLVED + " = 1 WHERE " + COLUMN_PUZZLE_ID + " = ?", new String[]{puzzleId});
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
            queryBuilder.setLength(queryBuilder.length() - 1);
            queryBuilder.append(")");
        }

        queryBuilder.append(" GROUP BY ").append(COLUMN_RATING).append(" LIMIT 5");

        return executeQuery(db, queryBuilder.toString(), null);
    }

    public Puzzle getPuzzleById(String puzzleId) {
        SQLiteDatabase db = dbHelper.openDatabase();
        String query = "SELECT * FROM " + PUZZLE_TABLE_NAME +
                " WHERE " + COLUMN_PUZZLE_ID + " = ?";
        List<Puzzle> puzzles = executeQuery(db, query, new String[]{puzzleId});
        if(puzzles.isEmpty()){
            throw new RuntimeException("Puzzle ID  not found");
        }
        return puzzles.get(0);
    }

    private List<Puzzle> executeQuery(SQLiteDatabase db, String query, String[] selectionArgs) {
        List<Puzzle> puzzles = new ArrayList<>();
        try (Cursor cursor = db.rawQuery(query, selectionArgs)) {
            int puzzleIdIndex = cursor.getColumnIndex(COLUMN_PUZZLE_ID);
            int fenIndex = cursor.getColumnIndex(PuzzleTable.COLUMN_FEN);
            int movesIndex = cursor.getColumnIndex(PuzzleTable.COLUMN_MOVES);
            int ratingIndex = cursor.getColumnIndex(COLUMN_RATING);
            while (cursor.moveToNext()) {
                if (puzzleIdIndex >= 0 && fenIndex >= 0 && movesIndex >= 0 && ratingIndex >= 0) {

                    puzzles.add(new Puzzle(
                            cursor.getString(puzzleIdIndex),
                            cursor.getString(fenIndex),
                            cursor.getString(movesIndex),
                            cursor.getInt(ratingIndex)
                    ));
                }
            }
        }
        return puzzles;
    }

    public void storePlayerRating(int rating) {
        SQLiteDatabase db = dbHelper.openDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PLAYER_RATING, rating);
        db.update(PLAYER_TABLE_NAME, values, COLUMN_PLAYER_ID + " = 1", null);
    }

    public int getPlayerRating() {
        SQLiteDatabase db = dbHelper.openDatabase();
        try (Cursor cursor = db.rawQuery("SELECT " + COLUMN_PLAYER_RATING + " FROM " + PLAYER_TABLE_NAME, null)) {
            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        }
        return DEFAULT_PLAYER_RATING;
    }

    public void storePlayerAutoplay(boolean isChecked) {
        SQLiteDatabase db = dbHelper.openDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_AUTOPLAY_ENABLED, isChecked ? 1 : 0);
        db.update(PLAYER_TABLE_NAME, values, COLUMN_PLAYER_ID + " = 1", null);
    }

    public boolean getPlayerAutoplay() {
        SQLiteDatabase db = dbHelper.openDatabase();
        try (Cursor cursor = db.rawQuery("SELECT " + COLUMN_AUTOPLAY_ENABLED + " FROM " + PLAYER_TABLE_NAME, null)) {
            if (cursor.moveToFirst()) {
                return cursor.getInt(0) == 1;
            }
        }
        return true;
    }
}