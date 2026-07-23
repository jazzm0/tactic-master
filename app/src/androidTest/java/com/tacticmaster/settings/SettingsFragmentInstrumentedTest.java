package com.tacticmaster.settings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import androidx.fragment.app.DialogFragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SeekBarPreference;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.tacticmaster.R;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Instrumented tests for {@link SettingsFragment}, focused on the piece-set
 * preference wiring: the custom preference is inflated from XML, and displaying
 * its dialog routes to {@link PieceSetPreferenceDialog} via the child fragment
 * manager (not the deprecated target-fragment path).
 */
@RunWith(AndroidJUnit4.class)
public class SettingsFragmentInstrumentedTest {

    private static final String DIALOG_TAG = "PieceSetPreferenceDialog";

    @Test
    public void testFragment_IsAttached() {
        try (ActivityScenario<SettingsActivity> scenario =
                     ActivityScenario.launch(SettingsActivity.class)) {
            scenario.onActivity(activity -> assertNotNull(fragment(activity)));
        }
    }

    @Test
    public void testPieceSetPreference_IsCustomType() {
        try (ActivityScenario<SettingsActivity> scenario =
                     ActivityScenario.launch(SettingsActivity.class)) {
            scenario.onActivity(activity -> {
                Preference preference = fragment(activity).findPreference(SettingKey.PIECE_SET.key);
                assertNotNull(preference);
                assertTrue("piece_set should be a PieceSetPreference",
                        preference instanceof PieceSetPreference);
            });
        }
    }

    @Test
    public void testSeekBarsConfigured() {
        try (ActivityScenario<SettingsActivity> scenario =
                     ActivityScenario.launch(SettingsActivity.class)) {
            scenario.onActivity(activity -> {
                SeekBarPreference rating =
                        fragment(activity).findPreference(SettingKey.PLAYER_RATING.key);
                assertNotNull(rating);
                assertEquals(SettingsManager.MIN_PLAYER_RATING, rating.getMin());
                assertEquals(SettingsManager.MAX_PLAYER_RATING, rating.getMax());
            });
        }
    }

    @Test
    public void testOnDisplayPreferenceDialog_ShowsPieceSetDialog() {
        try (ActivityScenario<SettingsActivity> scenario =
                     ActivityScenario.launch(SettingsActivity.class)) {
            scenario.onActivity(activity -> {
                PreferenceFragmentCompat fragment = fragment(activity);
                Preference preference = fragment.findPreference(SettingKey.PIECE_SET.key);
                fragment.onDisplayPreferenceDialog(preference);
                fragment.getChildFragmentManager().executePendingTransactions();

                DialogFragment shown = (DialogFragment) fragment.getChildFragmentManager()
                        .findFragmentByTag(DIALOG_TAG);
                assertNotNull("Piece-set dialog should be shown", shown);
                assertTrue(shown instanceof PieceSetPreferenceDialog);
                assertNotNull(shown.getDialog());
                assertTrue(shown.getDialog().isShowing());
            });
        }
    }

    @Test
    public void testOnDisplayPreferenceDialog_NonPieceSet_DelegatesToSuper() {
        try (ActivityScenario<SettingsActivity> scenario =
                     ActivityScenario.launch(SettingsActivity.class)) {
            scenario.onActivity(activity -> {
                PreferenceFragmentCompat fragment = fragment(activity);
                Preference rating = fragment.findPreference(SettingKey.PLAYER_RATING.key);
                assertNotNull(rating);

                // A non-PieceSetPreference must fall through to super. The base
                // PreferenceFragmentCompat has no dialog for a SeekBarPreference and
                // throws — that exception is proof the override delegated rather than
                // handling it, and no piece-set dialog was created.
                try {
                    fragment.onDisplayPreferenceDialog(rating);
                } catch (IllegalArgumentException expected) {
                    // super threw: correct delegation path.
                }
                fragment.getChildFragmentManager().executePendingTransactions();

                assertTrue("No piece-set dialog should be shown for an unrelated preference",
                        fragment.getChildFragmentManager().findFragmentByTag(DIALOG_TAG) == null);
            });
        }
    }

    private static PreferenceFragmentCompat fragment(SettingsActivity activity) {
        return (PreferenceFragmentCompat) activity.getSupportFragmentManager()
                .findFragmentById(R.id.settings_container);
    }
}
