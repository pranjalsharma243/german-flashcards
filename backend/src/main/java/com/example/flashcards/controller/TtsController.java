package com.example.flashcards.controller;

import com.example.flashcards.repository.CardRepository;
import com.example.flashcards.service.TtsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/api/tts")
public class TtsController {

    private static final Logger log = LoggerFactory.getLogger(TtsController.class);

    private final TtsService ttsService;
    private final CardRepository cardRepository;

    public TtsController(TtsService ttsService, CardRepository cardRepository) {
        this.ttsService = ttsService;
        this.cardRepository = cardRepository;
    }

    /**
     * GET /api/tts/{cardId}
     * Returns MP3 audio for the German word on the card.
     * Cached aggressively (30 days) — audio is deterministic per card.
     */
    @GetMapping("/{cardId}")
    public ResponseEntity<byte[]> speak(@PathVariable String cardId) {
        byte[] audio = ttsService.getAudio(cardId);
        if (audio == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "audio/mpeg")
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .cacheControl(CacheControl.maxAge(30, TimeUnit.DAYS).immutable())
                .body(audio);
    }

    /**
     * POST /api/tts/warmup?chapterId= (admin-only)
     * Pre-generates TTS for all cards in a chapter (or all chapters if chapterId blank).
     */
    @PostMapping("/warmup")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> warmup(
            @RequestParam(required = false) String chapterId
    ) {
        var cards = (chapterId != null && !chapterId.isBlank())
                ? cardRepository.findByChapterIdOrderByPosition(chapterId)
                : cardRepository.findAll();
        var generated = new AtomicInteger();
        var cached = new AtomicInteger();
        var failed = new AtomicInteger();
        for (var card : cards) {
            try {
                byte[] audio = ttsService.getAudio(card.getId());
                if (audio != null) generated.incrementAndGet();
                else cached.incrementAndGet();
            } catch (Exception e) {
                log.warn("TTS warmup failed for card {}: {}", card.getId(), e.getMessage());
                failed.incrementAndGet();
            }
        }
        return ResponseEntity.ok(Map.of(
                "total", cards.size(),
                "generated", generated.get(),
                "cached", cached.get(),
                "failed", failed.get()
        ));
    }
}
