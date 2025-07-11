package com.tacticmaster.db;

import static com.tacticmaster.db.PlayerTable.COLUMN_AUTOPLAY_ENABLED;
import static com.tacticmaster.db.PlayerTable.COLUMN_PLAYER_ID;
import static com.tacticmaster.db.PlayerTable.COLUMN_PLAYER_RATING;
import static com.tacticmaster.db.PlayerTable.DEFAULT_PLAYER_RATING;
import static com.tacticmaster.db.PlayerTable.PLAYER_TABLE_NAME;
import static com.tacticmaster.db.PuzzleTable.COLUMN_PUZZLE_ID;
import static com.tacticmaster.db.PuzzleTable.COLUMN_SOLVED;
import static com.tacticmaster.db.PuzzleTable.PUZZLE_TABLE_NAME;
import static java.util.Objects.isNull;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "puzzle.db";
    private static final int DATABASE_VERSION = 4;
    private final Context context;
    private final String databasePath;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
        this.databasePath = context.getFilesDir().getPath() + "/" + DATABASE_NAME;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Handle database upgrade if needed
        if (oldVersion < 3 && newVersion == 3) { // change to if (oldVersion < 3 && newVersion >= 3)
            SQLiteDatabase localDb = openDatabase();
            localDb.execSQL("ALTER TABLE " + PLAYER_TABLE_NAME + " ADD COLUMN " + COLUMN_AUTOPLAY_ENABLED + " INTEGER DEFAULT 1");
            try (Cursor cursor = localDb.rawQuery("SELECT * FROM " + PLAYER_TABLE_NAME, null)) {
                if (cursor.getCount() > 0) {
                    ContentValues values = new ContentValues();
                    values.put(COLUMN_PLAYER_ID, 1);
                    localDb.update(PLAYER_TABLE_NAME, values, COLUMN_PLAYER_ID + " != 1", null);
                } else {
                    createPlayer(localDb);
                }
            }
        }
        if (oldVersion < 3 && newVersion == 3) {
            SQLiteDatabase localDb = openDatabase();
            localDb.execSQL("ALTER TABLE " + PLAYER_TABLE_NAME + " ADD COLUMN " + COLUMN_AUTOPLAY_ENABLED + " INTEGER DEFAULT 1");
            try (Cursor cursor = localDb.rawQuery("SELECT * FROM " + PLAYER_TABLE_NAME, null)) {
                if (cursor.getCount() > 0) {
                    ContentValues values = new ContentValues();
                    values.put(COLUMN_PLAYER_ID, 1);
                    localDb.update(PLAYER_TABLE_NAME, values, COLUMN_PLAYER_ID + " != 1", null);
                } else {
                    createPlayer(localDb);
                }
            }
        }
        if (oldVersion < 4 && newVersion >= 4) {
            SQLiteDatabase localDb = openDatabase();

            // Store player table data
            ContentValues[] playerData = null;
            try (Cursor cursor = localDb.rawQuery("SELECT * FROM " + PLAYER_TABLE_NAME, null)) {
                playerData = new ContentValues[cursor.getCount()];
                int index = 0;
                while (cursor.moveToNext()) {
                    ContentValues values = new ContentValues();
                    values.put(COLUMN_PLAYER_ID, cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PLAYER_ID)));
                    values.put(COLUMN_PLAYER_RATING, cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PLAYER_RATING)));
                    values.put(COLUMN_AUTOPLAY_ENABLED, cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_AUTOPLAY_ENABLED)));
                    playerData[index++] = values;
                }
            }

            // Store solved puzzle IDs
            String[] solvedPuzzleIds = null;
            try (Cursor cursor = localDb.rawQuery("SELECT " + COLUMN_PUZZLE_ID + " FROM " + PUZZLE_TABLE_NAME + " WHERE " + COLUMN_SOLVED + " = 1", null)) {
                solvedPuzzleIds = new String[cursor.getCount()];
                int index = 0;
                while (cursor.moveToNext()) {
                    solvedPuzzleIds[index++] = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PUZZLE_ID));
                }
            }

            // Close database
            localDb.close();

            // Copy new database
            try {
                copyDatabase();
            } catch (IOException e) {
                throw new Error("Error copying database during upgrade");
            }

            // Reopen database and apply schema changes
            localDb = openDatabase();
            localDb.execSQL("ALTER TABLE " + PUZZLE_TABLE_NAME + " ADD COLUMN " + COLUMN_SOLVED + " INTEGER DEFAULT 0");
            createPlayerRatingTable(localDb);

            if (!isNull(playerData)) {
                for (ContentValues values : playerData) {
                    localDb.insert(PLAYER_TABLE_NAME, null, values);
                }
            } else {
                createPlayer(localDb);
            }

            if (!isNull(solvedPuzzleIds) && solvedPuzzleIds.length > 0) {
                ContentValues values = new ContentValues();
                values.put(COLUMN_SOLVED, 1);
                for (String puzzleId : solvedPuzzleIds) {
                    localDb.update(PUZZLE_TABLE_NAME, values, "puzzle_id = ?", new String[]{puzzleId});
                }
            }

            localDb.close();
        }
    }

    public void createDatabase() throws Error {
        boolean dbExist = checkDatabase();
        if (!dbExist) {
            try {
                copyDatabase();
                SQLiteDatabase db = openDatabase();
                db.execSQL("ALTER TABLE " + PUZZLE_TABLE_NAME + " ADD COLUMN " + COLUMN_SOLVED + " INTEGER DEFAULT 0");
                createPlayerRatingTable(db);
                createPlayer(db);
                db.close();
            } catch (IOException e) {
                throw new Error("Error copying database");
            }
        }
    }

    private boolean checkDatabase() {
        SQLiteDatabase checkDB = null;
        try {
            checkDB = SQLiteDatabase.openDatabase(databasePath, null, SQLiteDatabase.OPEN_READONLY);
        } catch (Exception e) {
            Log.i("DatabaseHelper", "Database does not exist yet, creating one");
        }
        if (!isNull(checkDB)) {
            checkDB.close();
        }
        SQLiteDatabase db = getWritableDatabase(); // open the bundled database once, to trigger onUpdate if needed
        db.close();
        return !isNull(checkDB);
    }

    private void copyDatabase() throws IOException {
        InputStream input = context.getAssets().open(DATABASE_NAME);
        OutputStream output = new FileOutputStream(databasePath);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = input.read(buffer)) > 0) {
            output.write(buffer, 0, length);
        }
        output.flush();
        output.close();
        input.close();
    }

    public SQLiteDatabase openDatabase() {
        return SQLiteDatabase.openDatabase(databasePath, null, SQLiteDatabase.OPEN_READWRITE);
    }

    private void createPlayerRatingTable(SQLiteDatabase db) {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS " + PLAYER_TABLE_NAME + " (" +
                COLUMN_PLAYER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_PLAYER_RATING + " INTEGER NOT NULL DEFAULT " + DEFAULT_PLAYER_RATING + ", " +
                COLUMN_AUTOPLAY_ENABLED + " INTEGER NOT NULL DEFAULT 1)";
        db.execSQL(createTableSQL);
    }

    private void createPlayer(SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_PLAYER_ID, 1);
        values.put(COLUMN_PLAYER_RATING, DEFAULT_PLAYER_RATING);
        db.insert(PLAYER_TABLE_NAME, null, values);
    }
}