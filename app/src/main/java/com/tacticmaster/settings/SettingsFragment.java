package com.tacticmaster.settings;

import static java.util.Objects.isNull;

import android.os.Bundle;

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

        configureAnimationSpeedPreference();
        configurePlayerRatingPreference();
    }

    private void configureAnimationSpeedPreference() {
        SeekBarPreference pref = findPreference(SettingKey.ANIMATION_SPEED.key);
        if (isNull(pref)) {
            return;
        }
        pref.setMin(SettingsManager.ANIMATION_SPEED_NORMAL);
        pref.setMax(SettingsManager.ANIMATION_SPEED_SLOW);
        pref.setShowSeekBarValue(true);
    }

    private void configurePlayerRatingPreference() {
        SeekBarPreference pref = findPreference(SettingKey.PLAYER_RATING.key);
        if (isNull(pref)) {
            return;
        }
        pref.setMin(SettingsManager.MIN_PLAYER_RATING);
        pref.setMax(SettingsManager.MAX_PLAYER_RATING);
        pref.setSeekBarIncrement(PLAYER_RATING_STEP);
        pref.setShowSeekBarValue(true);
    }
}
