package com.tacticmaster.db;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class PuzzleFilter {

    public static final String MATE_PATTERNS = "Mate Patterns";
    public static final String TACTICAL_MOTIFS = "Tactical Motifs";
    public static final String ATTACKING_STRATEGIES = "Attacking Strategies";
    public static final String ENDGAME_TECHNIQUES = "Endgame Techniques";
    public static final String PAWN_PLAY = "Pawn Play";
    public static final String POSITIONAL_PLAY = "Positional and Strategic Concepts";
    public static final String GAME_PHASES = "Game Phase Specific";
    public static final String MATE_IN_X_MOVES = "Mate in Fixed Moves";
    public static final String SHORT_AND_LONG = "Puzzle Complexity and Skill Level";
    public static final String PIECE_EXPLOITATION = "Piece Exploitation";

    private final Map<String, Set<String>> themeGroups;
    private final DatabaseAccessor databaseAccessor;

    public PuzzleFilter(DatabaseAccessor databaseAccessor) {
        this.databaseAccessor = databaseAccessor;
        this.themeGroups = createChessThemeGroups();
    }

    public Map<String, Set<String>> createChessThemeGroups() {
        Map<String, Set<String>> themeGroups = new TreeMap<>();

        themeGroups.put(MATE_PATTERNS, new HashSet<>(Arrays.asList(
                "anastasiaMate", "arabianMate", "backRankMate", "bodenMate",
                "doubleBishopMate", "dovetailMate", "hookMate",
                "killBoxMate", "smotheredMate", "vukovicMate", "mate"
        )));

        themeGroups.put(TACTICAL_MOTIFS, new HashSet<>(Arrays.asList(
                "attraction", "capturingDefender", "clearance", "deflection",
                "discoveredAttack", "doubleCheck", "fork", "interference",
                "intermezzo", "pin", "skewer", "xRayAttack"
        )));

        themeGroups.put(ATTACKING_STRATEGIES, new HashSet<>(Arrays.asList(
                "attackingF2F7", "kingsideAttack", "queensideAttack", "exposedKing"
        )));

        themeGroups.put(ENDGAME_TECHNIQUES, new HashSet<>(Arrays.asList(
                "bishopEndgame", "knightEndgame", "pawnEndgame", "queenEndgame",
                "queenRookEndgame", "rookEndgame", "zugzwang", "endgame"
        )));

        themeGroups.put(PAWN_PLAY, new HashSet<>(Arrays.asList(
                "advancedPawn", "enPassant", "promotion", "underPromotion"
        )));

        themeGroups.put(POSITIONAL_PLAY, new HashSet<>(Arrays.asList(
                "advantage", "defensiveMove", "quietMove", "sacrifice"
        )));

        themeGroups.put(GAME_PHASES, new HashSet<>(Arrays.asList(
                "opening", "middlegame", "castling"
        )));

        themeGroups.put(MATE_IN_X_MOVES, new HashSet<>(Arrays.asList(
                "mateIn1", "mateIn2", "mateIn3", "mateIn4", "mateIn5", "oneMove"
        )));

        themeGroups.put(SHORT_AND_LONG, new HashSet<>(Arrays.asList(
                "short", "long", "veryLong", "master", "masterVsMaster", "superGM", "crushing"
        )));

        themeGroups.put(PIECE_EXPLOITATION, new HashSet<>(Arrays.asList(
                "hangingPiece", "trappedPiece"
        )));

        var puzzleThemes = databaseAccessor.getPuzzleThemes();
        Map<String, Set<String>> themeGroupsCopy = new TreeMap<>();

        for (Map.Entry<String, Set<String>> entry : themeGroups.entrySet()) {
            var themes = entry.getValue();
            var themeGroupKey = entry.getKey();
            themes.removeIf(theme -> !puzzleThemes.contains(theme));
            if (!themes.isEmpty()) {
                themeGroupsCopy.put(themeGroupKey, themes);
            }
        }

        return themeGroupsCopy;
    }

    public Map<String, Set<String>> getThemeGroups() {
        return themeGroups;
    }
}
