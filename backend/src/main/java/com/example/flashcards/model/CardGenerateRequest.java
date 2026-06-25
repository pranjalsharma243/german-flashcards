package com.example.flashcards.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CardGenerateRequest(
        @NotBlank String topic,
        @NotBlank String chapterId,
        @Min(3) @Max(20) int count
) {}
