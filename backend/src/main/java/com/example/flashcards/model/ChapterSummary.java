package com.example.flashcards.model;

public record ChapterSummary(
        String id,
        String level,
        String title,
        String theme,
        int cardCount
) {
}
