package com.tacticmaster.settings;

import static java.util.Objects.isNull;

import android.content.Context;
import android.content.SharedPreferences;

import com.tacticmaster.board.ChessboardPieceManager;

/**
 * Centralized SharedPreferences access for the entire application.
 * Consolidates all preference CRUD operations into a single, typed, testable class.
 * Setting definitions (key, type, default) live in {@link SettingKey}.
 */
public class SettingsManager {

    private static final String PREFS_NAME = "TacticMasterSettings";

    // Animation speed bounds (also enforced by the SeekBar UI)
    public static final int ANIMATION_SPEED_SLOW = 800;
    public static final int ANIMATION_SPEED_NORMAL = 300;

    // Player rating bounds — clamps both UI input and ELO calculator drift
    public static final int MIN_PLAYER_RATING = 1000;
    public static final int MAX_PLAYER_RATING = 3000;

    private final SharedPreferences sharedPreferences;
    private final Context appContext;
    private static SettingsManager instance;

    private SettingsManager(Context context) {
        this.appContext = context.getApplicationContext();
        this.sharedPreferences = appContext
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized SettingsManager getInstance(Context context) {
        if (isNull(instance)) {
            instance = new SettingsManager(context);
        }
        return instance;
    }

    // ---- Generic typed accessors keyed by SettingKey ----

    private boolean getBool(SettingKey k) {
        return sharedPreferences.getBoolean(k.key, k.defaultBool());
    }

    private void setBool(SettingKey k, boolean value) {
        sharedPreferences.edit().putBoolean(k.key, value).apply();
    }

    private int getInt(SettingKey k) {
        return sharedPreferences.getInt(k.key, k.defaultInt());
    }

    private void setInt(SettingKey k, int value) {
        sharedPreferences.edit().putInt(k.key, value).apply();
    }

    private String getString(SettingKey k) {
        return sharedPreferences.getString(k.key, k.defaultString());
    }

    private void setString(SettingKey k, String value) {
        sharedPreferences.edit().putString(k.key, value).apply();
    }

    // ---- Public typed API (unchanged signatures) ----

    public boolean isAutoplayEnabled() {
        return getBool(SettingKey.AUTOPLAY);
    }

    public void setAutoplayEnabled(Boolean v) {
        setBool(SettingKey.AUTOPLAY, v);
    }

    public boolean isSoundEnabled() {
        return getBool(SettingKey.SOUND_ENABLED);
    }

    public void setSoundEnabled(Boolean v) {
        setBool(SettingKey.SOUND_ENABLED, v);
    }

    public boolean areAnimationsEnabled() {
        return getBool(SettingKey.ANIMATIONS_ENABLED);
    }

    public void setAnimationsEnabled(Boolean v) {
        setBool(SettingKey.ANIMATIONS_ENABLED, v);
    }

    public int getAnimationSpeed() {
        return getInt(SettingKey.ANIMATION_SPEED);
    }

    public void setAnimationSpeed(int v) {
        setInt(SettingKey.ANIMATION_SPEED, v);
    }

    public boolean isShowPuzzleRating() {
        return getBool(SettingKey.SHOW_PUZZLE_RATING);
    }

    public void setShowPuzzleRating(Boolean v) {
        setBool(SettingKey.SHOW_PUZZLE_RATING, v);
    }

    public boolean isShowPuzzleId() {
        return getBool(SettingKey.SHOW_PUZZLE_ID);
    }

    public void setShowPuzzleId(Boolean v) {
        setBool(SettingKey.SHOW_PUZZLE_ID, v);
    }

    public boolean isShowPuzzlesCount() {
        return getBool(SettingKey.SHOW_PUZZLES_COUNT);
    }

    public void setShowPuzzlesCount(Boolean v) {
        setBool(SettingKey.SHOW_PUZZLES_COUNT, v);
    }

    public int getPlayerRating() {
        return getInt(SettingKey.PLAYER_RATING);
    }

    public void setPlayerRating(int rating) {
        setInt(SettingKey.PLAYER_RATING, clampRating(rating));
    }

    public String getLastPuzzleId() {
        return getString(SettingKey.LAST_PUZZLE_ID);
    }

    public void setLastPuzzleId(String puzzleId) {
        setString(SettingKey.LAST_PUZZLE_ID, puzzleId);
    }

    public String getPieceSet() {
        String stored = getString(SettingKey.PIECE_SET);
        if (isNull(stored) || stored.isEmpty()) {
            return ChessboardPieceManager.defaultPieceSet(appContext);
        }
        return stored;
    }

    public void setPieceSet(String pieceSet) {
        setString(SettingKey.PIECE_SET, pieceSet);
    }

    private static int clampRating(int rating) {
        return Math.max(MIN_PLAYER_RATING, Math.min(MAX_PLAYER_RATING, rating));
    }
}
