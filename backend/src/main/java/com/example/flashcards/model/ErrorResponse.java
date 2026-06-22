package com.example.flashcards.model;

import java.util.List;

public record ErrorResponse(String message, List<String> errors) {
    public static ErrorResponse of(String message) {
        return new ErrorResponse(message, List.of());
    }

    public static ErrorResponse of(String message, List<String> errors) {
        return new ErrorResponse(message, errors);
    }
}
