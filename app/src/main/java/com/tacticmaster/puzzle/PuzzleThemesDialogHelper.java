package com.tacticmaster.puzzle;

import static java.util.Objects.isNull;

import android.content.Context;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.tacticmaster.db.DatabaseAccessor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class PuzzleThemesDialogHelper {

    private final PuzzleFilter puzzleFilter;
    private final PuzzleThemesListener puzzleThemesListener;
    private final Set<String> selectedThemes = new HashSet<>();

    public interface PuzzleThemesListener {
        void onThemesUpdated(Set<String> themes);
    }

    public PuzzleThemesDialogHelper(DatabaseAccessor databaseAccessor, PuzzleThemesListener puzzleThemesListener) {
        this.puzzleThemesListener = puzzleThemesListener;
        this.puzzleFilter = new PuzzleFilter(databaseAccessor);

    }

    private void setDialogButtonColors(Context context, AlertDialog dialog) {
        int color = context.getResources().getColor(android.R.color.darker_gray, null);
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(color);
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(color);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(color);
    }

    public void prepareDialogContent(Context context, MaterialButton filterButton, MaterialAutoCompleteTextView filterDropdown, Runnable callback) {
        var themesList = new ArrayList<>(puzzleFilter.getThemeGroups().keySet());
        var adapter = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, themesList);

        filterDropdown.setAdapter(adapter);
        filterDropdown.setDropDownHeight(0);

        filterButton.setOnClickListener(v -> {
            boolean[] checkedItems = new boolean[themesList.size()];

            for (int i = 0; i < themesList.size(); i++) {
                checkedItems[i] = selectedThemes.contains(themesList.get(i));
            }

            var builder = new MaterialAlertDialogBuilder(context).setMultiChoiceItems(themesList.toArray(new String[0]), checkedItems, (dialog, which, checked) -> {
                        String item = themesList.get(which);
                        if (checked) {
                            selectedThemes.add(item);
                        } else {
                            selectedThemes.remove(item);
                        }
                    })
                    .setPositiveButton("Done", (dialog, which) -> {
                        Set<String> allThemesInGroup = new HashSet<>();
                        selectedThemes.forEach(theme -> {
                            var themeGroup = puzzleFilter.getThemeGroups().get(theme);
                            if (!isNull(themeGroup)) {
                                allThemesInGroup.addAll(themeGroup);
                            }
                        });

                        puzzleThemesListener.onThemesUpdated(allThemesInGroup);
                        callback.run();
                    })
                    .setNeutralButton("Clear All", (dialog, which) -> {
                        selectedThemes.clear();
                        filterButton.setText("");
                        puzzleThemesListener.onThemesUpdated(selectedThemes);
                        callback.run();
                    })
                    .setNegativeButton("Cancel", null);

            AlertDialog dialog = builder.create();
            dialog.setOnShowListener(d -> setDialogButtonColors(context, dialog));
            dialog.show();
        });
    }
}
