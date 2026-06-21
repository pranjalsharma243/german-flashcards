package com.example.flashcards.model;

import jakarta.validation.constraints.NotBlank;

import java.time.Instant;
import java.util.List;

public record Progress(
        @NotBlank String chapterId,
        List<String> knownCardIds,
        List<String> practiceCardIds,
        Instant updatedAt
) {
}
