package com.example.flashcards.model;

import jakarta.validation.constraints.NotBlank;

public record HintRequest(
        @NotBlank String cardId,
        @NotBlank String word,
        String article,
        @NotBlank String english,
        String hindi,
        @NotBlank String wrongAnswer
) {
}
