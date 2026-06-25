package com.example.flashcards.repository;

import com.example.flashcards.entity.ReviewLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface ReviewLogRepository extends JpaRepository<ReviewLogEntity, Long> {

    /** Reviews for a user over a time window (for heatmap & stats). */
    @Query("""
        SELECT rl FROM ReviewLogEntity rl
        WHERE rl.user.username = :username
          AND rl.reviewedAt >= :from
        ORDER BY rl.reviewedAt ASC
        """)
    List<ReviewLogEntity> findByUserSince(
        @Param("username") String username,
        @Param("from") Instant from
    );

    /** Daily review counts for heatmap (last N days). */
    @Query(value = """
        SELECT DATE(reviewed_at) AS day, COUNT(*) AS cnt
        FROM review_logs rl
        JOIN app_users u ON rl.user_id = u.id
        WHERE u.username = :username
          AND reviewed_at >= :from
        GROUP BY DATE(reviewed_at)
        ORDER BY day ASC
        """, nativeQuery = true)
    List<Object[]> dailyCountsSince(
        @Param("username") String username,
        @Param("from") Instant from
    );

    /** Card-level stats: avg rating per card for a user. */
    @Query("""
        SELECT rl.card.id, AVG(rl.rating), COUNT(rl)
        FROM ReviewLogEntity rl
        JOIN rl.card c
        JOIN c.chapter ch
        WHERE rl.user.username = :username
          AND ch.id = :chapterId
        GROUP BY rl.card.id
        """)
    List<Object[]> cardStats(
        @Param("username") String username,
        @Param("chapterId") String chapterId
    );

    long countByUserUsername(String username);
}
