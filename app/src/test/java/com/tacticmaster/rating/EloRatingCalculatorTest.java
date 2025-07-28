package com.tacticmaster.rating;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class EloRatingCalculatorTest {

    @Test
    void testCalculateNewRatingPlayerWins() {
        int currentRating = 1500;
        int opponentRating = 1500;
        double result = 1.0; // Win

        int newRating = EloRatingCalculator.calculateNewRating(currentRating, opponentRating, result);

        assertEquals(1516, newRating);
    }

    @Test
    void testCalculateNewRatingPlayerLoses() {
        int currentRating = 1500;
        int opponentRating = 1500;
        double result = 0.0; // Loss

        int newRating = EloRatingCalculator.calculateNewRating(currentRating, opponentRating, result);

        assertEquals(1484, newRating);
    }

    @Test
    void testCalculateNewRatingPlayerDraw() {
        int currentRating = 1500;
        int opponentRating = 1500;
        double result = 0.5; // Draw

        int newRating = EloRatingCalculator.calculateNewRating(currentRating, opponentRating, result);

        assertEquals(1500, newRating);
    }

    @Test
    void testCalculateNewRatingWeakerPlayerWinsAgainstStronger() {
        int currentRating = 1200;
        int opponentRating = 1600;
        double result = 1.0; // Win

        int newRating = EloRatingCalculator.calculateNewRating(currentRating, opponentRating, result);

        assertEquals(1229, newRating);
    }

    @Test
    void testCalculateNewRatingStrongerPlayerWinsAgainstWeaker() {
        int currentRating = 1600;
        int opponentRating = 1200;
        double result = 1.0; // Win

        int newRating = EloRatingCalculator.calculateNewRating(currentRating, opponentRating, result);

        assertEquals(1603, newRating);
    }

    @Test
    void testCalculateNewRatingWeakerPlayerLosesAgainstStronger() {
        int currentRating = 1200;
        int opponentRating = 1600;
        double result = 0.0; // Loss

        int newRating = EloRatingCalculator.calculateNewRating(currentRating, opponentRating, result);

        assertEquals(1197, newRating);
    }

    @Test
    void testCalculateNewRatingStrongerPlayerLosesAgainstWeaker() {
        int currentRating = 1600;
        int opponentRating = 1200;
        double result = 0.0; // Loss

        int newRating = EloRatingCalculator.calculateNewRating(currentRating, opponentRating, result);

        assertEquals(1571, newRating);
    }

    @Test
    void testCalculateNewRatingVeryHighRating() {
        int currentRating = 2800;
        int opponentRating = 2800;
        double result = 1.0; // Win

        int newRating = EloRatingCalculator.calculateNewRating(currentRating, opponentRating, result);

        assertEquals(2816, newRating);
    }

    @Test
    void testCalculateNewRatingVeryLowRating() {
        int currentRating = 800;
        int opponentRating = 800;
        double result = 1.0; // Win

        int newRating = EloRatingCalculator.calculateNewRating(currentRating, opponentRating, result);

        assertEquals(816, newRating);
    }

    @Test
    void testCalculateNewRatingExtremeDifference() {
        int currentRating = 1000;
        int opponentRating = 2000;
        double result = 1.0; // Win

        int newRating = EloRatingCalculator.calculateNewRating(currentRating, opponentRating, result);

        assertEquals(1032, newRating);
    }

    @Test
    void testCalculateNewRatingZeroRating() {
        int currentRating = 0;
        int opponentRating = 1500;
        double result = 1.0; // Win

        int newRating = EloRatingCalculator.calculateNewRating(currentRating, opponentRating, result);

        assertEquals(32, newRating);
    }

    @Test
    void testCalculateNewRatingNegativeRating() {
        int currentRating = -100;
        int opponentRating = 1500;
        double result = 1.0; // Win

        int newRating = EloRatingCalculator.calculateNewRating(currentRating, opponentRating, result);

        assertEquals(-68, newRating);
    }
}
