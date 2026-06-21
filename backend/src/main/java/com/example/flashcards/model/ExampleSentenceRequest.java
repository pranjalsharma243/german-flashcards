package com.example.flashcards.model;

import jakarta.validation.constraints.NotBlank;

public record ExampleSentenceRequest(@NotBlank String sentenceDe, @NotBlank String sentenceEn) {}
