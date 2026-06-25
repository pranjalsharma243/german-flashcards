package com.example.flashcards.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ClozeRequest(
    @NotEmpty List<String> cardIds,
    int count
) {}
