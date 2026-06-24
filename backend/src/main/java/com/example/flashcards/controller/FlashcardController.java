package com.example.flashcards.controller;

import com.example.flashcards.model.Chapter;
import com.example.flashcards.model.ChapterSummary;
import com.example.flashcards.model.Progress;
import com.example.flashcards.model.ProgressUpdate;
import com.example.flashcards.service.ChapterService;
import com.example.flashcards.service.ProgressService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api")
public class FlashcardController {
    private final ChapterService chapterService;
    private final ProgressService progressService;

    public FlashcardController(ChapterService chapterService, ProgressService progressService) {
        this.chapterService = chapterService;
        this.progressService = progressService;
    }

    @GetMapping("/chapters")
    public List<ChapterSummary> getChapters() {
        return chapterService.getSummaries();
    }

    @GetMapping("/chapters/{chapterId}")
    public Chapter getChapter(@PathVariable String chapterId) {
        return chapterService.getChapter(chapterId);
    }

    @GetMapping("/progress/{chapterId}")
    public Progress getProgress(@PathVariable String chapterId, Principal principal) {
        return progressService.getProgress(principal.getName(), chapterId);
    }

    @PutMapping("/progress/{chapterId}")
    public Progress saveProgress(
            @PathVariable String chapterId,
            @Valid @RequestBody ProgressUpdate update,
            Principal principal
    ) {
        if (!chapterId.equals(update.chapterId())) {
            throw new IllegalArgumentException("Path chapter id and body chapter id must match");
        }
        chapterService.getChapter(chapterId);
        return progressService.saveProgress(principal.getName(), update);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<String> handleMissingChapter(NoSuchElementException exception) {
        return ResponseEntity.notFound().build();
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleBadRequest(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(exception.getMessage());
    }
}
