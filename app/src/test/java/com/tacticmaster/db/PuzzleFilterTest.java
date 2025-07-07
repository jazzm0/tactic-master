package com.tacticmaster.db;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;


public class PuzzleFilterTest {

    @Test
    public void testAllThemesExistInMap() {
        Map<String, Set<String>> themeGroups = PuzzleFilter.createChessThemeGroups();

        Set<String> allThemes = Set.of(
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

        for (String theme : allThemes) {
            boolean exists = themeGroups.values().stream().anyMatch(set -> set.contains(theme));
            assertTrue(exists, "Theme not found in map: " + theme);
            assertEquals(61, allThemes.size());
        }
    }
}