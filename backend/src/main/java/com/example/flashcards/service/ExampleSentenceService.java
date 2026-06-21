package com.example.flashcards.service;

import com.example.flashcards.entity.CardEntity;
import com.example.flashcards.entity.ChapterEntity;
import com.example.flashcards.entity.ExampleSentenceEntity;
import com.example.flashcards.repository.ExampleSentenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiFunction;

@Service
public class ExampleSentenceService {

    private final ExampleSentenceRepository repository;

    public ExampleSentenceService(ExampleSentenceRepository repository) {
        this.repository = repository;
    }

    // Templates take (CardEntity, lang) where lang = "de" or "en"
    @FunctionalInterface
    private interface SentenceGen extends BiFunction<CardEntity, String, String> {}

    private record Template(SentenceGen generate) {}

    // ── Article helpers ──

    private static String cap(String s) {
        return s == null || s.isEmpty() ? s : s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    /** Nominative article: der / die / das */
    private static String nom(CardEntity c) {
        return c.getArticle() != null ? c.getArticle() : "";
    }

    /** Accusative article: den / die / das */
    private static String akk(CardEntity c) {
        if (c.getArticle() == null) return "";
        return switch (c.getArticle()) {
            case "der" -> "den";
            case "die" -> "die";
            case "das" -> "das";
            default -> c.getArticle();
        };
    }

    /** Dative article: dem / der / dem */
    private static String dat(CardEntity c) {
        if (c.getArticle() == null) return "";
        return switch (c.getArticle()) {
            case "der" -> "dem";
            case "die" -> "der";
            case "das" -> "dem";
            default -> c.getArticle();
        };
    }

    /** Indefinite article: ein / eine / ein */
    private static String indef(CardEntity c) {
        if (c.getArticle() == null) return "ein";
        return "die".equals(c.getArticle()) ? "eine" : "ein";
    }

    // ── Noun templates (use correct articles) ──

    private static final List<Template> NOUN_TEMPLATES = List.of(
            new Template((c, lang) -> lang.equals("de")
                    ? cap(nom(c)) + " " + c.getWord() + " ist wirklich toll."
                    : "The " + c.getEnglish() + " is really great."),
            new Template((c, lang) -> lang.equals("de")
                    ? "Hast du " + akk(c) + " " + c.getWord() + " gesehen?"
                    : "Have you seen the " + c.getEnglish() + "?"),
            new Template((c, lang) -> lang.equals("de")
                    ? "Ich suche " + akk(c) + " " + c.getWord() + "."
                    : "I am looking for the " + c.getEnglish() + "."),
            new Template((c, lang) -> lang.equals("de")
                    ? "Kannst du mir " + akk(c) + " " + c.getWord() + " zeigen?"
                    : "Can you show me the " + c.getEnglish() + "?"),
            new Template((c, lang) -> lang.equals("de")
                    ? "Ich habe " + indef(c) + " " + c.getWord() + " gefunden."
                    : "I found a " + c.getEnglish() + "."),
            new Template((c, lang) -> lang.equals("de")
                    ? "Wir sprechen über " + akk(c) + " " + c.getWord() + "."
                    : "We are talking about the " + c.getEnglish() + "."),
            new Template((c, lang) -> lang.equals("de")
                    ? "Ich bin mit " + dat(c) + " " + c.getWord() + " zufrieden."
                    : "I am happy with the " + c.getEnglish() + "."),
            new Template((c, lang) -> lang.equals("de")
                    ? "Er hat " + akk(c) + " " + c.getWord() + " vergessen."
                    : "He forgot the " + c.getEnglish() + ".")
    );

    // ── Verb templates ──

    /** Strip leading "to " from English verb translations like "to book" → "book" */
    private static String verbEn(CardEntity c) {
        String en = c.getEnglish();
        return en.startsWith("to ") ? en.substring(3) : en;
    }

    private static final List<Template> VERB_TEMPLATES = List.of(
            new Template((c, lang) -> lang.equals("de")
                    ? "Ich möchte heute " + c.getWord() + "."
                    : "I would like to " + verbEn(c) + " today."),
            new Template((c, lang) -> lang.equals("de")
                    ? "Kannst du mir helfen zu " + c.getWord() + "?"
                    : "Can you help me " + verbEn(c) + "?"),
            new Template((c, lang) -> lang.equals("de")
                    ? "Es macht Spaß zu " + c.getWord() + "."
                    : "It is fun to " + verbEn(c) + "."),
            new Template((c, lang) -> lang.equals("de")
                    ? "Wann wollen wir " + c.getWord() + "?"
                    : "When do we want to " + verbEn(c) + "?"),
            new Template((c, lang) -> lang.equals("de")
                    ? "Man sollte regelmäßig " + c.getWord() + "."
                    : "One should " + verbEn(c) + " regularly."),
            new Template((c, lang) -> lang.equals("de")
                    ? "Ich habe keine Zeit zu " + c.getWord() + "."
                    : "I don't have time to " + verbEn(c) + "."),
            new Template((c, lang) -> lang.equals("de")
                    ? "Es ist wichtig zu " + c.getWord() + "."
                    : "It is important to " + verbEn(c) + "."),
            new Template((c, lang) -> lang.equals("de")
                    ? "Ich versuche zu " + c.getWord() + "."
                    : "I am trying to " + verbEn(c) + ".")
    );

    // ── Adjective templates ──

    private static final List<Template> ADJECTIVE_TEMPLATES = List.of(
            new Template((c, lang) -> lang.equals("de")
                    ? "Das Essen schmeckt sehr " + c.getWord() + "."
                    : "The food tastes very " + c.getEnglish() + "."),
            new Template((c, lang) -> lang.equals("de")
                    ? "Ich finde die Stadt wirklich " + c.getWord() + "."
                    : "I find the city really " + c.getEnglish() + "."),
            new Template((c, lang) -> lang.equals("de")
                    ? "Die Aufgabe war nicht " + c.getWord() + "."
                    : "The task was not " + c.getEnglish() + "."),
            new Template((c, lang) -> lang.equals("de")
                    ? "Er sieht heute " + c.getWord() + " aus."
                    : "He looks " + c.getEnglish() + " today."),
            new Template((c, lang) -> lang.equals("de")
                    ? "Das klingt " + c.getWord() + "."
                    : "That sounds " + c.getEnglish() + "."),
            new Template((c, lang) -> lang.equals("de")
                    ? "Alles war sehr " + c.getWord() + "."
                    : "Everything was very " + c.getEnglish() + ".")
    );

    // ── Adverb templates ──

    private static final List<Template> ADVERB_TEMPLATES = List.of(
            new Template((c, lang) -> lang.equals("de")
                    ? "Sie singt " + c.getWord() + "."
                    : "She sings " + c.getEnglish() + "."),
            new Template((c, lang) -> lang.equals("de")
                    ? "Das Kind läuft " + c.getWord() + "."
                    : "The child runs " + c.getEnglish() + "."),
            new Template((c, lang) -> lang.equals("de")
                    ? "Er hat " + c.getWord() + " geantwortet."
                    : "He answered " + c.getEnglish() + "."),
            new Template((c, lang) -> lang.equals("de")
                    ? "Ich komme " + c.getWord() + " zu spät."
                    : "I arrive late " + c.getEnglish() + "."),
            new Template((c, lang) -> lang.equals("de")
                    ? "Wir haben " + c.getWord() + " genug Zeit."
                    : "We have " + c.getEnglish() + " enough time."),
            new Template((c, lang) -> lang.equals("de")
                    ? "Das passiert " + c.getWord() + "."
                    : "That happens " + c.getEnglish() + ".")
    );

    // ── Preposition templates ──

    private static final List<Template> PREPOSITION_TEMPLATES = List.of(
            new Template((c, lang) -> lang.equals("de")
                    ? "Die Katze sitzt " + c.getWord() + " dem Stuhl."
                    : "The cat sits " + c.getEnglish() + " the chair."),
            new Template((c, lang) -> lang.equals("de")
                    ? "Das Geschenk ist " + c.getWord() + " dich."
                    : "The gift is " + c.getEnglish() + " you."),
            new Template((c, lang) -> lang.equals("de")
                    ? "Ich warte " + c.getWord() + " den Bus."
                    : "I am waiting " + c.getEnglish() + " the bus."),
            new Template((c, lang) -> lang.equals("de")
                    ? "Er steht " + c.getWord() + " der Tür."
                    : "He stands " + c.getEnglish() + " the door."),
            new Template((c, lang) -> lang.equals("de")
                    ? "Wir fahren " + c.getWord() + " Berlin."
                    : "We are driving " + c.getEnglish() + " Berlin.")
    );

    // ── Phrase / expression templates ──

    private static final List<Template> PHRASE_TEMPLATES = List.of(
            new Template((c, lang) -> lang.equals("de")
                    ? "Auf Deutsch sagt man \"" + c.getWord() + "\"."
                    : "In German, people say \"" + c.getEnglish() + "\"."),
            new Template((c, lang) -> lang.equals("de")
                    ? "Ich benutze oft den Ausdruck \"" + c.getWord() + "\"."
                    : "I often use the expression \"" + c.getEnglish() + "\"."),
            new Template((c, lang) -> lang.equals("de")
                    ? "Man hört oft \"" + c.getWord() + "\" im Alltag."
                    : "You often hear \"" + c.getEnglish() + "\" in everyday life."),
            new Template((c, lang) -> lang.equals("de")
                    ? "\"" + cap(c.getWord()) + "\" ist ein typisch deutscher Ausdruck."
                    : "\"" + cap(c.getEnglish()) + "\" is a typical German expression.")
    );

    // ── Fallback templates ──

    private static final List<Template> FALLBACK = List.of(
            new Template((c, lang) -> lang.equals("de")
                    ? "Ich lerne das Wort \"" + c.getWord() + "\"."
                    : "I am learning the word \"" + c.getEnglish() + "\"."),
            new Template((c, lang) -> lang.equals("de")
                    ? "Kennst du \"" + c.getWord() + "\"?"
                    : "Do you know \"" + c.getEnglish() + "\"?"),
            new Template((c, lang) -> lang.equals("de")
                    ? "\"" + cap(c.getWord()) + "\" ist ein wichtiges deutsches Wort."
                    : "\"" + cap(c.getEnglish()) + "\" is an important German word."),
            new Template((c, lang) -> lang.equals("de")
                    ? "Ich habe \"" + c.getWord() + "\" im Unterricht gelernt."
                    : "I learned \"" + c.getEnglish() + "\" in class.")
    );

    private static final Map<String, List<Template>> TEMPLATES = Map.of(
            "noun", NOUN_TEMPLATES,
            "verb", VERB_TEMPLATES,
            "adjective", ADJECTIVE_TEMPLATES,
            "adverb", ADVERB_TEMPLATES,
            "preposition", PREPOSITION_TEMPLATES,
            "phrase", PHRASE_TEMPLATES
    );

    @Transactional
    public List<ExampleSentenceEntity> generateForCard(CardEntity card) {
        List<Template> pool = TEMPLATES.getOrDefault(card.getType(), FALLBACK);
        if (pool.isEmpty()) pool = FALLBACK;

        List<Template> shuffled = new ArrayList<>(pool);
        int count = Math.min(2, shuffled.size());
        List<ExampleSentenceEntity> result = new ArrayList<>();
        ThreadLocalRandom rng = ThreadLocalRandom.current();

        for (int i = 0; i < count; i++) {
            int pick = rng.nextInt(shuffled.size());
            Template tpl = shuffled.remove(pick);

            ExampleSentenceEntity entity = new ExampleSentenceEntity();
            entity.setId(card.getId() + "-s" + (i + 1));
            entity.setCard(card);
            entity.setSentenceDe(tpl.generate().apply(card, "de"));
            entity.setSentenceEn(tpl.generate().apply(card, "en"));
            entity.setAutoGenerated(true);
            entity.setCreatedAt(Instant.now());
            result.add(entity);
        }

        card.getExampleSentences().addAll(result);
        return result;
    }

    @Transactional
    public void generateForChapter(ChapterEntity chapter) {
        for (CardEntity card : chapter.getCards()) {
            if (card.getExampleSentences().isEmpty()) {
                generateForCard(card);
            }
        }
    }

    @Transactional
    public ExampleSentenceEntity addSentence(String cardId, String sentenceDe, String sentenceEn,
                                              CardEntity card) {
        String id = cardId + "-s" + java.util.UUID.randomUUID().toString().substring(0, 8);
        ExampleSentenceEntity entity = new ExampleSentenceEntity();
        entity.setId(id);
        entity.setCard(card);
        entity.setSentenceDe(sentenceDe);
        entity.setSentenceEn(sentenceEn);
        entity.setAutoGenerated(false);
        entity.setCreatedAt(Instant.now());
        return repository.save(entity);
    }

    @Transactional
    public ExampleSentenceEntity updateSentence(String sentenceId, String sentenceDe, String sentenceEn) {
        ExampleSentenceEntity entity = repository.findById(sentenceId)
                .orElseThrow(() -> new NoSuchElementException("Sentence not found: " + sentenceId));
        entity.setSentenceDe(sentenceDe);
        entity.setSentenceEn(sentenceEn);
        entity.setAutoGenerated(false);
        return repository.save(entity);
    }

    @Transactional
    public void deleteSentence(String sentenceId) {
        ExampleSentenceEntity entity = repository.findById(sentenceId)
                .orElseThrow(() -> new NoSuchElementException("Sentence not found: " + sentenceId));
        entity.getCard().getExampleSentences().remove(entity);
        repository.delete(entity);
    }
}
