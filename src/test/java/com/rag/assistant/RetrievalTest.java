package com.rag.assistant;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;

class RetrievalTest {

    private static KnowledgeBase knowledgeBase;

    @BeforeAll
    static void setUp() {
        AppConfig cfg = new AppConfig("unused", "unused", Path.of("data"), 500, 80, 3, 0.0);
        knowledgeBase = new KnowledgeBase(cfg);
    }

    private static void print(String question, List<EmbeddingMatch<TextSegment>> matches) {
        System.out.println("\nQ: " + question);
        matches.forEach(m -> System.out.printf("  %.3f | %s%n",
                m.score(), m.embedded().text().replace("\n", " ")));
    }

    @Test
    void studentPriceIsRetrieved() {
        String q = "Combien coûte l'abonnement étudiant ?";
        var matches = knowledgeBase.search(q, 3, 0.0);
        print(q, matches);
        assertFalse(matches.isEmpty());
        assertTrue(matches.stream().anyMatch(m -> m.embedded().text().contains("22 €")));
    }

    @Test
    void sundayHoursAreRetrieved() {
        String q = "Le dimanche, la salle est ouverte de quelle heure à quelle heure ?";
        var matches = knowledgeBase.search(q, 3, 0.0);
        print(q, matches);
        assertTrue(matches.stream().anyMatch(m -> m.embedded().text().contains("9h00 à 13h00")));
    }
}