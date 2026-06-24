package com.example.flashcards.service;

import com.example.flashcards.entity.AppUser;
import com.example.flashcards.entity.ChapterEntity;
import com.example.flashcards.entity.ProgressEntity;
import com.example.flashcards.model.Progress;
import com.example.flashcards.model.ProgressUpdate;
import com.example.flashcards.repository.ChapterRepository;
import com.example.flashcards.repository.ProgressRepository;
import com.example.flashcards.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

@Service
public class ProgressService {
    private final ProgressRepository progressRepository;
    private final UserRepository userRepository;
    private final ChapterRepository chapterRepository;

    public ProgressService(
            ProgressRepository progressRepository,
            UserRepository userRepository,
            ChapterRepository chapterRepository
    ) {
        this.progressRepository = progressRepository;
        this.userRepository = userRepository;
        this.chapterRepository = chapterRepository;
    }

    @Transactional(readOnly = true)
    public Progress getProgress(String username, String chapterId) {
        return progressRepository.findByUserUsernameIgnoreCaseAndChapterId(username, chapterId)
                .map(this::toDto)
                .orElseGet(() -> emptyProgress(chapterId));
    }

    @Transactional
    public Progress saveProgress(String username, ProgressUpdate update) {
        AppUser user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        ChapterEntity chapter = chapterRepository.findById(update.chapterId())
                .orElseThrow(() -> new NoSuchElementException("Chapter not found: " + update.chapterId()));

        ProgressEntity progress = progressRepository
                .findByUserUsernameIgnoreCaseAndChapterId(username, update.chapterId())
                .orElseGet(() -> {
                    ProgressEntity entity = new ProgressEntity();
                    entity.setUser(user);
                    entity.setChapter(chapter);
                    return entity;
                });

        progress.getKnownCardIds().clear();
        progress.getKnownCardIds().addAll(new LinkedHashSet<>(nullToEmpty(update.knownCardIds())));
        progress.getPracticeCardIds().clear();
        progress.getPracticeCardIds().addAll(new LinkedHashSet<>(nullToEmpty(update.practiceCardIds())));
        progress.setUpdatedAt(Instant.now());
        return toDto(progressRepository.save(progress));
    }

    private Progress toDto(ProgressEntity progress) {
        return new Progress(
                progress.getChapter().getId(),
                List.copyOf(progress.getKnownCardIds()),
                List.copyOf(progress.getPracticeCardIds()),
                progress.getUpdatedAt()
        );
    }

    private Progress emptyProgress(String chapterId) {
        return new Progress(chapterId, List.of(), List.of(), null);
    }

    private List<String> nullToEmpty(List<String> cardIds) {
        return cardIds == null ? List.of() : cardIds.stream().filter(Objects::nonNull).distinct().toList();
    }
}
