package com.example.flashcards.model;

import java.util.List;

public record Card(
        String id,
        String type,
        String article,
        String word,
        String english,
        String hindi,
        List<ExampleSentence> exampleSentences
) {
}
