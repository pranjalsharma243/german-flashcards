package com.example.flashcards.exception;

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String email) {
        super("An account with email '" + email + "' already exists");
    }
}
