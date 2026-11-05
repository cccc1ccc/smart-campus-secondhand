package com.must.secondhand.service;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Lightweight text similarity used by the matching rule (REQ-5.1).
 * No external AI service: text is lower-cased, tokenised and compared against the buyer's
 * keywords. The score rewards how many of the buyer's keywords appear in the listing
 * (coverage) and keeps a smaller term for how focused the listing text is (precision).
 */
public final class TextSimilarity {

    private static final Set<String> STOPWORDS = Set.of(
            "the", "a", "an", "and", "or", "of", "to", "in", "on", "for", "with", "is", "are",
            "i", "im", "want", "wanted", "buy", "need", "needed", "looking", "used", "second",
            "hand", "secondhand", "please", "any", "some", "my", "me", "it", "this", "that");

    private static final double COVERAGE_WEIGHT = 0.75;
    private static final double PRECISION_WEIGHT = 0.25;

    private TextSimilarity() {
    }

    public static List<String> tokenize(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        String normalized = text.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9\\s]", " ");
        Set<String> tokens = new LinkedHashSet<>();
        Arrays.stream(normalized.split("\\s+"))
                .filter(token -> token.length() > 1)
                .filter(token -> !STOPWORDS.contains(token))
                .forEach(tokens::add);
        return List.copyOf(tokens);
    }

    /**
     * @param text     listing title and description
     * @param keywords the buyer's desired keywords
     * @return similarity in [0, 1]; 1 means every buyer keyword appears in the text equally often
     */
    public static double similarity(String text, String keywords) {
        List<String> textTokens = tokenize(text);
        List<String> keywordTokens = tokenize(keywords);
        if (textTokens.isEmpty() || keywordTokens.isEmpty()) {
            return 0d;
        }
        double matched = 0d;
        for (String keyword : keywordTokens) {
            double best = 0d;
            for (String token : textTokens) {
                if (token.equals(keyword)) {
                    best = 1d;
                    break;
                }
                // Half credit for a partial match, e.g. "algorithm" vs "algorithms".
                if (token.contains(keyword) || keyword.contains(token)) {
                    best = Math.max(best, 0.5d);
                }
            }
            matched += best;
        }
        double coverage = matched / keywordTokens.size();
        double precision = matched / textTokens.size();
        return Math.min(1d, COVERAGE_WEIGHT * coverage + PRECISION_WEIGHT * precision);
    }
}
