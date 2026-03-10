package com.tacticmaster.db;

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
        if (oldVersion < 4 && newVersion >= 4) {
            SQLiteDatabase localDb = openDatabase();

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

            if (!isNull(solvedPuzzleIds) && solvedPuzzleIds.length > 0) {
                ContentValues values = new ContentValues();
                values.put(COLUMN_SOLVED, 1);
                for (String puzzleId : solvedPuzzleIds) {
                    localDb.update(PUZZLE_TABLE_NAME, values, COLUMN_PUZZLE_ID + " = ?", new String[]{puzzleId});
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
}
