package com.example.flashcards.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record WritingFeedbackRequest(
    @NotBlank String prompt,
    @NotBlank @Size(min = 20, max = 2000) String userText
) {}
