package com.example.flashcards.model;

import java.time.Instant;
import java.util.List;

public record VocabRequestDto(
        Long id,
        String submittedBy,
        String status,
        String sourceType,
        List<Card> cards,
        Instant createdAt
) {}
