package com.tacticmaster;

import static java.util.Objects.isNull;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.tacticmaster.board.ChessboardView;
import com.tacticmaster.db.DatabaseAccessor;
import com.tacticmaster.db.DatabaseHelper;
import com.tacticmaster.db.PuzzleThemesManager;

public class MainActivity extends AppCompatActivity {

    private ChessboardController chessboardController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        View rootView = findViewById(R.id.root_layout);

        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            var systemInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            var cutoutInsets = insets.getInsets(WindowInsetsCompat.Type.displayCutout());
            int topPadding = Math.max(systemInsets.top, cutoutInsets.top);
            v.setPadding(systemInsets.left, topPadding, systemInsets.right, systemInsets.bottom);
            return insets;
        });

        DatabaseAccessor databaseAccessor = new DatabaseAccessor(new DatabaseHelper(this));
        ChessboardView chessboardView = findViewById(R.id.chessboard_view);
        chessboardView.setPlayerTurnIcon(findViewById(R.id.player_turn_icon));
        chessboardView.setPuzzleHintView(findViewById(R.id.hint_path_view));

        chessboardController = new ChessboardController(
                databaseAccessor,
                new PuzzleThemesManager(databaseAccessor, false),
                chessboardView,
                new PuzzleTextViews(this));

        chessboardController.loadNextPuzzle();

        ImageButton reloadPuzzle = findViewById(R.id.puzzle_reload);
        ImageButton previousPuzzle = findViewById(R.id.previous_puzzle);
        ImageButton nextPuzzle = findViewById(R.id.next_puzzle);
        ImageButton hint = findViewById(R.id.puzzle_hint);
        EditText puzzleId = findViewById(R.id.puzzle_id);
        TextView puzzleIdLink = findViewById(R.id.puzzle_id_link);

        SwitchMaterial autoplay = findViewById(R.id.toggle_autoplay);
        autoplay.setChecked(chessboardController.getAutoplay());
        autoplay.setOnCheckedChangeListener((buttonView, isChecked)
                -> chessboardController.setAutoplay(isChecked));
        autoplay.setThumbDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.autoplay, null));

        ColorStateList trackTintList = new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_checked},
                        new int[]{-android.R.attr.state_checked}
                },
                new int[]{
                        getResources().getColor(R.color.switch_track_on_color, null),
                        getResources().getColor(R.color.switch_track_off_color, null)
                }
        );

        autoplay.setTrackTintList(trackTintList);

        reloadPuzzle.setOnClickListener(v -> onReloadPuzzleClicked());
        previousPuzzle.setOnClickListener(v -> onPreviousPuzzleClicked());
        nextPuzzle.setOnClickListener(v -> onNextPuzzleClicked());
        hint.setOnClickListener(v -> onPuzzleHintClicked());
        puzzleId.setOnEditorActionListener((TextView v, int actionId, KeyEvent event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                chessboardController.loadPuzzleById(puzzleId.getText().toString());
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (!isNull(imm)) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
                return true;
            }
            return false;
        });
        puzzleIdLink.setOnClickListener(v -> onPuzzleIdLinkClicked());
    }

    @Override
    protected void onResume() {
        super.onResume();
        chessboardController.renderPuzzle();
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

    private void onPuzzleIdLinkClicked() {
        chessboardController.puzzleIdLinkClicked();
    }
}
