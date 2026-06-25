package com.example.flashcards.model;

import java.util.List;
import java.util.Map;

public record StatsResponse(
    // Overall
    long totalReviews,
    long totalCards,
    double averageRetention,   // % correct (rating >= 3) over all reviews
    int currentStreak,         // days with at least 1 review
    int longestStreak,
    // Per-chapter
    Map<String, ChapterStats> perChapter,
    // Daily activity (for heatmap) — map of "YYYY-MM-DD" -> count
    Map<String, Integer> dailyActivity,
    // Due forecast: next 7 days -> count
    List<ForecastDay> forecast
) {
    public record ChapterStats(
        int dueNow,
        int totalCards,
        int reviewedCards,
        double avgStability,
        double avgDifficulty
    ) {}

    public record ForecastDay(String date, int dueCount) {}
}
