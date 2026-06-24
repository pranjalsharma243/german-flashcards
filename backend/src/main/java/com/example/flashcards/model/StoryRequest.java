package com.example.flashcards.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record StoryRequest(
        @NotBlank String chapterTitle,
        @NotEmpty List<StoryWord> words
) {
    public record StoryWord(String word, String article, String english) {}
}
