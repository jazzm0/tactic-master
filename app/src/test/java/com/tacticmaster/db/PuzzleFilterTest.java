package com.tacticmaster.db;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.tacticmaster.puzzle.PuzzleFilter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class PuzzleFilterTest {

    private final Set<String> allThemes = Set.of(
            "advancedPawn", "advantage", "anastasiaMate", "arabianMate", "attackingF2F7",
            "attraction", "backRankMate", "bishopEndgame", "bodenMate", "capturingDefender",
            "castling", "clearance", "crushing", "defensiveMove", "deflection",
            "discoveredAttack", "doubleBishopMate", "doubleCheck", "dovetailMate", "enPassant",
            "endgame", "exposedKing", "fork", "hangingPiece", "hookMate", "interference",
            "intermezzo", "killBoxMate", "kingsideAttack", "knightEndgame", "long", "master",
            "masterVsMaster", "mate", "mateIn1", "mateIn2", "mateIn3", "mateIn4", "mateIn5",
            "middlegame", "oneMove", "opening", "pawnEndgame", "pin", "promotion", "queenEndgame",
            "queenRookEndgame", "queensideAttack", "quietMove", "rookEndgame", "sacrifice",
            "short", "skewer", "smotheredMate", "superGM", "trappedPiece", "underPromotion",
            "veryLong", "vukovicMate", "xRayAttack", "zugzwang"
    );

    private PuzzleFilter puzzleFilter;

    @Mock
    DatabaseAccessor databaseAccessor;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(databaseAccessor.getPuzzleThemes()).thenReturn(allThemes);
        puzzleFilter = new PuzzleFilter(databaseAccessor);

    }

    @Test
    public void testAllThemesExistInMap() {
        Map<String, Set<String>> themeGroups = puzzleFilter.getThemeGroups();


        for (String theme : allThemes) {
            boolean exists = themeGroups.values().stream().anyMatch(set -> set.contains(theme));
            assertTrue(exists, "Theme not found in map: " + theme);
            assertEquals(61, allThemes.size());
        }
    }

    @Test
    public void testThemesAreRemovedIfNotInDB() {
        var reducedThemes = new HashSet<>(allThemes);
        when(databaseAccessor.getPuzzleThemes()).thenReturn(reducedThemes);
        reducedThemes.removeAll(Set.of("mateIn1", "mateIn2", "mateIn3", "mateIn4", "mateIn5", "oneMove"));


        Map<String, Set<String>> themeGroups = puzzleFilter.getThemeGroups();
        assertEquals(themeGroups.size(), 9);
        var totalSize = themeGroups.values().stream()
                .mapToInt(Set::size)
                .sum();

        assertEquals(55, totalSize, "Total size of themes should be 55 after removing mateInX");
    }
}