package com.example.flashcards.model;

public record AiSentenceResponse(String de, String en, boolean available) {
    public static AiSentenceResponse of(String de, String en) {
        return new AiSentenceResponse(de, en, true);
    }
    public static AiSentenceResponse unavailable() {
        return new AiSentenceResponse(null, null, false);
    }
}
