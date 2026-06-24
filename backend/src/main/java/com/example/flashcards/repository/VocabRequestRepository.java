package com.example.flashcards.repository;

import com.example.flashcards.entity.VocabRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VocabRequestRepository extends JpaRepository<VocabRequestEntity, Long> {
    List<VocabRequestEntity> findByStatusOrderByCreatedAtDesc(String status);
    List<VocabRequestEntity> findBySubmittedByUsernameOrderByCreatedAtDesc(String username);
}
