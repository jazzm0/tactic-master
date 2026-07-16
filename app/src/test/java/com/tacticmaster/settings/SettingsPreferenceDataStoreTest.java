package com.tacticmaster.settings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class SettingsPreferenceDataStoreTest {

    @Mock
    private SettingsManager settingsManager;

    private SettingsPreferenceDataStore dataStore;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        dataStore = new SettingsPreferenceDataStore(settingsManager);
    }

    // ---- Boolean routing ----

    @Test
    public void getBoolean_routesAutoplayToSettingsManager() {
        when(settingsManager.isAutoplayEnabled()).thenReturn(true);

        assertTrue(dataStore.getBoolean("autoplay", false));
    }

    @Test
    public void putBoolean_routesAutoplayToSettingsManager() {
        dataStore.putBoolean("autoplay", true);

        verify(settingsManager).setAutoplayEnabled(true);
    }

    @Test
    public void putBoolean_routesShowPuzzleRatingToSettingsManager() {
        dataStore.putBoolean("show_puzzle_rating", false);

        verify(settingsManager).setShowPuzzleRating(false);
    }

    @Test
    public void getBoolean_unknownKeyReturnsDefault() {
        assertFalse(dataStore.getBoolean("not_a_real_key", false));
        assertTrue(dataStore.getBoolean("not_a_real_key", true));
    }

    // ---- Int routing ----

    @Test
    public void getInt_routesAnimationSpeedToSettingsManager() {
        when(settingsManager.getAnimationSpeed()).thenReturn(500);

        assertEquals(500, dataStore.getInt("animation_speed", 0));
    }

    @Test
    public void putInt_routesAnimationSpeedToSettingsManager() {
        dataStore.putInt("animation_speed", 600);

        verify(settingsManager).setAnimationSpeed(600);
    }

    @Test
    public void getInt_unknownKeyReturnsDefault() {
        assertEquals(42, dataStore.getInt("not_a_real_key", 42));
    }

    // ---- String routing (EditTextPreference bridge for player_rating) ----

    @Test
    public void getString_playerRatingReturnsStringifiedInt() {
        when(settingsManager.getPlayerRating()).thenReturn(1750);

        assertEquals("1750", dataStore.getString("player_rating", "0"));
    }

    @Test
    public void putString_playerRatingParsesAndRoutesToSettingsManager() {
        dataStore.putString("player_rating", "1750");

        verify(settingsManager).setPlayerRating(1750);
    }

    @Test
    public void putString_playerRatingTrimsWhitespace() {
        dataStore.putString("player_rating", "  1800  ");

        verify(settingsManager).setPlayerRating(1800);
    }

    @Test
    public void putString_playerRatingIgnoresInvalidNumber() {
        dataStore.putString("player_rating", "not-a-number");

        verify(settingsManager, never()).setPlayerRating(anyIntValue());
    }

    @Test
    public void putString_playerRatingIgnoresNull() {
        dataStore.putString("player_rating", null);

        verify(settingsManager, never()).setPlayerRating(anyIntValue());
    }

    @Test
    public void getString_unknownKeyReturnsDefault() {
        assertEquals("fallback", dataStore.getString("not_a_real_key", "fallback"));
    }

    @Test
    public void getString_pieceSetRoutesToSettingsManager() {
        when(settingsManager.getPieceSet()).thenReturn("lichess");

        assertEquals("lichess", dataStore.getString("piece_set", "classic"));
    }

    @Test
    public void putString_pieceSetRoutesToSettingsManager() {
        dataStore.putString("piece_set", "lichess");

        verify(settingsManager).setPieceSet("lichess");
    }

    @Test
    public void putString_pieceSetIgnoresNull() {
        dataStore.putString("piece_set", null);

        verify(settingsManager, never()).setPieceSet(org.mockito.ArgumentMatchers.any());
    }

    @Test
    public void getString_boolKeyReturnsDefault() {
        // String overrides only translate INT-typed keys; bool keys must not match.
        assertEquals("fallback", dataStore.getString("autoplay", "fallback"));
    }

    private static int anyIntValue() {
        return org.mockito.ArgumentMatchers.anyInt();
    }
}
