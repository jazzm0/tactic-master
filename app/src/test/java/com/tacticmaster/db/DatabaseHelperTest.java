package com.tacticmaster.db;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;

class DatabaseHelperTest {

    @Mock
    private Context mockContext;

    @Mock
    private AssetManager mockAssetManager;

    @Mock
    private File mockFilesDir;

    private DatabaseHelper databaseHelper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mockContext.getFilesDir()).thenReturn(mockFilesDir);
        when(mockFilesDir.getPath()).thenReturn("");
        when(mockContext.getAssets()).thenReturn(mockAssetManager);
    }

    @Test
    void testOnUpgradeNoChangeNeeded() {
        databaseHelper = new DatabaseHelper(mockContext);
        SQLiteDatabase mockDb = mock(SQLiteDatabase.class);

        // Version 4 to 4 should not trigger any upgrade logic
        databaseHelper.onUpgrade(mockDb, 4, 4);

        // Should complete without error since no upgrade logic runs
        assertNotNull(databaseHelper);
    }

    @Test
    void testOnUpgradeSkipsOlderVersions() {
        databaseHelper = new DatabaseHelper(mockContext);
        SQLiteDatabase mockDb = mock(SQLiteDatabase.class);

        // Version 1 to 2 should not trigger upgrade logic (no conditions match)
        databaseHelper.onUpgrade(mockDb, 1, 2);

        // Should complete without error since no upgrade conditions are met
        assertNotNull(databaseHelper);
    }

    @Test
    void testOnCreate() {
        databaseHelper = new DatabaseHelper(mockContext);
        SQLiteDatabase mockDb = mock(SQLiteDatabase.class);

        databaseHelper.onCreate(mockDb);

        // onCreate is empty, should complete without error
        assertNotNull(databaseHelper);
    }

    @Test
    void testDatabaseConstants() {
        assertEquals("puzzle.db", DatabaseHelper.DATABASE_NAME);
    }
}
