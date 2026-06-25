package com.example.flashcards.controller;

import com.example.flashcards.service.SemanticSearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final SemanticSearchService searchService;

    public SearchController(SemanticSearchService searchService) {
        this.searchService = searchService;
    }

    /**
     * GET /api/search?q=reisen&k=8
     * Returns semantically similar cards to the query.
     */
    @GetMapping
    public List<Map<String, Object>> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "8") int k
    ) {
        k = Math.min(20, Math.max(1, k));
        return searchService.search(q, k);
    }

    /**
     * POST /api/search/index (admin-only)
     * Reindex all cards into pgvector.
     */
    @PostMapping("/index")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> reindex() {
        int count = searchService.indexAllCards();
        return ResponseEntity.ok(Map.of("indexed", count));
    }
}
