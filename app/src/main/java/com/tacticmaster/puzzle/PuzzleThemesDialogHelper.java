package com.tacticmaster.puzzle;

import static java.util.Objects.isNull;

import android.content.Context;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.tacticmaster.R;
import com.tacticmaster.db.DatabaseAccessor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
        List<String> categoryKeys = new ArrayList<>(puzzleFilter.getThemeGroups().keySet());
        String[] displayLabels = new String[categoryKeys.size()];
        for (int i = 0; i < categoryKeys.size(); i++) {
            displayLabels[i] = context.getString(PuzzleFilter.getCategoryLabelRes(categoryKeys.get(i)));
        }

        var adapter = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, displayLabels);

        filterDropdown.setAdapter(adapter);
        filterDropdown.setDropDownHeight(0);

        filterButton.setOnClickListener(v -> {
            boolean[] checkedItems = new boolean[categoryKeys.size()];

            for (int i = 0; i < categoryKeys.size(); i++) {
                checkedItems[i] = selectedThemes.contains(categoryKeys.get(i));
            }

            var builder = new MaterialAlertDialogBuilder(context).setMultiChoiceItems(displayLabels, checkedItems, (dialog, which, checked) -> {
                        String key = categoryKeys.get(which);
                        if (checked) {
                            selectedThemes.add(key);
                        } else {
                            selectedThemes.remove(key);
                        }
                    })
                    .setPositiveButton(R.string.dialog_done, (dialog, which) -> {
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
                    .setNeutralButton(R.string.dialog_clear_all, (dialog, which) -> {
                        selectedThemes.clear();
                        filterButton.setText("");
                        puzzleThemesListener.onThemesUpdated(selectedThemes);
                        callback.run();
                    })
                    .setNegativeButton(R.string.dialog_cancel, null);

            AlertDialog dialog = builder.create();
            dialog.setOnShowListener(d -> setDialogButtonColors(context, dialog));
            dialog.show();
        });
    }
}
