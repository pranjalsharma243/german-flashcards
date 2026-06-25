package com.example.flashcards.srs;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * FSRS-6 algorithm (Free Spaced Repetition Scheduler).
 * Port of the open-spaced-repetition reference implementation.
 * Default weights are the pre-trained FSRS-6 defaults from the benchmark.
 */
public final class FsrsEngine {

    public static final double DESIRED_RETENTION = 0.9;
    public static final double DECAY = -0.5;
    public static final double FACTOR = Math.pow(0.9, 1.0 / DECAY) - 1; // ≈ 0.23457

    // FSRS-6 default pre-trained weights (19 values)
    private static final double[] W = {
        0.4072, 1.1829, 3.1262, 15.4722,
        7.2102, 0.5316, 1.0651, 0.0589,
        1.5220, 0.1544, 1.0071, 1.9323,
        0.1118, 0.2905, 2.2741, 0.2407,
        2.9466, 0.5034, 0.6567
    };

    private final double[] w;

    public FsrsEngine() {
        this.w = W;
    }

    public FsrsEngine(double[] customWeights) {
        if (customWeights.length != 19) throw new IllegalArgumentException("FSRS-6 requires 19 weights");
        this.w = customWeights;
    }

    public record FsrsState(
        double stability,
        double difficulty,
        int reps,
        int lapses,
        CardState state,
        int nextIntervalDays
    ) {}

    public enum CardState { NEW, LEARNING, REVIEW, RELEARNING }

    /** Called when a card is seen for the first time. Rating 1..4. */
    public FsrsState firstReview(int rating) {
        validateRating(rating);
        double s = initialStability(rating);
        double d = initialDifficulty(rating);
        int interval = nextInterval(s);
        CardState state = rating == 1 ? CardState.LEARNING : CardState.REVIEW;
        return new FsrsState(s, d, 1, rating == 1 ? 1 : 0, state, interval);
    }

    /** Called for every subsequent review. */
    public FsrsState review(double prevStability, double prevDifficulty,
                            int prevReps, int prevLapses, CardState prevState,
                            int elapsedDays, int rating) {
        validateRating(rating);
        double r = retrievability(elapsedDays, prevStability);
        double newD = nextDifficulty(prevDifficulty, rating);
        double newS;
        CardState newState;
        int newLapses = prevLapses;
        if (rating == 1) {
            newS = forgottenStability(newD, prevStability, r);
            newLapses = prevLapses + 1;
            newState = CardState.RELEARNING;
        } else {
            newS = recalledStability(newD, prevStability, r, rating);
            newState = CardState.REVIEW;
        }
        newS = Math.max(0.01, newS);
        int interval = nextInterval(newS);
        return new FsrsState(newS, newD, prevReps + 1, newLapses, newState, interval);
    }

    public double retrievability(int elapsedDays, double stability) {
        return Math.pow(1.0 + FACTOR * elapsedDays / stability, DECAY);
    }

    // ── private helpers ──────────────────────────────────────────────────────

    private double initialStability(int rating) { return w[rating - 1]; }

    private double initialDifficulty(int rating) {
        double d = w[4] - Math.exp(w[5] * (rating - 1)) + 1.0;
        return clampDifficulty(d);
    }

    private double nextDifficulty(double d, int rating) {
        double d0 = initialDifficulty(3); // target for "Good"
        double deltaD = w[6] * (rating - 3);
        double newD = d - deltaD;
        // mean reversion toward D0(Good)
        double reverted = w[7] * d0 + (1.0 - w[7]) * newD;
        return clampDifficulty(reverted);
    }

    private double recalledStability(double d, double s, double r, int rating) {
        double hardPenalty = (rating == 2) ? w[15] : 1.0;
        double easyBonus = (rating == 4) ? w[16] : 1.0;
        return s * Math.exp(w[8]) * (11.0 - d) * Math.pow(s, -w[9])
                * (Math.exp(w[10] * (1.0 - r)) - 1.0)
                * hardPenalty * easyBonus;
    }

    private double forgottenStability(double d, double s, double r) {
        return w[11] * Math.pow(d, -w[12])
                * (Math.pow(s + 1.0, w[13]) - 1.0)
                * Math.exp(w[14] * (1.0 - r));
    }

    private int nextInterval(double stability) {
        // At DESIRED_RETENTION=0.9, interval ≈ stability (elegant FSRS property)
        double t = stability / FACTOR * (Math.pow(DESIRED_RETENTION, 1.0 / DECAY) - 1.0);
        return Math.max(1, (int) Math.round(t));
    }

    private static double clampDifficulty(double d) {
        return Math.max(1.0, Math.min(10.0, d));
    }

    private static void validateRating(int rating) {
        if (rating < 1 || rating > 4) throw new IllegalArgumentException("Rating must be 1..4");
    }

    public static Instant dueInstant(int intervalDays) {
        return Instant.now().plus(intervalDays, ChronoUnit.DAYS);
    }
}
