package com.example.flashcards.repository;

import com.example.flashcards.entity.ExampleSentenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExampleSentenceRepository extends JpaRepository<ExampleSentenceEntity, String> {}
