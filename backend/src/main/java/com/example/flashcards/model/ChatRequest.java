package com.example.flashcards.model;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record ChatRequest(
        @NotEmpty List<ChatMessage> messages,
        String chapterContext
) {}
