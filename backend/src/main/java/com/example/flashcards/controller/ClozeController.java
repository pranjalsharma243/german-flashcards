package com.example.flashcards.controller;

import com.example.flashcards.model.ClozeItem;
import com.example.flashcards.model.ClozeRequest;
import com.example.flashcards.service.ClozeService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cloze")
public class ClozeController {

    private final ClozeService clozeService;

    public ClozeController(ClozeService clozeService) {
        this.clozeService = clozeService;
    }

    /**
     * POST /api/cloze/generate
     * Body: { "cardIds": ["id1","id2",...], "count": 5 }
     * Returns fill-in-the-blank exercises for the given cards.
     */
    @PostMapping("/generate")
    public List<ClozeItem> generate(@Valid @RequestBody ClozeRequest request) {
        int count = Math.min(10, Math.max(1, request.count() > 0 ? request.count() : 5));
        return clozeService.generateCloze(request.cardIds(), count);
    }
}
