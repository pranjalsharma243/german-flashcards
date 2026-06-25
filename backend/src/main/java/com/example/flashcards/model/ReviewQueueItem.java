package com.example.flashcards.model;

import com.example.flashcards.srs.FsrsEngine;

import java.time.Instant;

/** One card in the review queue, with its FSRS state (null if never reviewed). */
public record ReviewQueueItem(
    String cardId,
    String chapterId,
    String type,
    String article,
    String word,
    String english,
    String hindi,
    boolean isNew,
    // FSRS state (null for new cards)
    Double stability,
    Double difficulty,
    Integer reps,
    Integer lapses,
    FsrsEngine.CardState state,
    Instant dueAt,
    // Preview intervals (days) for each rating — so UI can show "Again: 1d / Hard: 3d / Good: 7d / Easy: 14d"
    int[] previewIntervals
) {}
