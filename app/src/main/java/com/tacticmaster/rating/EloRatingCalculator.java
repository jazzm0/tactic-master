package com.tacticmaster.rating;

public class EloRatingCalculator {

    private static final int K = 32; // K-factor, which determines the sensitivity of rating changes

    public static int calculateNewRating(int currentRating, int opponentRating, double result) {
        double expectedScore = 1 / (1 + Math.pow(10, (opponentRating - currentRating) / 400.0));
        return (int) Math.round(currentRating + K * (result - expectedScore));
    }
}
