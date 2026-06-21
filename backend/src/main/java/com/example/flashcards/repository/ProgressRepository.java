package com.example.flashcards.repository;

import com.example.flashcards.entity.ProgressEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProgressRepository extends JpaRepository<ProgressEntity, Long> {
    Optional<ProgressEntity> findByUserUsernameIgnoreCaseAndChapterId(String username, String chapterId);
}
