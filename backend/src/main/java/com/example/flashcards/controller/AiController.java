package com.example.flashcards.controller;

import com.example.flashcards.model.AiHintResponse;
import com.example.flashcards.model.AiSentenceResponse;
import com.example.flashcards.model.ChatRequest;
import com.example.flashcards.model.ChatResponse;
import com.example.flashcards.model.HintRequest;
import com.example.flashcards.model.StoryRequest;
import com.example.flashcards.model.StoryResponse;
import com.example.flashcards.repository.CardRepository;
import com.example.flashcards.service.AiService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
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

    private static final Logger log = LoggerFactory.getLogger(AiController.class);

    @PostMapping("/hint")
    public AiHintResponse hint(@Valid @RequestBody HintRequest request) {
        log.warn("AiController.hint() reached for word={}", request.word());
        return aiService.generateHint(
                request.cardId(), request.word(), request.article(),
                request.english(), request.hindi(), request.wrongAnswer()
        );
    }

    @PostMapping("/chat")
    public ChatResponse chat(@Valid @RequestBody ChatRequest request) {
        return aiService.chat(request.messages(), request.chapterContext());
    }

    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(@Valid @RequestBody ChatRequest request) {
        SseEmitter emitter = new SseEmitter(120_000L);
        var subscription = aiService.chatStream(request.messages(), request.chapterContext())
                .subscribe(
                        chunk -> {
                            try {
                                emitter.send(SseEmitter.event().data(chunk));
                            } catch (IOException e) {
                                emitter.completeWithError(e);
                            }
                        },
                        emitter::completeWithError,
                        () -> {
                            try {
                                emitter.send(SseEmitter.event().data("[DONE]"));
                                emitter.complete();
                            } catch (IOException e) {
                                emitter.completeWithError(e);
                            }
                        }
                );
        emitter.onCompletion(subscription::dispose);
        emitter.onTimeout(() -> { subscription.dispose(); emitter.complete(); });
        emitter.onError(e -> subscription.dispose());
        return emitter;
    }

    @PostMapping("/story")
    public StoryResponse story(@Valid @RequestBody StoryRequest request) {
        return aiService.generateStory(request.chapterTitle(), request.words());
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
