package com.example.flashcards.model;

import java.io.Serializable;

public record ChapterSummary(
        String id,
        String level,
        String title,
        String theme,
        int cardCount
) implements Serializable {
}
