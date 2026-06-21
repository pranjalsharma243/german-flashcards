package com.example.flashcards.service;

import com.example.flashcards.entity.CardEntity;
import com.example.flashcards.entity.ChapterEntity;
import com.example.flashcards.model.Card;
import com.example.flashcards.model.Chapter;
import com.example.flashcards.repository.ChapterRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.List;

@Component
public class ChapterSeeder implements ApplicationRunner {
    private final ObjectMapper objectMapper;
    private final ChapterRepository chapterRepository;
    private final ExampleSentenceService exampleSentenceService;

    public ChapterSeeder(ObjectMapper objectMapper, ChapterRepository chapterRepository,
                         ExampleSentenceService exampleSentenceService) {
        this.objectMapper = objectMapper;
        this.chapterRepository = chapterRepository;
        this.exampleSentenceService = exampleSentenceService;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        if (chapterRepository.count() > 0) {
            return;
        }

        ClassPathResource resource = new ClassPathResource("data/chapters.json");
        try (InputStream inputStream = resource.getInputStream()) {
            ChapterFile chapterFile = objectMapper.readValue(inputStream, ChapterFile.class);
            for (Chapter chapter : chapterFile.chapters()) {
                ChapterEntity entity = chapterRepository.save(toEntity(chapter));
                exampleSentenceService.generateForChapter(entity);
            }
        }
    }

    private ChapterEntity toEntity(Chapter chapter) {
        ChapterEntity entity = new ChapterEntity();
        entity.setId(chapter.id());
        entity.setLevel(chapter.level());
        entity.setTitle(chapter.title());
        entity.setTheme(chapter.theme());

        for (int index = 0; index < chapter.cards().size(); index++) {
            Card card = chapter.cards().get(index);
            CardEntity cardEntity = new CardEntity();
            cardEntity.setId("%s-%03d".formatted(chapter.id(), index + 1));
            cardEntity.setChapter(entity);
            cardEntity.setPosition(index + 1);
            cardEntity.setType(card.type());
            cardEntity.setArticle(card.article());
            cardEntity.setWord(card.word());
            cardEntity.setEnglish(card.english());
            cardEntity.setHindi(card.hindi());
            entity.getCards().add(cardEntity);
        }

        return entity;
    }

    private record ChapterFile(List<Chapter> chapters) {
    }
}
