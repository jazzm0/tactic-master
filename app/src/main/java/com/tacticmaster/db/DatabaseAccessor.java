package com.tacticmaster.db;

import static com.tacticmaster.puzzle.PuzzleTable.COLUMN_PUZZLE_ID;
import static com.tacticmaster.puzzle.PuzzleTable.COLUMN_RATING;
import static com.tacticmaster.puzzle.PuzzleTable.COLUMN_SOLVED;

import android.content.ContextWrapper;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.tacticmaster.puzzle.Puzzle;
import com.tacticmaster.puzzle.PuzzleTable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DatabaseAccessor {


    public static final String TABLE_NAME = "lichess_db_puzzle";
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
        db.execSQL("UPDATE " + TABLE_NAME + " SET solved = 1 WHERE " + COLUMN_PUZZLE_ID + " = '" + puzzleId + "'");
    }

    public int getSolvedPuzzleCount() {
        SQLiteDatabase db = dbHelper.openDatabase();
        try (Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE " + COLUMN_SOLVED + " = 1", null)) {
            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        }
        return 0;
    }


    public int getAllPuzzleCount() {
        SQLiteDatabase db = dbHelper.openDatabase();
        try (Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_NAME, null)) {
            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        }
        return 0;
    }

    public List<Puzzle> getPuzzlesWithRatingGreaterThan(int rating) {
        SQLiteDatabase db = dbHelper.openDatabase();
        return executeQuery(db, "SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_RATING + " > " + rating + " AND " + COLUMN_SOLVED + " = 0 ORDER BY " + COLUMN_RATING + " DESC LIMIT 10000");
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
}
