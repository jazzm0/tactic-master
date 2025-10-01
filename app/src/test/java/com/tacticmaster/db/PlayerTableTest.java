package com.tacticmaster.db;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class PlayerTableTest {

    @Test
    void testPlayerTableConstants() {
        assertEquals(1600, PlayerTable.DEFAULT_PLAYER_RATING);
        assertEquals("player_table", PlayerTable.PLAYER_TABLE_NAME);
        assertEquals("PlayerId", PlayerTable.COLUMN_PLAYER_ID);
        assertEquals("PlayerRating", PlayerTable.COLUMN_PLAYER_RATING);
        assertEquals("AutoplayEnabled", PlayerTable.COLUMN_AUTOPLAY_ENABLED);
    }
}
