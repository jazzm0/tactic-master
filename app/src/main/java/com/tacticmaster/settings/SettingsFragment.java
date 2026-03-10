package com.tacticmaster.settings;

import static java.util.Objects.isNull;

import android.os.Bundle;

import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.tacticmaster.R;

/**
 * Fragment for displaying and managing application settings using PreferenceScreen.
 */
public class SettingsFragment extends PreferenceFragmentCompat {

    private SettingsManager settingsManager;
    private ListPreference animationSpeedPref;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
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
            String currentSpeed = SettingsManager.animationSpeedToString(settingsManager.getAnimationSpeed());
            animationSpeedPref.setValue(currentSpeed);
            updateAnimationSpeedSummary(currentSpeed);

            animationSpeedPref.setOnPreferenceChangeListener((preference, newValue) -> {
                String speedValue = (String) newValue;
                int speedMs = SettingsManager.animationSpeedFromString(speedValue);
                settingsManager.setAnimationSpeed(speedMs);
                updateAnimationSpeedSummary(speedValue);
                return true;
            });

            // Set initial visibility
            updateAnimationSpeedVisibility(settingsManager.areAnimationsEnabled());
        }
    }

    private void updateAnimationSpeedVisibility(boolean animationsEnabled) {
        if (!isNull(animationSpeedPref)) {
            animationSpeedPref.setVisible(animationsEnabled);
        }
    }

    private void updateAnimationSpeedSummary(String speedValue) {
        if (!isNull(animationSpeedPref)) {
            String summary = switch (speedValue) {
                case "slow" -> getString(R.string.animation_speed_slow_summary);
                case "fast" -> getString(R.string.animation_speed_fast_summary);
                default -> getString(R.string.animation_speed_normal_summary);
            };
            animationSpeedPref.setSummary(summary);
        }
    }
}