package com.tacticmaster.settings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class SettingKeyTest {

    @Test
    public void fromKey_knownKey_resolvesEnum() {
        assertSame(SettingKey.PLAYER_RATING, SettingKey.fromKey("player_rating"));
        assertSame(SettingKey.PIECE_SET, SettingKey.fromKey("piece_set"));
        assertSame(SettingKey.AUTOPLAY, SettingKey.fromKey("autoplay"));
    }

    @Test
    public void fromKey_unknownKey_returnsNull() {
        assertNull(SettingKey.fromKey("not_a_real_key"));
    }

    @Test
    public void defaults_haveExpectedTypesAndValues() {
        assertEquals(1600, SettingKey.PLAYER_RATING.defaultInt());
        assertEquals(300, SettingKey.ANIMATION_SPEED.defaultInt());
        assertFalse(SettingKey.AUTOPLAY.defaultBool());
        assertTrue(SettingKey.SOUND_ENABLED.defaultBool());
        // PIECE_SET default is null — resolved to the first asset folder at read time.
        assertNull(SettingKey.PIECE_SET.defaultString());
    }

    @Test
    public void everyKey_isResolvableByItsKeyString() {
        for (SettingKey k : SettingKey.values()) {
            assertSame(k, SettingKey.fromKey(k.key));
        }
    }
}
