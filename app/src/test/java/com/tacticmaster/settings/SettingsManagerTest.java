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
    public void testAnimationSpeedFromString_Slow() {
        assertEquals(800, SettingsManager.animationSpeedFromString("slow"));
    }

    @Test
    public void testAnimationSpeedFromString_Normal() {
        assertEquals(300, SettingsManager.animationSpeedFromString("normal"));
    }

    @Test
    public void testAnimationSpeedFromString_Fast() {
        assertEquals(150, SettingsManager.animationSpeedFromString("fast"));
    }

    @Test
    public void testAnimationSpeedFromString_Unknown() {
        assertEquals(300, SettingsManager.animationSpeedFromString("unknown"));
    }

    @Test
    public void testAnimationSpeedToString_Slow() {
        assertEquals("slow", SettingsManager.animationSpeedToString(800));
        assertEquals("slow", SettingsManager.animationSpeedToString(900));
    }

    @Test
    public void testAnimationSpeedToString_Normal() {
        assertEquals("normal", SettingsManager.animationSpeedToString(300));
        assertEquals("normal", SettingsManager.animationSpeedToString(400));
    }

    @Test
    public void testAnimationSpeedToString_Fast() {
        assertEquals("fast", SettingsManager.animationSpeedToString(150));
        assertEquals("fast", SettingsManager.animationSpeedToString(100));
    }

    @Test
    public void testPlayerRating_DefaultValue() {
        when(mockPrefs.getInt(eq("player_rating"), eq(1600))).thenReturn(1600);

        assertEquals(1600, settingsManager.getPlayerRating());
    }

    @Test
    public void testPlayerRating_SetAndGet() {
        settingsManager.setPlayerRating(2000);

        verify(mockEditor).putInt("player_rating", 2000);
        verify(mockEditor).apply();
    }

    @Test
    public void testShowPuzzleRating_DefaultValue() {
        when(mockPrefs.getBoolean(eq("show_puzzle_rating"), eq(true))).thenReturn(true);

        assertTrue(settingsManager.isShowPuzzleRating());
    }

    @Test
    public void testShowPuzzleId_DefaultValue() {
        when(mockPrefs.getBoolean(eq("show_puzzle_id"), eq(true))).thenReturn(true);

        assertTrue(settingsManager.isShowPuzzleId());
    }

    @Test
    public void testShowPuzzlesCount_DefaultValue() {
        when(mockPrefs.getBoolean(eq("show_puzzles_count"), eq(true))).thenReturn(true);

        assertTrue(settingsManager.isShowPuzzlesCount());
    }
}