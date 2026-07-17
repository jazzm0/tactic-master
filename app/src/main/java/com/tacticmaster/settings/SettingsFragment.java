package com.tacticmaster.settings;

import static java.util.Objects.isNull;

import android.os.Bundle;

import androidx.preference.DropDownPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SeekBarPreference;

import com.tacticmaster.R;
import com.tacticmaster.board.ChessboardPieceManager;

/**
 * Settings UI. All persistence is delegated to {@link SettingsManager} via
 * {@link SettingsPreferenceDataStore} — the framework no longer touches
 * SharedPreferences directly.
 */
public class SettingsFragment extends PreferenceFragmentCompat {

    private static final int PLAYER_RATING_STEP = 50;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        SettingsManager settingsManager = SettingsManager.getInstance(requireContext());

        getPreferenceManager().setPreferenceDataStore(
                new SettingsPreferenceDataStore(settingsManager));

        setPreferencesFromResource(R.xml.preferences, rootKey);

        configureSeekBar(SettingKey.ANIMATION_SPEED.key,
                SettingsManager.ANIMATION_SPEED_NORMAL, SettingsManager.ANIMATION_SPEED_SLOW, 0);
        configureSeekBar(SettingKey.PLAYER_RATING.key,
                SettingsManager.MIN_PLAYER_RATING, SettingsManager.MAX_PLAYER_RATING, PLAYER_RATING_STEP);
        configurePieceSetPreference();
    }

    private void configureSeekBar(String key, int min, int max, int increment) {
        SeekBarPreference pref = findPreference(key);
        if (isNull(pref)) {
            return;
        }
        pref.setMin(min);
        pref.setMax(max);
        if (increment > 0) {
            pref.setSeekBarIncrement(increment);
        }
        pref.setShowSeekBarValue(true);
    }

    private void configurePieceSetPreference() {
        DropDownPreference pref = findPreference(SettingKey.PIECE_SET.key);
        if (isNull(pref)) {
            return;
        }
        String[] sets = ChessboardPieceManager.availablePieceSets(requireContext());
        CharSequence[] labels = new CharSequence[sets.length];
        for (int i = 0; i < sets.length; i++) {
            labels[i] = capitalize(sets[i]);
        }
        // Display the friendly label; store the raw folder name as the value.
        pref.setEntries(labels);
        pref.setEntryValues(sets);
    }

    private static String capitalize(String s) {
        if (isNull(s) || s.isEmpty()) {
            return s;
        }
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
