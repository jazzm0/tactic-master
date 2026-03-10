package com.tacticmaster.settings;

import static java.util.Objects.isNull;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Manages application settings using SharedPreferences.
 * Provides centralized access to all user preferences.
 */
public class SettingsManager {

    private static final String PREFS_NAME = "TacticMasterSettings";

    // Keys
    private static final String KEY_AUTOPLAY = "autoplay";
    private static final String KEY_SOUND_ENABLED = "sound_enabled";
    private static final String KEY_ANIMATIONS_ENABLED = "animations_enabled";
    private static final String KEY_ANIMATION_SPEED = "animation_speed";

    // Default values
    private static final boolean DEFAULT_AUTOPLAY = false;
    private static final boolean DEFAULT_SOUND_ENABLED = true;
    private static final boolean DEFAULT_ANIMATIONS_ENABLED = true;
    public static final int DEFAULT_ANIMATION_SPEED = 300; // milliseconds

    // Animation speed constants
    public static final int ANIMATION_SPEED_SLOW = 800;
    public static final int ANIMATION_SPEED_NORMAL = 300;
    public static final int ANIMATION_SPEED_FAST = 150;

    private final SharedPreferences sharedPreferences;
    private static SettingsManager instance;

    private SettingsManager(Context context) {
        this.sharedPreferences = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Gets the singleton instance of SettingsManager.
     */
    public static synchronized SettingsManager getInstance(Context context) {
        if (isNull(instance)) {
            instance = new SettingsManager(context);
        }
        return instance;
    }

    // Autoplay settings
    public boolean isAutoplayEnabled() {
        return sharedPreferences.getBoolean(KEY_AUTOPLAY, DEFAULT_AUTOPLAY);
    }

    public void setAutoplayEnabled(boolean enabled) {
        sharedPreferences.edit().putBoolean(KEY_AUTOPLAY, enabled).apply();
    }

    // Sound settings
    public boolean isSoundEnabled() {
        return sharedPreferences.getBoolean(KEY_SOUND_ENABLED, DEFAULT_SOUND_ENABLED);
    }

    public void setSoundEnabled(boolean enabled) {
        sharedPreferences.edit().putBoolean(KEY_SOUND_ENABLED, enabled).apply();
    }

    // Animation settings
    public boolean areAnimationsEnabled() {
        return sharedPreferences.getBoolean(KEY_ANIMATIONS_ENABLED, DEFAULT_ANIMATIONS_ENABLED);
    }

    public void setAnimationsEnabled(boolean enabled) {
        sharedPreferences.edit().putBoolean(KEY_ANIMATIONS_ENABLED, enabled).apply();
    }

    // Animation speed settings
    public int getAnimationSpeed() {
        return sharedPreferences.getInt(KEY_ANIMATION_SPEED, DEFAULT_ANIMATION_SPEED);
    }

    public void setAnimationSpeed(int speed) {
        sharedPreferences.edit().putInt(KEY_ANIMATION_SPEED, speed).apply();
    }

    /**
     * Converts animation speed value string to milliseconds.
     */
    public static int animationSpeedFromString(String value) {
        switch (value) {
            case "slow":
                return ANIMATION_SPEED_SLOW;
            case "fast":
                return ANIMATION_SPEED_FAST;
            case "normal":
            default:
                return ANIMATION_SPEED_NORMAL;
        }
    }

    /**
     * Converts animation speed milliseconds to string value.
     */
    public static String animationSpeedToString(int speed) {
        if (speed >= ANIMATION_SPEED_SLOW) {
            return "slow";
        } else if (speed <= ANIMATION_SPEED_FAST) {
            return "fast";
        } else {
            return "normal";
        }
    }
}