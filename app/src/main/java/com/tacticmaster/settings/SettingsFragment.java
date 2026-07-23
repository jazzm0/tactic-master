package com.tacticmaster.settings;

import static java.util.Objects.isNull;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SeekBarPreference;

import com.tacticmaster.R;

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

    @Override
    public void onDisplayPreferenceDialog(@NonNull Preference preference) {
        if (preference instanceof PieceSetPreference) {
            PieceSetPreferenceDialog dialog =
                    PieceSetPreferenceDialog.newInstance(preference.getKey());
            // Shown via the child fragment manager so the dialog can resolve its
            // preference through getParentFragment() — no deprecated target fragment.
            dialog.show(getChildFragmentManager(), "PieceSetPreferenceDialog");
            return;
        }
        super.onDisplayPreferenceDialog(preference);
    }
}
