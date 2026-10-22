package com.rag.assistant;

import java.nio.file.Path;

public record AppConfig(
        String apiKey,
        String modelName,
        Path dataDir,
        int chunkSize,
        int chunkOverlap,
        int maxResults,
        double minScore) {

    public static AppConfig fromEnv() {
        String apiKey = requireEnv("GEMINI_API_KEY");
        String modelName = requireEnv("GEMINI_MODEL");
        // Tunable without recompiling: see "Troubleshooting" if answers are always "I don't know"
        double minScore = Double.parseDouble(System.getenv().getOrDefault("RAG_MIN_SCORE", "0.5"));
        return new AppConfig(apiKey, modelName, Path.of("data"), 500, 80, 3, minScore);
    }

    private static String requireEnv(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing environment variable: " + name);
        }
        return value;
    }
}