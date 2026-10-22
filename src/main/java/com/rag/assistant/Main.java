package com.rag.assistant;

import java.util.Scanner;

import dev.langchain4j.rag.content.Content;
import dev.langchain4j.service.Result;

public class Main {

    public static void main(String[] args) {
        AppConfig cfg = AppConfig.fromEnv();

        System.out.println("Indexing documents...");
        KnowledgeBase knowledgeBase = new KnowledgeBase(cfg);
        System.out.println(knowledgeBase.documentCount() + " documents indexed.");

        RagService rag = new RagService(cfg, knowledgeBase);
        System.out.println("FitZone assistant ready. Type 'exit' to quit.");

        try (Scanner in = new Scanner(System.in)) {
            while (true) {
                System.out.print("\n> ");
                if (!in.hasNextLine()) break;
                String question = in.nextLine().trim();
                if (question.equalsIgnoreCase("exit")) break;
                if (question.isEmpty()) continue;

                try {
                    Result<String> result = rag.ask(question);
                    System.out.println(result.content());
                    for (Content source : result.sources()) {
                        String file = source.textSegment().metadata().getString("file_name");
                        System.out.println("  [source: " + file + "]");
                    }
                } catch (Exception e) {
                    System.err.println("Error: " + e.getMessage());
                }
            }
        }
    }
}