package com.tacticmaster.settings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.SharedPreferences;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SettingsManagerTest {

    @Mock
    private Context mockContext;

    @Mock
    private SharedPreferences mockPrefs;

    @Mock
    private SharedPreferences.Editor mockEditor;

    private SettingsManager settingsManager;

    @Before
    public void setUp() {
        when(mockContext.getApplicationContext()).thenReturn(mockContext);
        when(mockContext.getSharedPreferences(anyString(), anyInt())).thenReturn(mockPrefs);
        when(mockPrefs.edit()).thenReturn(mockEditor);
        when(mockEditor.putBoolean(anyString(), eq(true))).thenReturn(mockEditor);
        when(mockEditor.putBoolean(anyString(), eq(false))).thenReturn(mockEditor);
        when(mockEditor.putInt(anyString(), anyInt())).thenReturn(mockEditor);

        settingsManager = SettingsManager.getInstance(mockContext);
    }

    @Test
    public void testGetInstance_ReturnsSingleton() {
        SettingsManager instance1 = SettingsManager.getInstance(mockContext);
        SettingsManager instance2 = SettingsManager.getInstance(mockContext);

        assertNotNull(instance1);
        assertEquals(instance1, instance2);
    }

    @Test
    public void testAutoplay_DefaultValue() {
        when(mockPrefs.getBoolean(eq("autoplay"), eq(false))).thenReturn(false);

        assertFalse(settingsManager.isAutoplayEnabled());
    }

    @Test
    public void testAutoplay_SetAndGet() {
        settingsManager.setAutoplayEnabled(true);

        verify(mockEditor).putBoolean("autoplay", true);
        verify(mockEditor).apply();
    }

    @Test
    public void testSound_DefaultValue() {
        when(mockPrefs.getBoolean(eq("sound_enabled"), eq(true))).thenReturn(true);

        assertTrue(settingsManager.isSoundEnabled());
    }

    @Test
    public void testSound_SetAndGet() {
        settingsManager.setSoundEnabled(false);

        verify(mockEditor).putBoolean("sound_enabled", false);
        verify(mockEditor).apply();
    }

    @Test
    public void testAnimations_DefaultValue() {
        when(mockPrefs.getBoolean(eq("animations_enabled"), eq(true))).thenReturn(true);

        assertTrue(settingsManager.areAnimationsEnabled());
    }

    @Test
    public void testAnimations_SetAndGet() {
        settingsManager.setAnimationsEnabled(false);

        verify(mockEditor).putBoolean("animations_enabled", false);
        verify(mockEditor).apply();
    }

    @Test
    public void testAnimationSpeed_DefaultValue() {
        when(mockPrefs.getInt(eq("animation_speed"), eq(300))).thenReturn(300);

        assertEquals(300, settingsManager.getAnimationSpeed());
    }

    @Test
    public void testAnimationSpeed_SetAndGet() {
        settingsManager.setAnimationSpeed(800);

        verify(mockEditor).putInt("animation_speed", 800);
        verify(mockEditor).apply();
    }

    @Test
    public void testPlayerRating_DefaultValue() {
        when(mockPrefs.getInt(eq("player_rating"), eq(1600))).thenReturn(1600);

        assertEquals(1600, settingsManager.getPlayerRating());
    }

    @Test
    public void testPlayerRating_ReadsStoredValue() {
        when(mockPrefs.getInt(eq("player_rating"), eq(1600))).thenReturn(1800);

        assertEquals(1800, settingsManager.getPlayerRating());
    }

    @Test
    public void testPlayerRating_SetAndGet() {
        settingsManager.setPlayerRating(2000);

        verify(mockEditor).putInt("player_rating", 2000);
        verify(mockEditor).apply();
    }

    @Test
    public void testPlayerRating_ClampsBelowMinimum() {
        settingsManager.setPlayerRating(500);

        verify(mockEditor).putInt("player_rating", 1000);
        verify(mockEditor).apply();
    }

    @Test
    public void testPlayerRating_ClampsAboveMaximum() {
        settingsManager.setPlayerRating(5000);

        verify(mockEditor).putInt("player_rating", 3000);
        verify(mockEditor).apply();
    }

    @Test
    public void testPlayerRating_AcceptsBoundaryValues() {
        settingsManager.setPlayerRating(1000);
        verify(mockEditor).putInt("player_rating", 1000);

        settingsManager.setPlayerRating(3000);
        verify(mockEditor).putInt("player_rating", 3000);
    }

    @Test
    public void testShowPuzzleRating_DefaultValue() {
        when(mockPrefs.getBoolean(eq("show_puzzle_rating"), eq(true))).thenReturn(true);

        assertTrue(settingsManager.isShowPuzzleRating());
    }

    @Test
    public void testShowPuzzleRating_SetAndGet() {
        settingsManager.setShowPuzzleRating(false);

        verify(mockEditor).putBoolean("show_puzzle_rating", false);
        verify(mockEditor).apply();
    }

    @Test
    public void testShowPuzzleId_DefaultValue() {
        when(mockPrefs.getBoolean(eq("show_puzzle_id"), eq(true))).thenReturn(true);

        assertTrue(settingsManager.isShowPuzzleId());
    }

    @Test
    public void testShowPuzzleId_SetAndGet() {
        settingsManager.setShowPuzzleId(false);

        verify(mockEditor).putBoolean("show_puzzle_id", false);
        verify(mockEditor).apply();
    }

    @Test
    public void testShowPuzzlesCount_DefaultValue() {
        when(mockPrefs.getBoolean(eq("show_puzzles_count"), eq(true))).thenReturn(true);

        assertTrue(settingsManager.isShowPuzzlesCount());
    }

    @Test
    public void testShowPuzzlesCount_SetAndGet() {
        settingsManager.setShowPuzzlesCount(false);

        verify(mockEditor).putBoolean("show_puzzles_count", false);
        verify(mockEditor).apply();
    }
}