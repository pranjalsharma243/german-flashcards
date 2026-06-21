package com.example.flashcards.repository;

import com.example.flashcards.entity.ChapterEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChapterRepository extends JpaRepository<ChapterEntity, String> {
}
