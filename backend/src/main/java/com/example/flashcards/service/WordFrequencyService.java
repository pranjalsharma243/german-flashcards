package com.example.flashcards.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

/**
 * Estimates CEFR difficulty of a German word based on a curated frequency tier list.
 * A1/A2 words are the most common ~2000 words; B1 is 2001-5000; B2+ is rarer.
 * This uses a compact approximation — a real implementation would use the
 * Wortschatz Leipzig or BNC frequency corpus.
 */
@Service
public class WordFrequencyService {

    // Top-tier A1 words (sample from most frequent German words)
    private static final Set<String> A1_WORDS = Set.of(
            "sein", "haben", "werden", "können", "müssen", "sollen", "wollen", "dürfen", "machen",
            "gehen", "kommen", "sagen", "sehen", "wissen", "geben", "nehmen", "stehen", "denken",
            "ein", "eine", "und", "ist", "ich", "du", "er", "sie", "es", "wir", "ihr", "man",
            "ja", "nein", "bitte", "danke", "hallo", "gut", "schlecht", "groß", "klein",
            "haus", "auto", "kind", "frau", "mann", "tag", "nacht", "essen", "trinken",
            "schule", "buch", "stadt", "land", "wasser", "brot", "milch", "tee", "kaffee",
            "mutter", "vater", "bruder", "schwester", "freund", "freundin",
            "eins", "zwei", "drei", "vier", "fünf", "sechs", "sieben", "acht", "neun", "zehn",
            "montag", "dienstag", "mittwoch", "donnerstag", "freitag", "samstag", "sonntag"
    );

    // A2 words (next tier, common but not basic)
    private static final Set<String> A2_WORDS = Set.of(
            "arbeiten", "leben", "lernen", "spielen", "lesen", "schreiben", "kaufen", "verkaufen",
            "fahren", "fliegen", "reisen", "schlafen", "aufstehen", "anrufen", "bezahlen",
            "hotel", "restaurant", "bahnhof", "flughafen", "krankenhaus", "apotheke", "markt",
            "straße", "platz", "brücke", "park", "garten", "zimmer", "küche", "bad", "schlafzimmer",
            "morgen", "abend", "heute", "gestern", "übermorgen", "woche", "monat", "jahr",
            "links", "rechts", "geradeaus", "oben", "unten", "hier", "dort", "nah", "weit",
            "heiß", "kalt", "warm", "schön", "neu", "alt", "lang", "kurz", "schnell", "langsam"
    );

    // B1 words — intermediate, expected to be known at B1 level
    private static final Set<String> B1_WORDS = Set.of(
            "erklären", "beschreiben", "diskutieren", "entscheiden", "entwickeln", "erhalten",
            "vergleichen", "verstehen", "verändern", "ermöglichen", "verbessern", "vorschlagen",
            "verwenden", "berichten", "bestehen", "beziehen", "bilden", "darstellen",
            "gelegenheit", "erfahrung", "entwicklung", "veranstaltung", "möglichkeit", "bedeutung",
            "gesellschaft", "wirtschaft", "umwelt", "gesundheit", "bildung", "kultur", "politik",
            "situation", "problem", "lösung", "frage", "antwort", "beispiel", "ergebnis",
            "verschiedene", "besonders", "jedoch", "obwohl", "während", "nachdem", "bevor",
            "außerdem", "deshalb", "trotzdem", "schließlich", "zunächst", "anschließend"
    );

    public enum CefrLevel { A1, A2, B1, B2_PLUS, UNKNOWN }

    private final Map<CefrLevel, Set<String>> TIERS = Map.of(
            CefrLevel.A1, A1_WORDS,
            CefrLevel.A2, A2_WORDS,
            CefrLevel.B1, B1_WORDS
    );

    /**
     * Estimates the CEFR difficulty of a word.
     * Strips articles, lowercases, and looks up against tier sets.
     */
    public CefrLevel estimate(String word) {
        if (word == null || word.isBlank()) return CefrLevel.UNKNOWN;
        String normalized = word.strip().toLowerCase()
                .replaceFirst("^(der |die |das |ein |eine )", "");
        for (var entry : TIERS.entrySet()) {
            if (entry.getValue().contains(normalized)) return entry.getKey();
        }
        // Heuristic: short common words are likely A1/A2
        if (normalized.length() <= 4) return CefrLevel.A2;
        // B2+ for rarer / longer words
        return CefrLevel.B2_PLUS;
    }

    public String estimateLabel(String word) {
        return switch (estimate(word)) {
            case A1 -> "A1";
            case A2 -> "A2";
            case B1 -> "B1";
            case B2_PLUS -> "B2+";
            case UNKNOWN -> "";
        };
    }
}
