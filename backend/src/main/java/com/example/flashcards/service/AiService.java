package com.example.flashcards.service;

import com.example.flashcards.model.AiHintResponse;
import com.example.flashcards.model.AiSentenceResponse;
import com.example.flashcards.model.Card;
import com.example.flashcards.model.ChatMessage;
import com.example.flashcards.model.ChatResponse;
import com.example.flashcards.model.StoryRequest;
import com.example.flashcards.model.StoryResponse;
import com.example.flashcards.model.WritingFeedbackResponse;
import com.example.flashcards.model.GeneratedCard;
import com.example.flashcards.model.CardGenerateResponse;

import java.util.ArrayList;
import java.util.Collections;
import com.example.flashcards.entity.CardAiContentEntity;
import com.example.flashcards.repository.CardAiContentRepository;
import com.example.flashcards.repository.CardRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.ai.chat.model.ChatModel;
import reactor.core.publisher.Flux;

@Service
public class AiService {

    private static final Logger log = LoggerFactory.getLogger(AiService.class);

    private final ChatClient chatClient;
    private final ChatModel chatModel;
    private final ObjectMapper objectMapper;
    private final CardAiContentRepository aiContentRepo;
    private final CardRepository cardRepo;
    private final boolean enabled;

    private static final String TUTOR_SYSTEM_PROMPT = """
            You are "Deutschi", a friendly and encouraging German language tutor chatbot.
            Your job is to help students learn German vocabulary, grammar, and conversation.

            Guidelines:
            - Be warm, encouraging, and patient
            - Keep responses concise and clear (2-4 sentences unless the topic needs more detail)
            - Use emojis occasionally to make learning fun
            - When explaining German words, always show: word, article (if noun), English meaning, and an example sentence
            - For grammar rules, give the rule + 1-2 short examples
            - If the student writes in Hindi, respond in Hindi + German explanations
            - If they write in English, respond in English + German explanations
            - You can quiz the user if they ask
            - Format German words in bold by wrapping them like **word**
            - Help with: vocabulary, der/die/das articles, cases (Nominativ/Akkusativ/Dativ), verb conjugation, sentence structure, pronunciation tips
            """;

    public AiService(
            ChatClient.Builder chatClientBuilder,
            ChatModel chatModel,
            ObjectMapper objectMapper,
            CardAiContentRepository aiContentRepo,
            CardRepository cardRepo,
            @Value("${spring.ai.openai.api-key:disabled}") String apiKey
    ) {
        this.chatModel = chatModel;
        this.objectMapper = objectMapper;
        this.aiContentRepo = aiContentRepo;
        this.cardRepo = cardRepo;
        this.enabled = !apiKey.isBlank() && !apiKey.equals("disabled");
        this.chatClient = chatClientBuilder
                .defaultSystem("You are a concise German language tutor helping students memorize vocabulary.")
                .build();
    }

    public ChatResponse chat(List<ChatMessage> messages, String chapterContext) {
        if (!enabled) return new ChatResponse("AI tutor is not available right now. Please check your API configuration.");
        try {
            List<Message> springMessages = buildTutorMessages(messages, chapterContext);
            String reply = chatModel.call(new Prompt(springMessages)).getResult().getOutput().getText();
            return new ChatResponse(reply != null ? reply.trim() : "Entschuldigung, I could not understand that. Please try again.");
        } catch (Exception e) {
            log.warn("Chat failed: {}", e.getMessage());
            return new ChatResponse("Sorry, something went wrong. Please try again!");
        }
    }

    public Flux<String> chatStream(List<ChatMessage> messages, String chapterContext) {
        if (!enabled) return Flux.just("AI tutor is not available right now.");
        try {
            List<Message> springMessages = buildTutorMessages(messages, chapterContext);
            return chatModel.stream(new Prompt(springMessages))
                    .mapNotNull(r -> r.getResult())
                    .mapNotNull(r -> r.getOutput())
                    .mapNotNull(r -> r.getText())
                    .filter(s -> !s.isEmpty())
                    .onErrorReturn("Sorry, something went wrong. Please try again!");
        } catch (Exception e) {
            log.warn("Chat stream failed: {}", e.getMessage());
            return Flux.just("Sorry, something went wrong. Please try again!");
        }
    }

    private List<Message> buildTutorMessages(List<ChatMessage> messages, String chapterContext) {
        String systemPrompt = chapterContext != null && !chapterContext.isBlank()
                ? TUTOR_SYSTEM_PROMPT + "\n\nCurrent study context: " + chapterContext
                : TUTOR_SYSTEM_PROMPT;
        List<Message> springMessages = new ArrayList<>();
        springMessages.add(new SystemMessage(systemPrompt));
        for (ChatMessage msg : messages) {
            if ("user".equals(msg.role())) {
                springMessages.add(new UserMessage(msg.content()));
            } else if ("assistant".equals(msg.role())) {
                springMessages.add(new AssistantMessage(msg.content()));
            }
        }
        return springMessages;
    }

    public AiHintResponse generateHint(String cardId, String word, String article,
                                       String english, String hindi, String wrongAnswer) {
        if (!enabled) return AiHintResponse.unavailable();
        // Check DB cache first (mnemonic is per-card, not per-wrong-answer)
        var cached = aiContentRepo.findById(cardId);
        if (cached.isPresent() && cached.get().getMnemonic() != null) {
            return AiHintResponse.of(cached.get().getMnemonic());
        }
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
            if (hint == null) return AiHintResponse.unavailable();
            String trimmed = hint.trim();
            // Persist to DB
            var content = cached.map(c -> c).orElseGet(() -> {
                var c = new CardAiContentEntity();
                c.setCardId(cardId);
                cardRepo.findById(cardId).ifPresent(c::setCard);
                return c;
            });
            content.setMnemonic(trimmed);
            content.setUpdatedAt(java.time.Instant.now());
            aiContentRepo.save(content);
            return AiHintResponse.of(trimmed);
        } catch (Exception e) {
            log.warn("AI hint generation failed for card {}: {}", cardId, e.getMessage());
            return AiHintResponse.unavailable();
        }
    }

    public AiSentenceResponse generateSentence(String cardId, String word, String article, String english) {
        if (!enabled) return AiSentenceResponse.unavailable();
        // Check DB cache first
        var cached = aiContentRepo.findById(cardId);
        if (cached.isPresent() && cached.get().getAiExampleDe() != null) {
            return AiSentenceResponse.of(cached.get().getAiExampleDe(), cached.get().getAiExampleEn());
        }
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
            String de = node.get("de").asText();
            String en = node.get("en").asText();
            // Persist to DB
            var content = cached.map(c -> c).orElseGet(() -> {
                var c = new CardAiContentEntity();
                c.setCardId(cardId);
                cardRepo.findById(cardId).ifPresent(c::setCard);
                return c;
            });
            content.setAiExampleDe(de);
            content.setAiExampleEn(en);
            content.setUpdatedAt(java.time.Instant.now());
            aiContentRepo.save(content);
            return AiSentenceResponse.of(de, en);
        } catch (Exception e) {
            log.warn("AI sentence generation failed for card {}: {}", cardId, e.getMessage());
            return AiSentenceResponse.unavailable();
        }
    }

    public StoryResponse generateStory(String chapterTitle, List<StoryRequest.StoryWord> words) {
        if (!enabled) return StoryResponse.unavailable();
        String wordList = words.stream()
                .limit(30)
                .map(w -> (w.article() != null && !w.article().isBlank() ? w.article() + " " : "") + w.word() + " (" + w.english() + ")")
                .collect(Collectors.joining(", "));
        try {
            String raw = chatClient.prompt()
                    .options(OpenAiChatOptions.builder().maxTokens(800).build())
                    .user(u -> u.text("""
                            Write a short German story (120-150 words) for a B1 learner.
                            Chapter theme: {theme}

                            Use at least 8 of these vocabulary words naturally in the story:
                            {words}

                            Return ONLY a raw JSON object (no markdown, no code block) with three keys:
                            "story" (the German story text), "translation" (English translation), "wordsUsed" (array of German words used from the list).
                            """)
                            .param("theme", chapterTitle)
                            .param("words", wordList))
                    .call()
                    .content();
            if (raw == null) return StoryResponse.unavailable();
            String json = extractJson(raw.trim());
            JsonNode node = objectMapper.readTree(json);
            List<String> wordsUsed = new ArrayList<>();
            node.path("wordsUsed").forEach(w -> wordsUsed.add(w.asText()));
            return new StoryResponse(
                    node.path("story").asText(""),
                    node.path("translation").asText(""),
                    wordsUsed
            );
        } catch (Exception e) {
            log.warn("Story generation failed: {}", e.getMessage());
            return StoryResponse.unavailable();
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
                    .options(OpenAiChatOptions.builder().maxTokens(2000).build())
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

    public List<Card> extractAndTranslateFromImage(byte[] imageBytes, String mimeType) {
        if (!enabled) throw new IllegalStateException("AI is not configured");
        try {
            String raw = chatClient.prompt()
                    .options(OpenAiChatOptions.builder().maxTokens(2000).build())
                    .user(u -> u.text("""
                            Look at this image. Find ALL German words or vocabulary items visible.
                            For each German word, return a JSON object with:
                            - "type": one of noun/verb/adjective/adverb/phrase
                            - "article": "der", "die", or "das" for nouns, null for others
                            - "word": the German word exactly as shown
                            - "english": concise English meaning
                            - "hindi": Hindi meaning in Devanagari script

                            Return ONLY a valid JSON array. No markdown, no code blocks, no explanation.
                            If no German words are found, return an empty array: []
                            """)
                            .media(MimeType.valueOf(mimeType), new ByteArrayResource(imageBytes)))
                    .call()
                    .content();
            if (raw == null) throw new RuntimeException("Empty response from AI");
            return parseCardsFromJson(extractJsonArray(raw.trim()));
        } catch (Exception e) {
            log.warn("AI image extraction failed: {}", e.getMessage());
            throw new RuntimeException("AI image extraction failed: " + e.getMessage());
        }
    }

    public List<Card> extractAndTranslateFromText(String pdfText) {
        if (!enabled) throw new IllegalStateException("AI is not configured");
        try {
            String raw = chatClient.prompt()
                    .options(OpenAiChatOptions.builder().maxTokens(2000).build())
                    .user(u -> u.text("""
                            The following text was extracted from a PDF. Find ALL German words or vocabulary items in it.
                            For each German word, return a JSON object with:
                            - "type": one of noun/verb/adjective/adverb/phrase
                            - "article": "der", "die", or "das" for nouns, null for others
                            - "word": the German word exactly as shown
                            - "english": concise English meaning
                            - "hindi": Hindi meaning in Devanagari script

                            PDF text:
                            {text}

                            Return ONLY a valid JSON array. No markdown, no code blocks, no explanation.
                            If no German words are found, return: []
                            """)
                            .param("text", pdfText.length() > 4000 ? pdfText.substring(0, 4000) : pdfText))
                    .call()
                    .content();
            if (raw == null) throw new RuntimeException("Empty response from AI");
            return parseCardsFromJson(extractJsonArray(raw.trim()));
        } catch (Exception e) {
            log.warn("AI text extraction failed: {}", e.getMessage());
            throw new RuntimeException("AI text extraction failed: " + e.getMessage());
        }
    }

    private List<Card> parseCardsFromJson(String json) throws Exception {
        try {
            return parseNodes(objectMapper.readTree(json));
        } catch (Exception e) {
            // JSON truncated mid-object — repair by cutting at last complete entry
            String repaired = repairTruncatedJsonArray(json);
            log.warn("AI returned truncated JSON, repaired to {} chars", repaired.length());
            return parseNodes(objectMapper.readTree(repaired));
        }
    }

    private List<Card> parseNodes(JsonNode arr) {
        List<Card> cards = new ArrayList<>();
        for (JsonNode node : arr) {
            String word = node.path("word").asText("").trim();
            if (word.isEmpty()) continue;
            String article = node.path("article").isNull() ? null : node.path("article").asText(null);
            cards.add(new Card(
                    null,
                    node.path("type").asText("noun"),
                    (article != null && article.isBlank()) ? null : article,
                    word,
                    node.path("english").asText(""),
                    node.path("hindi").asText(""),
                    null
            ));
        }
        return cards;
    }

    private String repairTruncatedJsonArray(String json) {
        // Find the last fully closed object "}" and close the array after it
        int lastClose = json.lastIndexOf('}');
        if (lastClose >= 0) {
            return json.substring(0, lastClose + 1) + "]";
        }
        return "[]";
    }

    public WritingFeedbackResponse analyzeWriting(String prompt, String userText) {
        if (!enabled) return new WritingFeedbackResponse(userText, List.of(), "?", "AI not available.");
        try {
            String raw = chatClient.prompt()
                    .options(OpenAiChatOptions.builder().maxTokens(1200).build())
                    .user(u -> u.text("""
                            You are a German B1 language teacher. Analyse this student writing and give structured feedback.

                            Task prompt: {prompt}

                            Student's text:
                            {text}

                            Return ONLY a valid JSON object with:
                            - "correctedText": the full corrected text
                            - "corrections": array of objects {{"original": "...", "corrected": "...", "explanation": "..."}} for each error (max 8)
                            - "cefrLevel": estimated CEFR level (A2/B1/B2 etc.)
                            - "overallFeedback": 2-3 sentence overall feedback in English

                            No markdown, no code block, just JSON.
                            """)
                            .param("prompt", prompt)
                            .param("text", userText))
                    .call()
                    .content();
            if (raw == null) return fallbackFeedback();
            String json = extractJson(raw.trim());
            JsonNode node = objectMapper.readTree(json);
            List<WritingFeedbackResponse.Correction> corrections = new ArrayList<>();
            node.path("corrections").forEach(c -> corrections.add(
                    new WritingFeedbackResponse.Correction(
                            c.path("original").asText(""),
                            c.path("corrected").asText(""),
                            c.path("explanation").asText(""))));
            return new WritingFeedbackResponse(
                    node.path("correctedText").asText(userText),
                    corrections,
                    node.path("cefrLevel").asText("B1"),
                    node.path("overallFeedback").asText("Good effort!"));
        } catch (Exception e) {
            log.warn("Writing analysis failed: {}", e.getMessage());
            return fallbackFeedback();
        }
    }

    private WritingFeedbackResponse fallbackFeedback() {
        return new WritingFeedbackResponse("", List.of(), "?", "Could not analyse your writing. Please try again.");
    }

    /**
     * Generates vocabulary cards for a given topic at B1 level.
     * Returns a list of GeneratedCard (not yet saved to DB — caller decides).
     */
    public CardGenerateResponse generateCards(String topic, String chapterId, int count) {
        if (!enabled) return new CardGenerateResponse(List.of(), topic, chapterId, count, 0);
        int safeCount = Math.min(Math.max(count, 3), 20);
        try {
            String raw = chatClient.prompt()
                    .options(OpenAiChatOptions.builder().maxTokens(1800).build())
                    .user(u -> u.text("""
                            Generate {count} German B1-level vocabulary words related to the topic: "{topic}".
                            For each word, provide:
                            - word: the German word (noun with capital letter if applicable)
                            - article: "der", "die", "das" for nouns, empty string for verbs/adjectives/adverbs
                            - english: concise English translation (max 4 words)
                            - hindi: concise Hindi translation (max 4 words)
                            - type: one of NOUN, VERB, ADJECTIVE, ADVERB, PHRASE

                            Return ONLY a valid JSON array, no markdown:
                            [{"word":"...","article":"...","english":"...","hindi":"...","type":"..."}]
                            """)
                            .param("count", String.valueOf(safeCount))
                            .param("topic", topic))
                    .call()
                    .content();
            if (raw == null) return new CardGenerateResponse(List.of(), topic, chapterId, count, 0);
            String json = extractJsonArray(raw.trim());
            JsonNode arr = objectMapper.readTree(json);
            List<GeneratedCard> cards = new ArrayList<>();
            for (JsonNode node : arr) {
                cards.add(new GeneratedCard(
                        node.path("word").asText(""),
                        node.path("article").asText(""),
                        node.path("english").asText(""),
                        node.path("hindi").asText(""),
                        node.path("type").asText("NOUN")
                ));
            }
            return new CardGenerateResponse(cards, topic, chapterId, count, cards.size());
        } catch (Exception e) {
            log.warn("Card generation failed for topic '{}': {}", topic, e.getMessage());
            return new CardGenerateResponse(List.of(), topic, chapterId, count, 0);
        }
    }

    private String extractJsonArray(String text) {
        int start = text.indexOf('[');
        int end = text.lastIndexOf(']');
        if (start >= 0 && end > start) return text.substring(start, end + 1);
        // No closing bracket — try to repair
        if (start >= 0) return repairTruncatedJsonArray(text.substring(start));
        return "[]";
    }
}
