package com.example.flashcards.model;

import java.util.List;

public record WritingFeedbackResponse(
    String correctedText,
    List<Correction> corrections,
    String cefrLevel,
    String overallFeedback
) {
    public record Correction(String original, String corrected, String explanation) {}
}
