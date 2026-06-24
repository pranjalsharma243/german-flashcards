package com.example.flashcards.model;

public record AiHintResponse(String hint, boolean available) {
    public static AiHintResponse of(String hint) {
        return new AiHintResponse(hint, true);
    }
    public static AiHintResponse unavailable() {
        return new AiHintResponse(null, false);
    }
}
