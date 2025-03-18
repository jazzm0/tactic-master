package com.tacticmaster;

import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.tacticmaster.board.ChessboardView;
import com.tacticmaster.db.DatabaseAccessor;

public class MainActivity extends AppCompatActivity {

    private ChessboardController chessboardController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        deleteDatabase("puzzle.db");

        DatabaseAccessor databaseAccessor = new DatabaseAccessor(this);
        ChessboardView chessboardView = findViewById(R.id.chessboard_view);

        chessboardController = new ChessboardController(this, databaseAccessor, chessboardView, findViewById(R.id.puzzle_id), findViewById(R.id.puzzle_rating), findViewById(R.id.puzzle_themes), findViewById(R.id.puzzle_moves));
        chessboardController.loadPuzzlesWithRatingGreaterThan(2000);


        ImageButton previousPuzzle = findViewById(R.id.previous_puzzle);
        ImageButton nextPuzzle = findViewById(R.id.next_puzzle);

        previousPuzzle.setOnClickListener(v -> onPreviousPuzzleClicked());
        nextPuzzle.setOnClickListener(v -> onNextPuzzleClicked());
    }

    private void onPreviousPuzzleClicked() {
        chessboardController.loadPreviousPuzzle();
    }

    private void onNextPuzzleClicked() {
        chessboardController.loadNextPuzzle();
    }
}