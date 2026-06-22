package com.example.flashcards.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthRequest(
        @NotBlank @Email @Size(max = 80) String username,
        @NotBlank @Size(min = 6, max = 120) String password
) {
}
