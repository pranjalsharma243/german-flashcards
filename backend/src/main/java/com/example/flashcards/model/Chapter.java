package com.example.flashcards.model;

import java.io.Serializable;
import java.util.List;

public record Chapter(
        String id,
        String level,
        String title,
        String theme,
        List<Card> cards
) implements Serializable {
}
