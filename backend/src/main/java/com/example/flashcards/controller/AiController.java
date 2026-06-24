package com.example.flashcards.controller;

import com.example.flashcards.model.AiHintResponse;
import com.example.flashcards.model.AiSentenceResponse;
import com.example.flashcards.model.HintRequest;
import com.example.flashcards.repository.CardRepository;
import com.example.flashcards.service.AiService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiService aiService;
    private final CardRepository cardRepository;

    public AiController(AiService aiService, CardRepository cardRepository) {
        this.aiService = aiService;
        this.cardRepository = cardRepository;
    }

    @PostMapping("/hint")
    public AiHintResponse hint(@Valid @RequestBody HintRequest request) {
        return aiService.generateHint(
                request.cardId(), request.word(), request.article(),
                request.english(), request.hindi(), request.wrongAnswer()
        );
    }

    @GetMapping("/sentence/{cardId}")
    public AiSentenceResponse sentence(@PathVariable String cardId) {
        var card = cardRepository.findById(cardId)
                .orElseThrow(() -> new NoSuchElementException("Card not found: " + cardId));
        return aiService.generateSentence(cardId, card.getWord(), card.getArticle(), card.getEnglish());
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(NoSuchElementException.class)
    public org.springframework.http.ResponseEntity<Void> handleNotFound() {
        return org.springframework.http.ResponseEntity.notFound().build();
    }
}
