package com.example.flashcards.model;

import java.util.List;

public record CardGenerateResponse(
        List<GeneratedCard> cards,
        String topic,
        String chapterId,
        int requested,
        int generated
) {}
