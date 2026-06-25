package com.example.flashcards.repository;

import com.example.flashcards.entity.CardEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CardRepository extends JpaRepository<CardEntity, String> {
    List<CardEntity> findByChapterIdOrderByPosition(String chapterId);
}
