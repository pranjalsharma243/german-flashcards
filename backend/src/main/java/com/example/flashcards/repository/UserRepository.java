package com.example.flashcards.repository;

import com.example.flashcards.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByUsernameIgnoreCase(String username);

    Optional<AppUser> findByProviderId(String providerId);

    boolean existsByUsernameIgnoreCase(String username);
}
