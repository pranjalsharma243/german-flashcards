package com.example.flashcards.service;

import com.example.flashcards.model.AiHintResponse;
import com.example.flashcards.model.AiSentenceResponse;
import com.example.flashcards.model.Card;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AiService {

    private static final Logger log = LoggerFactory.getLogger(AiService.class);

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;
    private final boolean enabled;

    public AiService(
            ChatClient.Builder chatClientBuilder,
            ObjectMapper objectMapper,
            @Value("${spring.ai.openai.api-key:disabled}") String apiKey
    ) {
        this.objectMapper = objectMapper;
        this.enabled = !apiKey.isBlank() && !apiKey.equals("disabled");
        this.chatClient = chatClientBuilder
                .defaultSystem("You are a concise German language tutor helping students memorize vocabulary.")
                .build();
    }

    @Cacheable(value = "ai-hints", key = "#cardId + ':' + #wrongAnswer")
    public AiHintResponse generateHint(String cardId, String word, String article,
                                       String english, String hindi, String wrongAnswer) {
        if (!enabled) return AiHintResponse.unavailable();
        try {
            String articlePart = (article != null && !article.isBlank()) ? article + " " : "";
            String hindiPart = (hindi != null && !hindi.isBlank()) ? ", Hindi: " + hindi : "";
            String hint = chatClient.prompt()
                    .user(u -> u.text("""
                            German word: {word}
                            Meaning: {english}{hindi}
                            Student typed: "{wrong}"

                            Give ONE memory trick (max 2 sentences) to help remember "{word}".
                            Use wordplay, visual associations, or etymology. Be creative and fun.
                            Reply only in English. No intro, just the tip.
                            """)
                            .param("word", articlePart + word)
                            .param("english", english)
                            .param("hindi", hindiPart)
                            .param("wrong", wrongAnswer))
                    .call()
                    .content();
            return AiHintResponse.of(hint != null ? hint.trim() : "");
        } catch (Exception e) {
            log.warn("AI hint generation failed for card {}: {}", cardId, e.getMessage());
            return AiHintResponse.unavailable();
        }
    }

    @Cacheable(value = "ai-sentences", key = "#cardId")
    public AiSentenceResponse generateSentence(String cardId, String word, String article, String english) {
        if (!enabled) return AiSentenceResponse.unavailable();
        try {
            String articlePart = (article != null && !article.isBlank()) ? article + " " : "";
            String raw = chatClient.prompt()
                    .user(u -> u.text("""
                            Create one simple B1-level German sentence using the word "{word}" (meaning: {english}).
                            Return ONLY valid JSON with no markdown: {"de":"<German sentence>","en":"<English translation>"}
                            """)
                            .param("word", articlePart + word)
                            .param("english", english))
                    .call()
                    .content();
            if (raw == null) return AiSentenceResponse.unavailable();
            String json = extractJson(raw.trim());
            JsonNode node = objectMapper.readTree(json);
            return AiSentenceResponse.of(node.get("de").asText(), node.get("en").asText());
        } catch (Exception e) {
            log.warn("AI sentence generation failed for card {}: {}", cardId, e.getMessage());
            return AiSentenceResponse.unavailable();
        }
    }

    public List<Card> translateWords(List<String> words) {
        if (!enabled) throw new IllegalStateException("AI translation is not configured");
        String wordList = words.stream()
                .filter(w -> w != null && !w.isBlank())
                .map(w -> "- " + w.trim())
                .collect(Collectors.joining("\n"));
        try {
            String raw = chatClient.prompt()
                    .user(u -> u.text("""
                            Translate these German words to English and Hindi.
                            For each word return a JSON object with:
                            - "type": one of noun/verb/adjective/adverb/phrase
                            - "article": "der", "die", or "das" for nouns, null for others
                            - "word": the German word exactly as provided
                            - "english": concise English meaning
                            - "hindi": Hindi meaning in Devanagari script

                            German words:
                            {words}

                            Return ONLY a valid JSON array. No markdown, no code blocks, no explanation.
                            """)
                            .param("words", wordList))
                    .call()
                    .content();
            if (raw == null) throw new RuntimeException("Empty response from AI");
            String json = extractJsonArray(raw.trim());
            JsonNode arr = objectMapper.readTree(json);
            List<Card> cards = new ArrayList<>();
            for (JsonNode node : arr) {
                String article = node.path("article").isNull() ? null : node.path("article").asText(null);
                cards.add(new Card(
                        null,
                        node.path("type").asText("noun"),
                        (article != null && article.isBlank()) ? null : article,
                        node.path("word").asText(""),
                        node.path("english").asText(""),
                        node.path("hindi").asText(""),
                        null
                ));
            }
            return cards;
        } catch (Exception e) {
            log.warn("AI translation failed: {}", e.getMessage());
            throw new RuntimeException("AI translation failed: " + e.getMessage());
        }
    }

    private String extractJson(String text) {
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start) return text.substring(start, end + 1);
        return text;
    }

    private String extractJsonArray(String text) {
        int start = text.indexOf('[');
        int end = text.lastIndexOf(']');
        if (start >= 0 && end > start) return text.substring(start, end + 1);
        return text;
    }
}
