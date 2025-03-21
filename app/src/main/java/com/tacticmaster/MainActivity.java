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

        DatabaseAccessor databaseAccessor = new DatabaseAccessor(this);
        ChessboardView chessboardView = findViewById(R.id.chessboard_view);

        chessboardController = new ChessboardController(this,
                databaseAccessor,
                chessboardView,
                findViewById(R.id.puzzle_id),
                findViewById(R.id.puzzle_rating),
                findViewById(R.id.puzzles_count),
                findViewById(R.id.puzzle_themes),
                findViewById(R.id.puzzle_moves),
                findViewById(R.id.puzzle_popularity),
                findViewById(R.id.puzzle_nbplays),
                findViewById(R.id.player_rating));
        chessboardController.loadNextPuzzles();


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