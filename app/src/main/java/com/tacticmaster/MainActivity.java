package com.tacticmaster;

import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.tacticmaster.board.ChessboardView;
import com.tacticmaster.db.DatabaseAccessor;

import java.security.SecureRandom;

public class MainActivity extends AppCompatActivity {

    private ChessboardController chessboardController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DatabaseAccessor databaseAccessor = new DatabaseAccessor(this);
        ChessboardView chessboardView = findViewById(R.id.chessboard_view);
        chessboardView.setPlayerTurnIcon(findViewById(R.id.player_turn_icon));

        chessboardController = new ChessboardController(
                databaseAccessor,
                chessboardView,
                new PuzzleTextViews(this),
                new SecureRandom());

        chessboardController.loadNextPuzzle();

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