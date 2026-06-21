package com.example.flashcards.model;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record ProgressUpdate(
        @NotBlank String chapterId,
        List<String> knownCardIds,
        List<String> practiceCardIds
) {
}
