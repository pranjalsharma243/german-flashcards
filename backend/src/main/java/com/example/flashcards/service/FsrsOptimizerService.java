package com.example.flashcards.service;

import com.example.flashcards.repository.ReviewLogRepository;
import com.example.flashcards.srs.FsrsEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Lightweight personal FSRS optimizer.
 * After 50+ reviews, estimates a per-user desired retention rate and computes
 * a recommended interval multiplier compared to the default 90% baseline.
 * This avoids a full gradient-descent weight optimization (too compute-heavy
 * for a web request) while still personalizing the experience meaningfully.
 */
@Service
public class FsrsOptimizerService {

    private static final Logger log = LoggerFactory.getLogger(FsrsOptimizerService.class);
    private static final int MIN_REVIEWS = 50;

    private final ReviewLogRepository reviewLogRepo;

    public FsrsOptimizerService(ReviewLogRepository reviewLogRepo) {
        this.reviewLogRepo = reviewLogRepo;
    }

    public record OptimizerResult(
            long totalReviews,
            double estimatedRetention,  // 0.0–1.0 observed recall rate
            double suggestedDesiredRetention,  // recommended FSRS desired retention
            double intervalMultiplier,  // multiply default interval by this
            boolean enoughData,
            String advice
    ) {}

    public OptimizerResult analyze(String username) {
        long total = reviewLogRepo.countByUserUsername(username);
        if (total < MIN_REVIEWS) {
            return new OptimizerResult(total, 0, 0.9, 1.0, false,
                    "Need at least " + MIN_REVIEWS + " reviews to personalize. Keep going!");
        }

        // Pull last 90 days of reviews
        var since = Instant.now().minus(90, ChronoUnit.DAYS);
        var logs = reviewLogRepo.findByUserSince(username, since);

        // Count pass (rating ≥ 3) vs total reviews
        long passes = logs.stream().filter(l -> l.getRating() >= 3).count();
        double retention = logs.isEmpty() ? 0.9 : (double) passes / logs.size();

        // Clamp to [0.7, 0.97]
        retention = Math.max(0.70, Math.min(0.97, retention));

        // Suggest desired retention one step toward ideal 0.9
        double suggested = 0.9;
        if (retention < 0.82) {
            suggested = 0.82; // struggling — ease up
        } else if (retention > 0.95) {
            suggested = 0.95; // acing it — raise bar
        }

        // Interval multiplier: FSRS interval ~ stability at desired retention
        // ratio of stability at suggested vs 0.9 baseline
        double multiplier = computeMultiplier(suggested, 0.9);

        String advice;
        if (retention < 0.82) {
            advice = "Your recall rate is " + pct(retention) + ". Consider reducing daily new cards to let reviews consolidate.";
        } else if (retention > 0.95) {
            advice = "Your recall rate is " + pct(retention) + "! Cards are spacing out further. Keep it up.";
        } else {
            advice = "Your recall rate is " + pct(retention) + ". FSRS is well-calibrated for you.";
        }

        return new OptimizerResult(total, retention, suggested, multiplier, true, advice);
    }

    private double computeMultiplier(double suggested, double baseline) {
        // FSRS retrievability at t=S is (1+FACTOR*1)^DECAY = baseline
        // interval = S * FACTOR * (R^(1/DECAY) - 1) for target R
        // ratio: t_suggested / t_baseline = (suggested^(1/DECAY) - 1) / (baseline^(1/DECAY) - 1)
        double decay = FsrsEngine.DECAY;
        double factor = FsrsEngine.FACTOR;
        double exponent = 1.0 / decay; // negative, so R < 1 → R^exp < 1
        // (R^(1/decay) - 1) / factor  — but factor is negative, so signs work out
        double numerator = Math.pow(suggested, exponent) - 1;
        double denominator = Math.pow(baseline, exponent) - 1;
        if (Math.abs(denominator) < 1e-9) return 1.0;
        double ratio = numerator / denominator;
        return Math.max(0.5, Math.min(2.0, ratio));
    }

    private String pct(double v) {
        return Math.round(v * 100) + "%";
    }
}
