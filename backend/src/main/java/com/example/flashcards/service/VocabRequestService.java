package com.example.flashcards.service;

import com.example.flashcards.entity.AppUser;
import com.example.flashcards.entity.VocabRequestEntity;
import com.example.flashcards.model.Card;
import com.example.flashcards.model.Chapter;
import com.example.flashcards.model.VocabRequestDto;
import com.example.flashcards.repository.UserRepository;
import com.example.flashcards.repository.VocabRequestRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class VocabRequestService {

    private final VocabRequestRepository repo;
    private final UserRepository userRepository;
    private final AiService aiService;
    private final ChapterService chapterService;
    private final ObjectMapper objectMapper;

    public VocabRequestService(VocabRequestRepository repo, UserRepository userRepository,
                                AiService aiService, ChapterService chapterService,
                                ObjectMapper objectMapper) {
        this.repo = repo;
        this.userRepository = userRepository;
        this.aiService = aiService;
        this.chapterService = chapterService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public VocabRequestDto submitFromFile(MultipartFile file, String username) throws Exception {
        AppUser user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        String contentType = file.getContentType() != null ? file.getContentType() : "";
        List<Card> cards;
        String sourceType;

        if (contentType.startsWith("image/")) {
            sourceType = "IMAGE";
            cards = aiService.extractAndTranslateFromImage(file.getBytes(), contentType);
        } else if (contentType.equals("application/pdf")) {
            sourceType = "PDF";
            String text = extractPdfText(file);
            cards = aiService.extractAndTranslateFromText(text);
        } else {
            throw new IllegalArgumentException("Sirf image (PNG/JPG) ya PDF upload karo");
        }

        if (cards.isEmpty()) {
            throw new IllegalArgumentException("Is file mein koi German word nahi mila");
        }

        VocabRequestEntity entity = new VocabRequestEntity();
        entity.setSubmittedBy(user);
        entity.setSourceType(sourceType);
        entity.setStatus("PENDING");
        entity.setCardsJson(objectMapper.writeValueAsString(cards));
        repo.save(entity);

        return toDto(entity, cards);
    }

    @Transactional(readOnly = true)
    public List<VocabRequestDto> getPendingRequests() {
        return repo.findByStatusOrderByCreatedAtDesc("PENDING").stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<VocabRequestDto> getMyRequests(String username) {
        return repo.findBySubmittedByUsernameOrderByCreatedAtDesc(username).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public Chapter approveRequest(Long id, String chapterId) throws Exception {
        VocabRequestEntity entity = repo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Request not found: " + id));
        List<Card> cards = objectMapper.readValue(entity.getCardsJson(), new TypeReference<>() {});
        Chapter chapter = chapterService.appendCards(chapterId, cards);
        entity.setStatus("APPROVED");
        repo.save(entity);
        return chapter;
    }

    @Transactional
    public void rejectRequest(Long id) {
        VocabRequestEntity entity = repo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Request not found: " + id));
        entity.setStatus("REJECTED");
        repo.save(entity);
    }

    private VocabRequestDto toDto(VocabRequestEntity entity) {
        try {
            List<Card> cards = objectMapper.readValue(entity.getCardsJson(), new TypeReference<>() {});
            return toDto(entity, cards);
        } catch (Exception e) {
            return new VocabRequestDto(entity.getId(), entity.getSubmittedBy().getUsername(),
                    entity.getStatus(), entity.getSourceType(), List.of(), entity.getCreatedAt());
        }
    }

    private VocabRequestDto toDto(VocabRequestEntity entity, List<Card> cards) {
        return new VocabRequestDto(entity.getId(), entity.getSubmittedBy().getUsername(),
                entity.getStatus(), entity.getSourceType(), cards, entity.getCreatedAt());
    }

    private String extractPdfText(MultipartFile file) throws Exception {
        try (PDDocument doc = Loader.loadPDF(file.getBytes())) {
            return new PDFTextStripper().getText(doc);
        }
    }
}
