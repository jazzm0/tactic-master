package com.tacticmaster.db;

import static com.tacticmaster.puzzle.PuzzleTable.COLUMN_RATING;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.tacticmaster.puzzle.Puzzle;
import com.tacticmaster.puzzle.PuzzleTable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DatabaseAccessor {


    private static final String TABLE_NAME = "lichess_db_puzzle";
    private final DatabaseHelper dbHelper;

    public DatabaseAccessor(Context context) {
        dbHelper = new DatabaseHelper(context);
        try {
            dbHelper.createDatabase();
        } catch (IOException e) {
            Log.e("The following error occurred: ", e.getMessage());
        }

        SQLiteDatabase db = dbHelper.openDatabase();
    }

    public List<Puzzle> getPuzzlesWithRatingGreaterThan(int rating) {
        SQLiteDatabase db = dbHelper.openDatabase();
        return executeQuery(db, "SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_RATING + " > " + rating);
    }

    private List<Puzzle> executeQuery(SQLiteDatabase db, String query) {
        List<Puzzle> puzzles = new ArrayList<>();
        try (Cursor cursor = db.rawQuery(query, null)) {
            int puzzleIdIndex = cursor.getColumnIndex(PuzzleTable.COLUMN_PUZZLE_ID);
            int fenIndex = cursor.getColumnIndex(PuzzleTable.COLUMN_FEN);
            int movesIndex = cursor.getColumnIndex(PuzzleTable.COLUMN_MOVES);
            int ratingIndex = cursor.getColumnIndex(PuzzleTable.COLUMN_RATING);
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
