package com.tacticmaster.settings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import androidx.preference.PreferenceFragmentCompat;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.tacticmaster.R;
import com.tacticmaster.board.ChessboardPieceManager;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Instrumented tests for {@link PieceSetPreference}. These run on a device/emulator
 * because the preference extends the Android {@code DialogPreference} framework
 * class, which cannot be instantiated on the plain JVM.
 */
@RunWith(AndroidJUnit4.class)
public class PieceSetPreferenceInstrumentedTest {

    /** Resolves the piece-set preference from a live SettingsFragment. */
    private PieceSetPreference withPreference(ActivityScenario<SettingsActivity> scenario) {
        AtomicReference<PieceSetPreference> ref = new AtomicReference<>();
        scenario.onActivity(activity -> {
            PreferenceFragmentCompat fragment = (PreferenceFragmentCompat) activity
                    .getSupportFragmentManager()
                    .findFragmentById(R.id.settings_container);
            assertNotNull(fragment);
            ref.set(fragment.findPreference(SettingKey.PIECE_SET.key));
        });
        return ref.get();
    }

    @Test
    public void testPreferenceIsPresentAndTyped() {
        try (ActivityScenario<SettingsActivity> scenario =
                     ActivityScenario.launch(SettingsActivity.class)) {
            PieceSetPreference preference = withPreference(scenario);
            assertNotNull(preference);
        }
    }

    @Test
    public void testSetValue_IsReflectedByGetValue() {
        try (ActivityScenario<SettingsActivity> scenario =
                     ActivityScenario.launch(SettingsActivity.class)) {
            scenario.onActivity(activity -> {
                PieceSetPreference preference = fragmentPreference(activity);
                preference.setValue("lichess");
                assertEquals("lichess", preference.getValue());
            });
        }
    }

    @Test
    public void testSummary_ReflectsCapitalizedSelection() {
        try (ActivityScenario<SettingsActivity> scenario =
                     ActivityScenario.launch(SettingsActivity.class)) {
            scenario.onActivity(activity -> {
                PieceSetPreference preference = fragmentPreference(activity);
                preference.setValue("merida");
                assertEquals("Merida", preference.getSummary().toString());
            });
        }
    }

    @Test
    public void testGetValue_DefaultsToAnAvailableSetWhenUnset() {
        try (ActivityScenario<SettingsActivity> scenario =
                     ActivityScenario.launch(SettingsActivity.class)) {
            scenario.onActivity(activity -> {
                PieceSetPreference preference = fragmentPreference(activity);
                String value = preference.getValue();
                assertNotNull(value);
                assertTrue(!value.isEmpty());

                // The resolved value must be a real bundled set.
                String[] sets = ChessboardPieceManager.availablePieceSets(activity);
                boolean known = false;
                for (String set : sets) {
                    if (set.equals(value)) {
                        known = true;
                        break;
                    }
                }
                assertTrue("Default value should be a bundled set: " + value, known);
            });
        }
    }

    @Test
    public void testSetValue_PersistsAcrossFragmentRecreation() {
        try (ActivityScenario<SettingsActivity> scenario =
                     ActivityScenario.launch(SettingsActivity.class)) {
            scenario.onActivity(activity -> fragmentPreference(activity).setValue("sashite"));

            scenario.recreate();

            scenario.onActivity(activity ->
                    assertEquals("sashite", fragmentPreference(activity).getValue()));
        }
    }

    private static PieceSetPreference fragmentPreference(SettingsActivity activity) {
        PreferenceFragmentCompat fragment = (PreferenceFragmentCompat) activity
                .getSupportFragmentManager()
                .findFragmentById(R.id.settings_container);
        assertNotNull(fragment);
        PieceSetPreference preference = fragment.findPreference(SettingKey.PIECE_SET.key);
        assertNotNull(preference);
        return preference;
    }
}
