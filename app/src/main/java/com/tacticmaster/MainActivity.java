package com.tacticmaster;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.tacticmaster.db.DatabaseAccessor;

public class MainActivity extends AppCompatActivity {

    private DatabaseAccessor databaseAccessor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        databaseAccessor = new DatabaseAccessor(this);
        var puzzles = databaseAccessor.getPuzzlesWithRatingGreaterThan(2500);
        if (!puzzles.isEmpty()) {
        }
    }
}