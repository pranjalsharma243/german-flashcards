package com.example.flashcards.service;

import com.example.flashcards.model.Card;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PdfParsingService {

    // Patterns for common vocabulary PDF formats:
    // Format 1: "der/die/das Word - English - Hindi"
    // Format 2: "Word (article) | English | Hindi"
    // Format 3: tab/comma separated columns
    private static final Pattern ARTICLE_WORD_PATTERN = Pattern.compile(
            "^\\s*(der|die|das|Der|Die|Das)\\s+(.+?)\\s*[-–|]\\s*(.+?)\\s*[-–|]\\s*(.+?)\\s*$"
    );

    private static final Pattern WORD_TRANSLATIONS_PATTERN = Pattern.compile(
            "^\\s*(.+?)\\s*[-–|,\\t]\\s*(.+?)\\s*[-–|,\\t]\\s*(.+?)\\s*$"
    );

    public List<Card> parsePdf(MultipartFile file) throws IOException {
        List<Card> cards = new ArrayList<>();

        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);

            String[] lines = text.split("\\r?\\n");
            int position = 1;

            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty() || line.length() < 3) {
                    continue;
                }

                // Skip header-like lines
                if (line.toLowerCase().contains("chapter") || line.toLowerCase().contains("vocabulary")
                        || line.toLowerCase().contains("page") || line.matches("^\\d+$")) {
                    continue;
                }

                // Try article + word pattern first
                Matcher articleMatcher = ARTICLE_WORD_PATTERN.matcher(line);
                if (articleMatcher.matches()) {
                    String article = articleMatcher.group(1).toLowerCase();
                    String word = articleMatcher.group(2).trim();
                    String english = articleMatcher.group(3).trim();
                    String hindi = articleMatcher.group(4).trim();

                    cards.add(new Card(
                            String.valueOf(position),
                            "noun",
                            article,
                            word,
                            english,
                            hindi,
                            null
                    ));
                    position++;
                    continue;
                }

                // Try generic word - translation - translation pattern
                Matcher wordMatcher = WORD_TRANSLATIONS_PATTERN.matcher(line);
                if (wordMatcher.matches()) {
                    String wordPart = wordMatcher.group(1).trim();
                    String english = wordMatcher.group(2).trim();
                    String hindi = wordMatcher.group(3).trim();

                    // Check if word part contains an article
                    String article = null;
                    String word = wordPart;
                    String type = "other";

                    String lower = wordPart.toLowerCase();
                    if (lower.startsWith("der ") || lower.startsWith("die ") || lower.startsWith("das ")) {
                        article = lower.substring(0, 3);
                        word = wordPart.substring(4).trim();
                        type = "noun";
                    }

                    cards.add(new Card(
                            String.valueOf(position),
                            type,
                            article,
                            word,
                            english,
                            hindi,
                            null
                    ));
                    position++;
                }
            }
        }

        return cards;
    }
}
