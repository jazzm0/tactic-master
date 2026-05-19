package com.tacticmaster.settings;

import java.util.HashMap;
import java.util.Map;

/**
 * Centralized definition of every persisted setting. Each entry pairs a
 * SharedPreferences key with its value type and default. The enum is the single
 * source of truth for what settings exist; SettingsManager and
 * SettingsPreferenceDataStore both read from it.
 */
public enum SettingKey {

    AUTOPLAY("autoplay", Type.BOOL, false),
    SOUND_ENABLED("sound_enabled", Type.BOOL, true),
    ANIMATIONS_ENABLED("animations_enabled", Type.BOOL, true),
    ANIMATION_SPEED("animation_speed", Type.INT, 300),
    SHOW_PUZZLE_RATING("show_puzzle_rating", Type.BOOL, true),
    SHOW_PUZZLE_ID("show_puzzle_id", Type.BOOL, true),
    SHOW_PUZZLES_COUNT("show_puzzles_count", Type.BOOL, true),
    PLAYER_RATING("player_rating", Type.INT, 1600);

    public enum Type {BOOL, INT}

    public final String key;
    public final Type type;
    public final Object defaultValue;

    SettingKey(String key, Type type, Object defaultValue) {
        this.key = key;
        this.type = type;
        this.defaultValue = defaultValue;
    }

    public boolean defaultBool() {
        return (Boolean) defaultValue;
    }

    public int defaultInt() {
        return (Integer) defaultValue;
    }

    private static final Map<String, SettingKey> BY_KEY = buildIndex();

    private static Map<String, SettingKey> buildIndex() {
        Map<String, SettingKey> map = new HashMap<>();
        for (SettingKey k : values()) {
            map.put(k.key, k);
        }
        return map;
    }

    public static SettingKey fromKey(String key) {
        return BY_KEY.get(key);
    }
}
