package com.example.flashcards.service;

import com.example.flashcards.entity.CardEntity;
import com.example.flashcards.entity.ReviewLogEntity;
import com.example.flashcards.entity.ReviewStateEntity;
import com.example.flashcards.model.ReviewQueueItem;
import com.example.flashcards.model.ReviewSubmitResponse;
import com.example.flashcards.model.StatsResponse;
import com.example.flashcards.repository.CardRepository;
import com.example.flashcards.repository.ReviewLogRepository;
import com.example.flashcards.repository.ReviewStateRepository;
import com.example.flashcards.repository.UserRepository;
import com.example.flashcards.srs.FsrsEngine;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@Transactional
public class ReviewService {

    private static final int DEFAULT_QUEUE_LIMIT = 20;
    private static final int NEW_CARD_LIMIT = 10;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final FsrsEngine FSRS = new FsrsEngine();

    private final ReviewStateRepository reviewStateRepo;
    private final ReviewLogRepository reviewLogRepo;
    private final CardRepository cardRepo;
    private final UserRepository userRepo;

    public ReviewService(ReviewStateRepository reviewStateRepo,
                         ReviewLogRepository reviewLogRepo,
                         CardRepository cardRepo,
                         UserRepository userRepo) {
        this.reviewStateRepo = reviewStateRepo;
        this.reviewLogRepo = reviewLogRepo;
        this.cardRepo = cardRepo;
        this.userRepo = userRepo;
    }

    /** Returns the review queue for a user. Mixes due cards + new cards. */
    @Transactional(readOnly = true)
    public List<ReviewQueueItem> getQueue(String username, String chapterId, int limit) {
        Instant now = Instant.now();
        List<ReviewQueueItem> result = new ArrayList<>();

        // 1. Due cards (already reviewed at least once, now overdue/due)
        List<ReviewStateEntity> dueStates = chapterId != null
                ? reviewStateRepo.findDueByChapter(username, chapterId, now)
                : reviewStateRepo.findDueAll(username, now);

        for (ReviewStateEntity rs : dueStates) {
            if (result.size() >= limit) break;
            result.add(toQueueItem(rs.getCard(), rs, false));
        }

        // 2. Fill remaining slots with new cards (never reviewed)
        if (result.size() < limit) {
            int newLimit = Math.min(NEW_CARD_LIMIT, limit - result.size());
            List<CardEntity> allCards = chapterId != null
                    ? cardRepo.findByChapterIdOrderByPosition(chapterId)
                    : cardRepo.findAll();

            // Find cards with no review state for this user
            int added = 0;
            for (CardEntity card : allCards) {
                if (added >= newLimit) break;
                Optional<ReviewStateEntity> existing =
                        reviewStateRepo.findByUserUsernameAndCardId(username, card.getId());
                if (existing.isEmpty()) {
                    result.add(toQueueItem(card, null, true));
                    added++;
                }
            }
        }

        return result;
    }

    /** Submits a review rating (1..4) for a card. Creates/updates ReviewState and writes a ReviewLog. */
    public ReviewSubmitResponse submitReview(String username, String cardId, int rating) {
        var user = userRepo.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        var card = cardRepo.findById(cardId)
                .orElseThrow(() -> new NoSuchElementException("Card not found: " + cardId));

        Instant now = Instant.now();
        Optional<ReviewStateEntity> existing =
                reviewStateRepo.findByUserUsernameAndCardId(username, cardId);

        FsrsEngine.FsrsState newState;
        int elapsedDays;
        int scheduledDays;

        ReviewStateEntity rs;
        if (existing.isEmpty()) {
            // First review
            newState = FSRS.firstReview(rating);
            elapsedDays = 0;
            scheduledDays = newState.nextIntervalDays();
            rs = new ReviewStateEntity();
            rs.setUser(user);
            rs.setCard(card);
        } else {
            rs = existing.get();
            long ms = ChronoUnit.MILLIS.between(rs.getLastReviewedAt(), now);
            elapsedDays = (int) Math.max(0, ms / 86_400_000L);
            scheduledDays = (int) ChronoUnit.DAYS.between(rs.getLastReviewedAt(), rs.getDueAt());
            newState = FSRS.review(
                    rs.getStability(), rs.getDifficulty(),
                    rs.getReps(), rs.getLapses(), rs.getState(),
                    elapsedDays, rating);
        }

        // Update state
        rs.setStability(newState.stability());
        rs.setDifficulty(newState.difficulty());
        rs.setReps(newState.reps());
        rs.setLapses(newState.lapses());
        rs.setState(newState.state());
        rs.setLastReviewedAt(now);
        rs.setDueAt(FsrsEngine.dueInstant(newState.nextIntervalDays()));
        reviewStateRepo.save(rs);

        // Append immutable log
        ReviewLogEntity log = new ReviewLogEntity();
        log.setUser(user);
        log.setCard(card);
        log.setRating(rating);
        log.setElapsedDays(elapsedDays);
        log.setScheduledDays(scheduledDays);
        log.setStability(newState.stability());
        log.setDifficulty(newState.difficulty());
        log.setReviewedAt(now);
        log.setState(newState.state());
        reviewLogRepo.save(log);

        return new ReviewSubmitResponse(
                cardId, rating, newState.nextIntervalDays(), rs.getDueAt(),
                newState.stability(), newState.difficulty(),
                newState.reps(), newState.lapses(), newState.state());
    }

    /** Statistics for the stats dashboard. */
    @Transactional(readOnly = true)
    public StatsResponse getStats(String username) {
        Instant now = Instant.now();
        Instant from90 = now.minus(90, ChronoUnit.DAYS);

        long totalReviews = reviewLogRepo.countByUserUsername(username);
        List<Object[]> daily = reviewLogRepo.dailyCountsSince(username, from90);

        Map<String, Integer> dailyActivity = new LinkedHashMap<>();
        for (Object[] row : daily) {
            dailyActivity.put(row[0].toString(), ((Number) row[1]).intValue());
        }

        // Streak
        int[] streaks = computeStreaks(dailyActivity, now);
        int currentStreak = streaks[0];
        int longestStreak = streaks[1];

        // Average retention: fraction of reviews rated >= 3
        List<ReviewLogEntity> logs = reviewLogRepo.findByUserSince(username, from90);
        long goodOrEasy = logs.stream().filter(l -> l.getRating() >= 3).count();
        double avgRetention = logs.isEmpty() ? 0.0 : (double) goodOrEasy / logs.size();

        // Due forecast: next 7 days — pull all future states once, bucket by day
        Instant sevenDaysOut = now.plus(7, ChronoUnit.DAYS);
        List<ReviewStateEntity> futureStates = reviewStateRepo.findDueAll(username, sevenDaysOut);
        List<StatsResponse.ForecastDay> forecast = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate day = LocalDate.now(ZoneOffset.UTC).plusDays(i);
            Instant dayStart = day.atStartOfDay().toInstant(ZoneOffset.UTC);
            Instant dayEnd = day.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
            long count = futureStates.stream()
                    .filter(rs -> !rs.getDueAt().isBefore(dayStart) && rs.getDueAt().isBefore(dayEnd))
                    .count();
            forecast.add(new StatsResponse.ForecastDay(day.format(DATE_FMT), (int) count));
        }

        return new StatsResponse(
                totalReviews,
                cardRepo.count(),
                avgRetention,
                currentStreak,
                longestStreak,
                Map.of(),   // perChapter populated on demand
                dailyActivity,
                forecast
        );
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private ReviewQueueItem toQueueItem(CardEntity card, ReviewStateEntity rs, boolean isNew) {
        int[] previews = previewIntervals(rs);
        return new ReviewQueueItem(
                card.getId(),
                card.getChapter().getId(),
                card.getType(),
                card.getArticle(),
                card.getWord(),
                card.getEnglish(),
                card.getHindi(),
                isNew,
                rs == null ? null : rs.getStability(),
                rs == null ? null : rs.getDifficulty(),
                rs == null ? null : rs.getReps(),
                rs == null ? null : rs.getLapses(),
                rs == null ? null : rs.getState(),
                rs == null ? null : rs.getDueAt(),
                previews
        );
    }

    /**
     * For each possible rating, what interval would be scheduled?
     * Shown in the UI under Again/Hard/Good/Easy buttons.
     */
    private int[] previewIntervals(ReviewStateEntity rs) {
        int[] intervals = new int[4];
        if (rs == null) {
            // New card
            for (int r = 1; r <= 4; r++) {
                intervals[r - 1] = FSRS.firstReview(r).nextIntervalDays();
            }
        } else {
            Instant now = Instant.now();
            long ms = ChronoUnit.MILLIS.between(rs.getLastReviewedAt(), now);
            int elapsedDays = (int) Math.max(0, ms / 86_400_000L);
            for (int r = 1; r <= 4; r++) {
                intervals[r - 1] = FSRS.review(
                        rs.getStability(), rs.getDifficulty(),
                        rs.getReps(), rs.getLapses(), rs.getState(),
                        elapsedDays, r).nextIntervalDays();
            }
        }
        return intervals;
    }

    private int[] computeStreaks(Map<String, Integer> daily, Instant now) {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        int current = 0;
        int longest = 0;
        int run = 0;
        // walk backward up to 365 days
        for (int i = 0; i < 365; i++) {
            String day = today.minusDays(i).format(DATE_FMT);
            if (daily.containsKey(day) && daily.get(day) > 0) {
                run++;
                if (i == 0 || i <= current + 1) current = run;
                longest = Math.max(longest, run);
            } else {
                if (i > 0) run = 0;
            }
        }
        return new int[]{current, longest};
    }
}
