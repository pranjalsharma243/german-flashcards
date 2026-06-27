package com.example.flashcards.service;

import com.example.flashcards.entity.AppUser;
import com.example.flashcards.model.AuthResponse;
import com.example.flashcards.repository.UserRepository;
import com.example.flashcards.security.JwtService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OAuth2GoogleService {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    public OAuth2GoogleService(UserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse findOrCreateUser(String email, String googleSub) {
        String normalizedEmail = email.trim().toLowerCase();

        AppUser user = userRepository.findByProviderId(googleSub)
                .orElseGet(() -> userRepository.findByUsernameIgnoreCase(normalizedEmail)
                        .map(existing -> {
                            existing.setProviderId(googleSub);
                            return userRepository.save(existing);
                        })
                        .orElseGet(() -> createGoogleUser(normalizedEmail, googleSub)));

        return new AuthResponse(
                jwtService.createToken(user.getUsername(), user.getRole()),
                user.getUsername(),
                user.getRole());
    }

    private AppUser createGoogleUser(String email, String googleSub) {
        String role = userRepository.count() == 0 ? "ADMIN" : "USER";
        AppUser user = new AppUser();
        user.setUsername(email);
        user.setPasswordHash("{google-oauth-only}");
        user.setProvider("GOOGLE");
        user.setProviderId(googleSub);
        user.setRole(role);
        return userRepository.save(user);
    }
}
