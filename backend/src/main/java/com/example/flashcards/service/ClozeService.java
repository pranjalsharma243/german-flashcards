package com.example.flashcards.service;

import com.example.flashcards.model.ClozeItem;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import com.example.flashcards.repository.CardRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClozeService {

    private static final Logger log = LoggerFactory.getLogger(ClozeService.class);
    private static final TypeReference<List<ClozeItem>> CLOZE_LIST_TYPE = new TypeReference<>() {};

    private final ChatClient chatClient;
    private final CardRepository cardRepo;
    private final ObjectMapper mapper;
    private final boolean enabled;

    public ClozeService(ChatClient.Builder chatClientBuilder,
                        CardRepository cardRepo,
                        ObjectMapper mapper,
                        @Value("${spring.ai.openai.api-key:disabled}") String apiKey) {
        this.chatClient = chatClientBuilder
                .defaultSystem("You are a German language teacher creating B1-level fill-in-the-blank exercises.")
                .build();
        this.cardRepo = cardRepo;
        this.mapper = mapper;
        this.enabled = !apiKey.isBlank() && !apiKey.equals("disabled");
    }

    /**
     * Generates cloze sentences for a batch of card IDs.
     * Cached per card set (order-independent via sorted join).
     */
    @Cacheable(value = "ai-cloze", key = "#cardIds.stream().sorted().collect(T(java.util.stream.Collectors).joining(','))")
    public List<ClozeItem> generateCloze(List<String> cardIds, int count) {
        if (!enabled) return List.of();

        // Load cards from DB
        var cards = cardRepo.findAllById(cardIds);
        if (cards.isEmpty()) return List.of();

        // Build the word list for the prompt
        String wordList = cards.stream()
                .limit(Math.min(count, 10))
                .map(c -> {
                    String article = (c.getArticle() != null && !c.getArticle().isBlank()) ? c.getArticle() + " " : "";
                    return String.format("- %s%s (EN: %s, card_id: %s)", article, c.getWord(), c.getEnglish(), c.getId());
                })
                .collect(Collectors.joining("\n"));

        try {
            String raw = chatClient.prompt()
                    .options(OpenAiChatOptions.builder().maxTokens(1500).build())
                    .user(u -> u.text("""
                            Create B1-level German fill-in-the-blank exercises for these words:
                            {words}

                            For each word create ONE exercise item. Return a JSON array where each element has:
                            - "cardId": the card_id from the word list above
                            - "sentenceDe": a natural German sentence using the word
                            - "sentenceEn": English translation of the sentence
                            - "blankedDe": the sentence with the target word replaced by "____"
                            - "answer": the exact form of the word as used in the sentence
                            - "distractors": array of exactly 3 wrong but plausible German words

                            Rules:
                            - B1 vocabulary level
                            - Sentences should give enough context to guess the missing word
                            - Distractors must be the same part of speech as the answer
                            - Return ONLY valid JSON array, no markdown, no explanation
                            """)
                            .param("words", wordList))
                    .call()
                    .content();

            if (raw == null) return List.of();
            String json = extractJsonArray(raw.trim());
            return mapper.readValue(json, CLOZE_LIST_TYPE);
        } catch (Exception e) {
            log.warn("Cloze generation failed: {}", e.getMessage());
            return List.of();
        }
    }

    private String extractJsonArray(String text) {
        int start = text.indexOf('[');
        int end = text.lastIndexOf(']');
        if (start >= 0 && end > start) return text.substring(start, end + 1);
        return "[]";
    }
}
