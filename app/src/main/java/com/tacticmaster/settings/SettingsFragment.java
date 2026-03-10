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
    private SeekBarPreference animationSpeedPref;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Use the same SharedPreferences as SettingsManager
        PreferenceManager prefManager = getPreferenceManager();
        prefManager.setSharedPreferencesName("TacticMasterSettings");

        setPreferencesFromResource(R.xml.preferences, rootKey);

        settingsManager = SettingsManager.getInstance(requireContext());

        // Initialize preferences
        SwitchPreferenceCompat autoplayPref = findPreference("autoplay");
        SwitchPreferenceCompat soundPref = findPreference("sound_enabled");
        SwitchPreferenceCompat animationsPref = findPreference("animations_enabled");
        animationSpeedPref = findPreference("animation_speed");

        // Load current values from SettingsManager
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
                boolean enabled = (Boolean) newValue;
                settingsManager.setAnimationsEnabled(enabled);
                updateAnimationSpeedVisibility(enabled);
                return true;
            });
        }

        if (!isNull(animationSpeedPref)) {
            int currentSpeed = ensureSpeedLimits(settingsManager.getAnimationSpeed());
            settingsManager.setAnimationSpeed(currentSpeed);

            animationSpeedPref.setMin(300);
            animationSpeedPref.setMax(800);
            animationSpeedPref.setValue(currentSpeed);
            animationSpeedPref.setShowSeekBarValue(true);

            animationSpeedPref.setOnPreferenceChangeListener((preference, newValue) -> {
                int speedMs = ensureSpeedLimits((Integer) newValue);
                settingsManager.setAnimationSpeed(speedMs);
                return true;
            });

            // Set initial visibility
            updateAnimationSpeedVisibility(settingsManager.areAnimationsEnabled());
        }
    }

    private int ensureSpeedLimits(int speed) {
        if (speed < 300) {
            return 300;
        } else if (speed > 800) {
            return 800;
        }
        return speed;
    }

    private void updateAnimationSpeedVisibility(boolean animationsEnabled) {
        if (!isNull(animationSpeedPref)) {
            animationSpeedPref.setVisible(animationsEnabled);
        }
    }
}
