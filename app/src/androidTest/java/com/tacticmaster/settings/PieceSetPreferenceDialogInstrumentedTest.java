package com.tacticmaster.settings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.app.Dialog;
import android.view.View;
import android.widget.ListView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.preference.PreferenceFragmentCompat;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.tacticmaster.R;
import com.tacticmaster.board.ChessboardPieceManager;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Instrumented tests for {@link PieceSetPreferenceDialog}: the piece-set picker
 * dialog. Runs on a device because it inflates real views and renders piece
 * bitmaps from assets.
 */
@RunWith(AndroidJUnit4.class)
public class PieceSetPreferenceDialogInstrumentedTest {

    private static final String DIALOG_TAG = "PieceSetPreferenceDialog";

    @Test
    public void testDialog_ListsEveryAvailableSet() {
        try (ActivityScenario<SettingsActivity> scenario =
                     ActivityScenario.launch(SettingsActivity.class)) {
            int expected = availableSetCount(scenario);
            showDialog(scenario);

            AlertDialog dialog = (AlertDialog) currentDialog(scenario);
            assertNotNull(dialog);
            ListView list = dialog.getListView();
            assertNotNull(list);
            assertEquals(expected, list.getAdapter().getCount());
        }
    }

    @Test
    public void testDialog_HighlightsCurrentSet() {
        try (ActivityScenario<SettingsActivity> scenario =
                     ActivityScenario.launch(SettingsActivity.class)) {
            scenario.onActivity(activity -> preference(activity).setValue("lichess"));
            showDialog(scenario);

            String[] sets = availableSets(scenario);
            int selected = -1;
            for (int i = 0; i < sets.length; i++) {
                if ("lichess".equals(sets[i])) {
                    selected = i;
                    break;
                }
            }
            assertTrue("Test fixture must contain the 'lichess' set", selected >= 0);

            final int selectedPos = selected;
            scenario.onActivity(activity -> {
                AlertDialog dialog = (AlertDialog) currentDialogFragment(activity).getDialog();
                ListView list = dialog.getListView();

                // The selected row is highlighted (opaque background); a different
                // row is not (transparent background).
                int selectedBg = backgroundColorOf(list, selectedPos);
                int otherBg = backgroundColorOf(list, selectedPos == 0 ? 1 : 0);

                assertTrue("Selected row should be highlighted",
                        android.graphics.Color.alpha(selectedBg) != 0);
                assertEquals("Non-selected row should not be highlighted",
                        android.graphics.Color.TRANSPARENT, otherBg);
                assertTrue("Highlight should differ from a normal row",
                        selectedBg != otherBg);
            });
        }
    }

    /**
     * Resolves the solid background color of an adapter row, or transparent.
     */
    private static int backgroundColorOf(ListView list, int position) {
        View row = list.getAdapter().getView(position, null, list);
        android.graphics.drawable.Drawable bg = row.getBackground();
        if (bg instanceof android.graphics.drawable.ColorDrawable) {
            return ((android.graphics.drawable.ColorDrawable) bg).getColor();
        }
        return android.graphics.Color.TRANSPARENT;
    }

    @Test
    public void testDialog_SelectingRowPersistsAndDismisses() {
        try (ActivityScenario<SettingsActivity> scenario =
                     ActivityScenario.launch(SettingsActivity.class)) {
            String[] sets = availableSets(scenario);
            // Pick a set different from whatever is currently selected.
            String current = currentValue(scenario);
            int target = 0;
            for (int i = 0; i < sets.length; i++) {
                if (!sets[i].equals(current)) {
                    target = i;
                    break;
                }
            }
            String targetSet = sets[target];

            showDialog(scenario);
            final int position = target;
            scenario.onActivity(activity -> {
                AlertDialog dialog = (AlertDialog) currentDialogFragment(activity).getDialog();
                dialog.getListView().performItemClick(
                        dialog.getListView().getAdapter().getView(position, null, dialog.getListView()),
                        position, position);
            });

            scenario.onActivity(activity -> {
                assertEquals(targetSet, preference(activity).getValue());
                assertTrue("Dialog should be dismissed after selection",
                        currentDialogFragment(activity) == null
                                || currentDialogFragment(activity).getDialog() == null
                                || !currentDialogFragment(activity).getDialog().isShowing());
            });
        }
    }

    @Test
    public void testDialog_SurvivesRecreation() {
        try (ActivityScenario<SettingsActivity> scenario =
                     ActivityScenario.launch(SettingsActivity.class)) {
            showDialog(scenario);
            scenario.recreate();

            AlertDialog dialog = (AlertDialog) currentDialog(scenario);
            assertNotNull("Dialog should be restored after recreation", dialog);
            assertTrue(dialog.isShowing());
            assertNotNull(dialog.getListView().getAdapter());
        }
    }

    // --- helpers ---

    private void showDialog(ActivityScenario<SettingsActivity> scenario) {
        scenario.onActivity(activity -> {
            PreferenceFragmentCompat fragment = fragment(activity);
            fragment.onDisplayPreferenceDialog(preference(activity));
        });
        // Let the dialog fragment commit and create its dialog.
        scenario.onActivity(activity ->
                activity.getSupportFragmentManager().executePendingTransactions());
    }

    private Dialog currentDialog(ActivityScenario<SettingsActivity> scenario) {
        AtomicReference<Dialog> ref = new AtomicReference<>();
        scenario.onActivity(activity -> {
            DialogFragment df = currentDialogFragment(activity);
            ref.set(df == null ? null : df.getDialog());
        });
        return ref.get();
    }

    private static DialogFragment currentDialogFragment(SettingsActivity activity) {
        return (DialogFragment) fragment(activity)
                .getChildFragmentManager()
                .findFragmentByTag(DIALOG_TAG);
    }

    private static PreferenceFragmentCompat fragment(SettingsActivity activity) {
        return (PreferenceFragmentCompat) activity.getSupportFragmentManager()
                .findFragmentById(R.id.settings_container);
    }

    private static PieceSetPreference preference(SettingsActivity activity) {
        PieceSetPreference preference = fragment(activity).findPreference(SettingKey.PIECE_SET.key);
        assertNotNull(preference);
        return preference;
    }

    private String currentValue(ActivityScenario<SettingsActivity> scenario) {
        AtomicReference<String> ref = new AtomicReference<>();
        scenario.onActivity(activity -> ref.set(preference(activity).getValue()));
        return ref.get();
    }

    private String[] availableSets(ActivityScenario<SettingsActivity> scenario) {
        AtomicReference<String[]> ref = new AtomicReference<>();
        scenario.onActivity(activity ->
                ref.set(ChessboardPieceManager.availablePieceSets(activity)));
        return ref.get();
    }

    private int availableSetCount(ActivityScenario<SettingsActivity> scenario) {
        return availableSets(scenario).length;
    }
}
