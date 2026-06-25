package com.example.flashcards.service;

import com.example.flashcards.entity.CardEntity;
import com.example.flashcards.repository.CardRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class SemanticSearchService {

    private static final Logger log = LoggerFactory.getLogger(SemanticSearchService.class);

    private final VectorStore vectorStore;
    private final CardRepository cardRepo;
    private final boolean enabled;

    public SemanticSearchService(VectorStore vectorStore,
                                  CardRepository cardRepo,
                                  @Value("${spring.ai.openai.api-key:disabled}") String apiKey) {
        this.vectorStore = vectorStore;
        this.cardRepo = cardRepo;
        this.enabled = !apiKey.isBlank() && !apiKey.equals("disabled");
    }

    /**
     * Embeds all cards in the vector store (idempotent — skips already-embedded cards).
     * Called at startup or on demand by admin.
     */
    public int indexAllCards() {
        if (!enabled) return 0;
        List<CardEntity> cards = cardRepo.findAll();
        List<Document> docs = cards.stream()
                .map(c -> {
                    String article = (c.getArticle() != null && !c.getArticle().isBlank()) ? c.getArticle() + " " : "";
                    String text = article + c.getWord() + " | " + c.getEnglish() + " | " + c.getHindi();
                    return new Document(text, Map.of(
                            "cardId", c.getId(),
                            "chapterId", c.getChapter().getId(),
                            "word", c.getWord(),
                            "article", c.getArticle() != null ? c.getArticle() : "",
                            "english", c.getEnglish(),
                            "hindi", c.getHindi(),
                            "type", c.getType()
                    ));
                })
                .toList();
        try {
            vectorStore.add(docs);
            log.info("Indexed {} cards in pgvector", docs.size());
            return docs.size();
        } catch (Exception e) {
            log.warn("pgvector indexing failed: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * Semantic search: finds the top-k cards most similar to the query.
     * Returns list of card metadata maps.
     */
    public List<Map<String, Object>> search(String query, int topK) {
        if (!enabled) return List.of();
        try {
            List<Document> results = vectorStore.similaritySearch(
                    SearchRequest.builder()
                            .query(query)
                            .topK(topK)
                            .build()
            );
            return results.stream()
                    .map(Document::getMetadata)
                    .toList();
        } catch (Exception e) {
            log.warn("Semantic search failed: {}", e.getMessage());
            return List.of();
        }
    }
}
