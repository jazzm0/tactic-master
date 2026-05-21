package com.tacticmaster.settings;

import static java.util.Objects.isNull;

import androidx.preference.PreferenceDataStore;

import java.util.EnumMap;
import java.util.Map;

/**
 * Bridges the AndroidX Preference framework to {@link SettingsManager}, so that
 * SettingsManager remains the only class that touches SharedPreferences.
 *
 * <p>Routing is configured as Map&lt;SettingKey, Lambda&gt; tables — one pair of
 * getter/setter maps per primitive type. The framework's {@code key} string is
 * resolved to a {@link SettingKey} and dispatched.</p>
 *
 * <p>{@code EditTextPreference} is String-typed at the framework level even when
 * the on-disk value is an int (e.g. player rating). The {@code String} overrides
 * here translate to int and route through the int maps.</p>
 */
public class SettingsPreferenceDataStore extends PreferenceDataStore {

    @FunctionalInterface
    public interface BoolGetter {
        boolean get();
    }

    @FunctionalInterface
    public interface BoolSetter {
        void set(boolean value);
    }

    @FunctionalInterface
    public interface IntGetter {
        int get();
    }

    @FunctionalInterface
    public interface IntSetter {
        void set(int value);
    }

    private final Map<SettingKey, BoolGetter> boolGetters;
    private final Map<SettingKey, BoolSetter> boolSetters;
    private final Map<SettingKey, IntGetter> intGetters;
    private final Map<SettingKey, IntSetter> intSetters;

    public SettingsPreferenceDataStore(SettingsManager s) {
        boolGetters = new EnumMap<>(SettingKey.class);
        boolGetters.put(SettingKey.AUTOPLAY, s::isAutoplayEnabled);
        boolGetters.put(SettingKey.SOUND_ENABLED, s::isSoundEnabled);
        boolGetters.put(SettingKey.ANIMATIONS_ENABLED, s::areAnimationsEnabled);
        boolGetters.put(SettingKey.SHOW_PUZZLE_RATING, s::isShowPuzzleRating);
        boolGetters.put(SettingKey.SHOW_PUZZLE_ID, s::isShowPuzzleId);
        boolGetters.put(SettingKey.SHOW_PUZZLES_COUNT, s::isShowPuzzlesCount);

        boolSetters = new EnumMap<>(SettingKey.class);
        boolSetters.put(SettingKey.AUTOPLAY, s::setAutoplayEnabled);
        boolSetters.put(SettingKey.SOUND_ENABLED, s::setSoundEnabled);
        boolSetters.put(SettingKey.ANIMATIONS_ENABLED, s::setAnimationsEnabled);
        boolSetters.put(SettingKey.SHOW_PUZZLE_RATING, s::setShowPuzzleRating);
        boolSetters.put(SettingKey.SHOW_PUZZLE_ID, s::setShowPuzzleId);
        boolSetters.put(SettingKey.SHOW_PUZZLES_COUNT, s::setShowPuzzlesCount);

        intGetters = new EnumMap<>(SettingKey.class);
        intGetters.put(SettingKey.ANIMATION_SPEED, s::getAnimationSpeed);
        intGetters.put(SettingKey.PLAYER_RATING, s::getPlayerRating);

        intSetters = new EnumMap<>(SettingKey.class);
        intSetters.put(SettingKey.ANIMATION_SPEED, s::setAnimationSpeed);
        intSetters.put(SettingKey.PLAYER_RATING, s::setPlayerRating);
    }

    @Override
    public boolean getBoolean(String key, boolean defValue) {
        BoolGetter getter = boolGetters.get(SettingKey.fromKey(key));
        return isNull(getter) ? defValue : getter.get();
    }

    @Override
    public void putBoolean(String key, boolean value) {
        BoolSetter setter = boolSetters.get(SettingKey.fromKey(key));
        if (!isNull(setter)) {
            setter.set(value);
        }
    }

    @Override
    public int getInt(String key, int defValue) {
        IntGetter getter = intGetters.get(SettingKey.fromKey(key));
        return isNull(getter) ? defValue : getter.get();
    }

    @Override
    public void putInt(String key, int value) {
        IntSetter setter = intSetters.get(SettingKey.fromKey(key));
        if (!isNull(setter)) {
            setter.set(value);
        }
    }

    @Override
    public String getString(String key, String defValue) {
        SettingKey k = SettingKey.fromKey(key);
        if (!isNull(k) && k.type == SettingKey.Type.INT) {
            IntGetter getter = intGetters.get(k);
            if (!isNull(getter)) {
                return String.valueOf(getter.get());
            }
        }
        return defValue;
    }

    @Override
    public void putString(String key, String value) {
        SettingKey k = SettingKey.fromKey(key);
        if (isNull(k) || k.type != SettingKey.Type.INT || isNull(value)) {
            return;
        }
        IntSetter setter = intSetters.get(k);
        if (isNull(setter)) {
            return;
        }
        try {
            setter.set(Integer.parseInt(value.trim()));
        } catch (NumberFormatException ignored) {
            // Invalid input — silently ignore. The change listener in
            // SettingsFragment surfaces a Toast for user-facing feedback.
        }
    }
}
