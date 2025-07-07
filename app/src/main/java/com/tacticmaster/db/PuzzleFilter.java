package com.tacticmaster.db;

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

    private static final Map<String, Set<String>> themeGroups = createChessThemeGroups();

    private PuzzleFilter() {
    }

    public static Map<String, Set<String>> createChessThemeGroups() {
        Map<String, Set<String>> themeGroups = new TreeMap<>();

        themeGroups.put(MATE_PATTERNS, Set.of(
                "anastasiaMate", "arabianMate", "backRankMate", "bodenMate",
                "doubleBishopMate", "dovetailMate", "hookMate",
                "killBoxMate", "smotheredMate", "vukovicMate", "mate"
        ));

        themeGroups.put(TACTICAL_MOTIFS, Set.of(
                "attraction", "capturingDefender", "clearance", "deflection",
                "discoveredAttack", "doubleCheck", "fork", "interference",
                "intermezzo", "pin", "skewer", "xRayAttack"
        ));

        themeGroups.put(ATTACKING_STRATEGIES, Set.of(
                "attackingF2F7", "kingsideAttack", "queensideAttack", "exposedKing"
        ));

        themeGroups.put(ENDGAME_TECHNIQUES, Set.of(
                "bishopEndgame", "knightEndgame", "pawnEndgame", "queenEndgame",
                "queenRookEndgame", "rookEndgame", "zugzwang", "endgame"
        ));

        themeGroups.put(PAWN_PLAY, Set.of(
                "advancedPawn", "enPassant", "promotion", "underPromotion"
        ));

        themeGroups.put(POSITIONAL_PLAY, Set.of(
                "advantage", "defensiveMove", "quietMove", "sacrifice", "trappedPiece"
        ));

        themeGroups.put(GAME_PHASES, Set.of(
                "opening", "middlegame", "castling"
        ));

        themeGroups.put(MATE_IN_X_MOVES, Set.of(
                "mateIn1", "mateIn2", "mateIn3", "mateIn4", "mateIn5", "oneMove"
        ));

        themeGroups.put(SHORT_AND_LONG, Set.of(
                "short", "long", "veryLong", "master", "masterVsMaster", "superGM", "crushing"
        ));

        themeGroups.put(PIECE_EXPLOITATION, Set.of(
                "hangingPiece", "trappedPiece"
        ));

        return themeGroups;
    }

    public static Map<String, Set<String>> getThemeGroups() {
        return themeGroups;
    }
}
