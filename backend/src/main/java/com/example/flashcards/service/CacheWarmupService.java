package com.example.flashcards.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class CacheWarmupService {

    private static final Logger log = LoggerFactory.getLogger(CacheWarmupService.class);

    private final ChapterService chapterService;

    public CacheWarmupService(ChapterService chapterService) {
        this.chapterService = chapterService;
    }

    // Evict all caches every hour, then re-warm them
    @Scheduled(fixedRateString = "3600000", initialDelayString = "60000")
    public void syncCache() {
        log.info("Cache sync started");
        evictAll();
        warmup();
        log.info("Cache sync complete");
    }

    @CacheEvict(value = {"chapters", "chapter-summaries"}, allEntries = true)
    public void evictAll() {}

    private void warmup() {
        try {
            var summaries = chapterService.getSummaries();
            summaries.forEach(s -> {
                try {
                    chapterService.getChapter(s.id());
                } catch (Exception e) {
                    log.warn("Failed to warm cache for chapter {}: {}", s.id(), e.getMessage());
                }
            });
            log.info("Warmed cache for {} chapters", summaries.size());
        } catch (Exception e) {
            log.error("Cache warmup failed: {}", e.getMessage());
        }
    }
}
