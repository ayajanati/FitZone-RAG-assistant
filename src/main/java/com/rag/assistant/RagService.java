package com.rag.assistant;

import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.Result;

public class RagService {

    private final Assistant assistant;

    public RagService(AppConfig cfg, KnowledgeBase knowledgeBase) {
        ChatModel chatModel = GoogleAiGeminiChatModel.builder()
                .apiKey(cfg.apiKey())
                .modelName(cfg.modelName())
                .build();

        this.assistant = AiServices.builder(Assistant.class)
                .chatModel(chatModel)
                .contentRetriever(knowledgeBase.retriever())
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .build();
    }

    public Result<String> ask(String question) {
        return assistant.chat(question);
    }
}
