package com.example.flashcards.model;

public record AuthResponse(
        String token,
        String username,
        String role
) {
}
