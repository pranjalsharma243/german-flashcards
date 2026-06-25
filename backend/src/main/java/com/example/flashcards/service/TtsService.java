package com.example.flashcards.service;

import com.example.flashcards.entity.TtsCacheEntity;
import com.example.flashcards.repository.CardRepository;
import com.example.flashcards.repository.TtsCacheRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.openai.OpenAiAudioSpeechModel;
import org.springframework.ai.openai.OpenAiAudioSpeechOptions;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.ai.openai.audio.speech.SpeechPrompt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.NoSuchElementException;

@Service
public class TtsService {

    private static final Logger log = LoggerFactory.getLogger(TtsService.class);

    private final TtsCacheRepository cacheRepo;
    private final CardRepository cardRepo;
    private final OpenAiAudioSpeechModel speechModel;
    private final boolean enabled;

    public TtsService(TtsCacheRepository cacheRepo,
                      CardRepository cardRepo,
                      OpenAiAudioSpeechModel speechModel,
                      @Value("${spring.ai.openai.api-key:disabled}") String apiKey) {
        this.cacheRepo = cacheRepo;
        this.cardRepo = cardRepo;
        this.speechModel = speechModel;
        this.enabled = !apiKey.isBlank() && !apiKey.equals("disabled");
    }

    /**
     * Returns MP3 bytes for the German pronunciation of a card.
     * Uses cache to avoid re-generating. Falls back to null if AI is unavailable.
     */
    @Transactional
    public byte[] getAudio(String cardId) {
        var card = cardRepo.findById(cardId)
                .orElseThrow(() -> new NoSuchElementException("Card not found: " + cardId));

        String text = (card.getArticle() != null && !card.getArticle().isBlank())
                ? card.getArticle() + " " + card.getWord()
                : card.getWord();

        String key = cacheKey(text);
        var cached = cacheRepo.findById(key);
        if (cached.isPresent()) {
            return cached.get().getAudioMp3();
        }

        if (!enabled) return null;

        try {
            byte[] mp3 = synthesize(text);
            TtsCacheEntity entity = new TtsCacheEntity();
            entity.setCacheKey(key);
            entity.setAudioMp3(mp3);
            cacheRepo.save(entity);
            return mp3;
        } catch (Exception e) {
            log.warn("TTS synthesis failed for '{}': {}", text, e.getMessage());
            return null;
        }
    }

    /** Synthesize arbitrary German text (for sentences etc). Not cached. */
    public byte[] synthesizeText(String text) {
        if (!enabled) return null;
        try {
            return synthesize(text);
        } catch (Exception e) {
            log.warn("TTS synthesis failed: {}", e.getMessage());
            return null;
        }
    }

    private byte[] synthesize(String text) {
        var options = OpenAiAudioSpeechOptions.builder()
                .model(OpenAiAudioApi.TtsModel.TTS_1.value)
                .voice(OpenAiAudioApi.SpeechRequest.Voice.NOVA.value)
                .responseFormat(OpenAiAudioApi.SpeechRequest.AudioResponseFormat.MP3)
                .speed(0.88f)
                .build();
        var response = speechModel.call(new SpeechPrompt(text, options));
        return response.getResult().getOutput();
    }

    private static String cacheKey(String text) {
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(("de-nova-" + text).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash).substring(0, 32);
        } catch (Exception e) {
            return Integer.toHexString(text.hashCode());
        }
    }
}
