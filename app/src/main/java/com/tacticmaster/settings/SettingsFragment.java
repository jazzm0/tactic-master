package com.tacticmaster.settings;

import static java.util.Objects.isNull;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SeekBarPreference;
import androidx.preference.SwitchPreferenceCompat;

import com.tacticmaster.R;

/**
 * Fragment for displaying and managing application settings using PreferenceScreen.
 */
public class SettingsFragment extends PreferenceFragmentCompat {

    private SettingsManager settingsManager;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Use the same SharedPreferences as SettingsManager
        PreferenceManager prefManager = getPreferenceManager();
        prefManager.setSharedPreferencesName("TacticMasterSettings");

        setPreferencesFromResource(R.xml.preferences, rootKey);

        settingsManager = SettingsManager.getInstance(requireContext());

        SwitchPreferenceCompat autoplayPref = findPreference("autoplay");
        SwitchPreferenceCompat soundPref = findPreference("sound_enabled");
        SwitchPreferenceCompat animationsPref = findPreference("animations_enabled");
        SeekBarPreference animationSpeedPref = findPreference("animation_speed");
        SwitchPreferenceCompat showPuzzleRatingPref = findPreference("show_puzzle_rating");
        SwitchPreferenceCompat showPuzzleIdPref = findPreference("show_puzzle_id");
        SwitchPreferenceCompat showPuzzlesCountPref = findPreference("show_puzzles_count");

        if (!isNull(autoplayPref)) {
            autoplayPref.setChecked(settingsManager.isAutoplayEnabled());
            autoplayPref.setOnPreferenceChangeListener((preference, newValue) -> {
                settingsManager.setAutoplayEnabled((Boolean) newValue);
                return true;
            });
        }

        if (!isNull(soundPref)) {
            soundPref.setChecked(settingsManager.isSoundEnabled());
            soundPref.setOnPreferenceChangeListener((preference, newValue) -> {
                settingsManager.setSoundEnabled((Boolean) newValue);
                return true;
            });
        }

        if (!isNull(animationsPref)) {
            animationsPref.setChecked(settingsManager.areAnimationsEnabled());
            animationsPref.setOnPreferenceChangeListener((preference, newValue) -> {
                settingsManager.setAnimationsEnabled((Boolean) newValue);
                return true;
            });
        }

        if (!isNull(animationSpeedPref)) {
            int currentSpeed = ensureSpeedLimits(settingsManager.getAnimationSpeed());
            settingsManager.setAnimationSpeed(currentSpeed);

            animationSpeedPref.setMin(SettingsManager.ANIMATION_SPEED_NORMAL);
            animationSpeedPref.setMax(SettingsManager.ANIMATION_SPEED_SLOW);
            animationSpeedPref.setValue(currentSpeed);
            animationSpeedPref.setShowSeekBarValue(true);

            animationSpeedPref.setOnPreferenceChangeListener((preference, newValue) -> {
                int speedMs = ensureSpeedLimits((Integer) newValue);
                settingsManager.setAnimationSpeed(speedMs);
                return true;
            });
        }

        if (!isNull(showPuzzleRatingPref)) {
            showPuzzleRatingPref.setChecked(settingsManager.isShowPuzzleRating());
            showPuzzleRatingPref.setOnPreferenceChangeListener((preference, newValue) -> {
                settingsManager.setShowPuzzleRating((Boolean) newValue);
                return true;
            });
        }

        if (!isNull(showPuzzleIdPref)) {
            showPuzzleIdPref.setChecked(settingsManager.isShowPuzzleId());
            showPuzzleIdPref.setOnPreferenceChangeListener((preference, newValue) -> {
                settingsManager.setShowPuzzleId((Boolean) newValue);
                return true;
            });
        }

        if (!isNull(showPuzzlesCountPref)) {
            showPuzzlesCountPref.setChecked(settingsManager.isShowPuzzlesCount());
            showPuzzlesCountPref.setOnPreferenceChangeListener((preference, newValue) -> {
                settingsManager.setShowPuzzlesCount((Boolean) newValue);
                return true;
            });
        }
    }

    private int ensureSpeedLimits(int speed) {
        if (speed < SettingsManager.ANIMATION_SPEED_NORMAL) {
            return SettingsManager.ANIMATION_SPEED_NORMAL;
        } else if (speed > SettingsManager.ANIMATION_SPEED_SLOW) {
            return SettingsManager.ANIMATION_SPEED_SLOW;
        }
        return speed;
    }
}
