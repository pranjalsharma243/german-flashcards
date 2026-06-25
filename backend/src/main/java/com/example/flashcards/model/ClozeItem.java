package com.example.flashcards.model;

import java.util.List;

public record ClozeItem(
    String cardId,
    String sentenceDe,      // full sentence in German
    String sentenceEn,      // English translation
    String blankedDe,       // sentence with target word replaced by "____"
    String answer,          // the target word (exact form used in sentence)
    List<String> distractors // 3 wrong options
) {}
