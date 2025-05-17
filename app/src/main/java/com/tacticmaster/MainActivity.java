package com.tacticmaster;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.tacticmaster.board.ChessboardView;
import com.tacticmaster.db.DatabaseAccessor;
import com.tacticmaster.db.DatabaseHelper;

import java.security.SecureRandom;

public class MainActivity extends AppCompatActivity {

    private ChessboardController chessboardController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        DatabaseAccessor databaseAccessor = new DatabaseAccessor(new DatabaseHelper(this));
        ChessboardView chessboardView = findViewById(R.id.chessboard_view);
        chessboardView.setPlayerTurnIcon(findViewById(R.id.player_turn_icon));
        chessboardView.setHintPathView(findViewById(R.id.hint_path_view));

        chessboardController = new ChessboardController(
                databaseAccessor,
                chessboardView,
                new PuzzleTextViews(this),
                new SecureRandom());

        chessboardController.loadNextPuzzle();

        ImageButton reloadPuzzle = findViewById(R.id.puzzle_reload);
        ImageButton previousPuzzle = findViewById(R.id.previous_puzzle);
        ImageButton nextPuzzle = findViewById(R.id.next_puzzle);
        ImageButton hint = findViewById(R.id.puzzle_hint);
        EditText puzzleId = findViewById(R.id.puzzle_id);
        TextView puzzleIdLink = findViewById(R.id.puzzle_id_link);

        reloadPuzzle.setOnClickListener(v -> onReloadPuzzleClicked());
        previousPuzzle.setOnClickListener(v -> onPreviousPuzzleClicked());
        nextPuzzle.setOnClickListener(v -> onNextPuzzleClicked());
        hint.setOnClickListener(v -> onPuzzleHintClicked());
        puzzleId.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    chessboardController.loadPuzzleById(puzzleId.getText().toString());
                    return true;
                }
                return false;
            }});
        puzzleIdLink.setOnClickListener(v->onPuzzleIdLinkClicked());
    }

    private void onReloadPuzzleClicked() {
        chessboardController.renderPuzzle();
    }

    private void onPreviousPuzzleClicked() {
        chessboardController.loadPreviousPuzzle();
    }

    private void onNextPuzzleClicked() {
        chessboardController.loadNextPuzzle();
    }

    private void onPuzzleHintClicked() {
        chessboardController.puzzleHintClicked();
    }

    private void onPuzzleIdLinkClicked() { chessboardController.puzzleIdLinkClicked(); }
}
