package com.tacticmaster.settings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

/**
 * JVM unit tests for the logic in {@link PieceSetPreference} that does not require
 * a live Android {@code Preference} instance. Behavior that depends on a real
 * preference (persistence, summary, default fallback) is covered by
 * {@code PieceSetPreferenceInstrumentedTest}.
 */
public class PieceSetPreferenceTest {

    @Test
    public void testCapitalize_LowercaseWord() {
        assertEquals("Classic", PieceSetPreference.capitalize("classic"));
    }

    @Test
    public void testCapitalize_AlreadyCapitalized() {
        assertEquals("Merida", PieceSetPreference.capitalize("Merida"));
    }

    @Test
    public void testCapitalize_SingleCharacter() {
        assertEquals("A", PieceSetPreference.capitalize("a"));
    }

    @Test
    public void testCapitalize_EmptyString_ReturnedUnchanged() {
        assertEquals("", PieceSetPreference.capitalize(""));
    }

    @Test
    public void testCapitalize_Null_ReturnsNull() {
        assertNull(PieceSetPreference.capitalize(null));
    }

    @Test
    public void testCapitalize_LeadingDigit_Unchanged() {
        assertEquals("3d", PieceSetPreference.capitalize("3d"));
    }

    @Test
    public void testCapitalize_DoesNotAlterRemainingCharacters() {
        assertEquals("LiChess", PieceSetPreference.capitalize("liChess"));
    }
}
