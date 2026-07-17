package com.tacticmaster.settings;

import static java.util.Objects.isNull;

import androidx.preference.PreferenceDataStore;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

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

    private final Map<SettingKey, BooleanSupplier> boolGetters;
    private final Map<SettingKey, Consumer<Boolean>> boolSetters;
    private final Map<SettingKey, IntSupplier> intGetters;
    private final Map<SettingKey, IntConsumer> intSetters;
    private final Map<SettingKey, Supplier<String>> stringGetters;
    private final Map<SettingKey, Consumer<String>> stringSetters;

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

        stringGetters = new EnumMap<>(SettingKey.class);
        stringGetters.put(SettingKey.PIECE_SET, s::getPieceSet);

        stringSetters = new EnumMap<>(SettingKey.class);
        stringSetters.put(SettingKey.PIECE_SET, s::setPieceSet);
    }

    @Override
    public boolean getBoolean(String key, boolean defValue) {
        BooleanSupplier getter = boolGetters.get(SettingKey.fromKey(key));
        return isNull(getter) ? defValue : getter.getAsBoolean();
    }

    @Override
    public void putBoolean(String key, boolean value) {
        Consumer<Boolean> setter = boolSetters.get(SettingKey.fromKey(key));
        if (!isNull(setter)) {
            setter.accept(value);
        }
    }

    @Override
    public int getInt(String key, int defValue) {
        IntSupplier getter = intGetters.get(SettingKey.fromKey(key));
        return isNull(getter) ? defValue : getter.getAsInt();
    }

    @Override
    public void putInt(String key, int value) {
        IntConsumer setter = intSetters.get(SettingKey.fromKey(key));
        if (!isNull(setter)) {
            setter.accept(value);
        }
    }

    @Override
    public String getString(String key, String defValue) {
        SettingKey k = SettingKey.fromKey(key);
        if (isNull(k)) {
            return defValue;
        }
        if (k.type == SettingKey.Type.STRING) {
            Supplier<String> getter = stringGetters.get(k);
            return isNull(getter) ? defValue : getter.get();
        }
        if (k.type == SettingKey.Type.INT) {
            IntSupplier getter = intGetters.get(k);
            if (!isNull(getter)) {
                return String.valueOf(getter.getAsInt());
            }
        }
        return defValue;
    }

    @Override
    public void putString(String key, String value) {
        SettingKey k = SettingKey.fromKey(key);
        if (isNull(k) || isNull(value)) {
            return;
        }
        if (k.type == SettingKey.Type.STRING) {
            Consumer<String> setter = stringSetters.get(k);
            if (!isNull(setter)) {
                setter.accept(value);
            }
            return;
        }
        if (k.type != SettingKey.Type.INT) {
            return;
        }
        IntConsumer setter = intSetters.get(k);
        if (isNull(setter)) {
            return;
        }
        try {
            setter.accept(Integer.parseInt(value.trim()));
        } catch (NumberFormatException ignored) {
            // Invalid input — silently ignore. The change listener in
            // SettingsFragment surfaces a Toast for user-facing feedback.
        }
    }
}
