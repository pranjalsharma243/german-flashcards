package com.example.flashcards.model;

import com.example.flashcards.srs.FsrsEngine;

import java.time.Instant;

public record ReviewSubmitResponse(
    String cardId,
    int rating,
    int nextIntervalDays,
    Instant nextDue,
    double stability,
    double difficulty,
    int reps,
    int lapses,
    FsrsEngine.CardState state
) {}
