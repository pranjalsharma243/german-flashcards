package com.example.flashcards.repository;

import com.example.flashcards.entity.ReviewStateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ReviewStateRepository extends JpaRepository<ReviewStateEntity, Long> {

    Optional<ReviewStateEntity> findByUserUsernameAndCardId(String username, String cardId);

    /** Cards due now for a user in a specific chapter. */
    @Query("""
        SELECT rs FROM ReviewStateEntity rs
        JOIN FETCH rs.card c
        JOIN c.chapter ch
        WHERE rs.user.username = :username
          AND ch.id = :chapterId
          AND rs.dueAt <= :now
        ORDER BY rs.dueAt ASC
        """)
    List<ReviewStateEntity> findDueByChapter(
        @Param("username") String username,
        @Param("chapterId") String chapterId,
        @Param("now") Instant now
    );

    /** All due cards for a user across all chapters. */
    @Query("""
        SELECT rs FROM ReviewStateEntity rs
        JOIN FETCH rs.card c
        WHERE rs.user.username = :username
          AND rs.dueAt <= :now
        ORDER BY rs.dueAt ASC
        """)
    List<ReviewStateEntity> findDueAll(
        @Param("username") String username,
        @Param("now") Instant now
    );

    /** Count of due cards per chapter for the dashboard widget. */
    @Query("""
        SELECT c.chapter.id, COUNT(rs)
        FROM ReviewStateEntity rs
        JOIN rs.card c
        WHERE rs.user.username = :username
          AND rs.dueAt <= :now
        GROUP BY c.chapter.id
        """)
    List<Object[]> countDuePerChapter(
        @Param("username") String username,
        @Param("now") Instant now
    );

    /** All states for a user in a chapter (for stats). */
    @Query("""
        SELECT rs FROM ReviewStateEntity rs
        JOIN rs.card c
        JOIN c.chapter ch
        WHERE rs.user.username = :username
          AND ch.id = :chapterId
        """)
    List<ReviewStateEntity> findAllByChapter(
        @Param("username") String username,
        @Param("chapterId") String chapterId
    );
}
