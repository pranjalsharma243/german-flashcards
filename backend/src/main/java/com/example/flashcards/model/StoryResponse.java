package com.example.flashcards.model;

import java.util.List;

public record StoryResponse(String story, String translation, List<String> wordsUsed) {
    public static StoryResponse unavailable() {
        return new StoryResponse("AI story generation is not available right now.", "", List.of());
    }
}
