package com.example.flashcards.service;

import com.example.flashcards.entity.AppUser;
import com.example.flashcards.exception.UserAlreadyExistsException;
import com.example.flashcards.model.AuthRequest;
import com.example.flashcards.model.AuthResponse;
import com.example.flashcards.repository.UserRepository;
import com.example.flashcards.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(AuthRequest request) {
        String username = normalizeUsername(request.username());
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new UserAlreadyExistsException(username);
        }

        String role = userRepository.count() == 0 ? "ADMIN" : "USER";

        AppUser user = new AppUser();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(role);
        userRepository.save(user);
        return responseFor(username, role);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(AuthRequest request) {
        String username = normalizeUsername(request.username());
        AppUser user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid username or password");
        }
        return responseFor(user.getUsername(), user.getRole());
    }

    private AuthResponse responseFor(String username, String role) {
        return new AuthResponse(jwtService.createToken(username, role), username, role);
    }

    private String normalizeUsername(String username) {
        return username.trim().toLowerCase();
    }
}
