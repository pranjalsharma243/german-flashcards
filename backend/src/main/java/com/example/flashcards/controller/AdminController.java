package com.example.flashcards.controller;

import com.example.flashcards.model.Card;
import com.example.flashcards.model.Chapter;
import com.example.flashcards.model.ChapterSummary;
import com.example.flashcards.service.ChapterService;
import com.example.flashcards.service.PdfParsingService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final ChapterService chapterService;
    private final PdfParsingService pdfParsingService;
    private final ObjectMapper objectMapper;

    public AdminController(ChapterService chapterService, PdfParsingService pdfParsingService, ObjectMapper objectMapper) {
        this.chapterService = chapterService;
        this.pdfParsingService = pdfParsingService;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/chapters")
    public List<ChapterSummary> getChapters() {
        return chapterService.getSummaries();
    }

    @PostMapping("/chapters/json")
    public Chapter uploadJson(@RequestParam("file") MultipartFile file,
                              @RequestParam("level") String level,
                              @RequestParam("title") String title,
                              @RequestParam(value = "theme", defaultValue = "") String theme) {
        try {
            // Expect JSON to be an array of card objects: [{type, article, word, english, hindi}, ...]
            List<Map<String, String>> rawCards = objectMapper.readValue(file.getInputStream(),
                    new TypeReference<>() {});

            List<Card> cards = rawCards.stream().map(m -> new Card(
                    null,
                    m.getOrDefault("type", "noun"),
                    m.get("article"),
                    m.get("word"),
                    m.get("english"),
                    m.get("hindi"),
                    null
            )).toList();

            return chapterService.saveChapter(level, title, theme.isEmpty() ? title : theme, cards);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid JSON file: " + e.getMessage());
        }
    }

    @PostMapping("/chapters/pdf")
    public List<Card> parsePdf(@RequestParam("file") MultipartFile file) {
        try {
            return pdfParsingService.parsePdf(file);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse PDF: " + e.getMessage());
        }
    }

    @PostMapping("/chapters/save")
    public Chapter saveChapter(@RequestBody SaveChapterRequest request) {
        return chapterService.saveChapter(request.level(), request.title(), request.theme(), request.cards());
    }

    @DeleteMapping("/chapters/{chapterId}")
    public ResponseEntity<Void> deleteChapter(@PathVariable String chapterId) {
        chapterService.deleteChapter(chapterId);
        return ResponseEntity.noContent().build();
    }

    @org.springframework.web.bind.annotation.ExceptionHandler({IllegalArgumentException.class})
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Void> handleNotFound(NoSuchElementException e) {
        return ResponseEntity.notFound().build();
    }

    public record SaveChapterRequest(String level, String title, String theme, List<Card> cards) {}
}
