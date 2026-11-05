package com.must.secondhand.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TextSimilarityTest {

    @Test
    @DisplayName("Identical text scores 1.0")
    void identicalText() {
        assertThat(TextSimilarity.similarity("24 inch monitor hdmi", "24 inch monitor hdmi")).isEqualTo(1.0);
    }

    @Test
    @DisplayName("Unrelated text scores 0")
    void unrelatedText() {
        assertThat(TextSimilarity.similarity("calculus textbook", "yoga mat")).isZero();
    }

    @Test
    @DisplayName("Partial token match lands between 0 and 1")
    void partialMatch() {
        double score = TextSimilarity.similarity(
                "Logitech MX Master 3 Wireless Mouse",
                "mouse wireless logitech");
        assertThat(score).isBetween(0.5, 1.0);
    }

    @Test
    @DisplayName("Stop words and punctuation are ignored")
    void stopWordsIgnored() {
        assertThat(TextSimilarity.tokenize("I want to buy the monitor, please!"))
                .containsExactly("monitor");
    }

    @Test
    @DisplayName("Singular and plural forms still match")
    void pluralTolerance() {
        double score = TextSimilarity.similarity(
                "Introduction to Algorithms 3rd Edition (CLRS)",
                "algorithm textbook clrs");
        assertThat(score).isGreaterThan(0.3);
    }

    @Test
    @DisplayName("Empty input is safe")
    void emptyInput() {
        assertThat(TextSimilarity.similarity(null, "monitor")).isZero();
        assertThat(TextSimilarity.similarity("", "")).isZero();
    }
}
