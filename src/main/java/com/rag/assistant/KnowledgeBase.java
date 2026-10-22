package com.rag.assistant;


import java.nio.file.Files;
import java.util.List;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;

public class KnowledgeBase {

    private final EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();
    private final InMemoryEmbeddingStore<TextSegment> store = new InMemoryEmbeddingStore<>();
    private final ContentRetriever retriever;
    private final int documentCount;

    public KnowledgeBase(AppConfig cfg) {
        if (!Files.isDirectory(cfg.dataDir())) {
            throw new IllegalStateException("Data folder not found: "
                    + cfg.dataDir().toAbsolutePath()
                    + " (the program must run from the project root)");
        }

        // 1. LOAD: read every .txt in data/ 
        List<Document> documents =
                FileSystemDocumentLoader.loadDocuments(cfg.dataDir(), new TextDocumentParser());
        if (documents.isEmpty()) {
            throw new IllegalStateException("No documents found in " + cfg.dataDir().toAbsolutePath());
        }
        this.documentCount = documents.size();

        // 2+3+4. SPLIT -> EMBED -> STORE, done in one call by the ingestor
        EmbeddingStoreIngestor.builder()
                .documentSplitter(DocumentSplitters.recursive(cfg.chunkSize(), cfg.chunkOverlap()))
                .embeddingModel(embeddingModel)
                .embeddingStore(store)
                .build()
                .ingest(documents);

        // The retriever is what AiServices will call before each question
        this.retriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(store)
                .embeddingModel(embeddingModel)
                .maxResults(cfg.maxResults())
                .minScore(cfg.minScore())
                .build();
    }

    public ContentRetriever retriever() {
        return retriever;
    }

    public int documentCount() {
        return documentCount;
    }

    // Direct search with score, used by the test and for debugging
    public List<EmbeddingMatch<TextSegment>> search(String question, int maxResults, double minScore) {
        Embedding questionEmbedding = embeddingModel.embed(question).content();
        EmbeddingSearchRequest request =
                new EmbeddingSearchRequest(questionEmbedding, maxResults, minScore, null);
        return store.search(request).matches();
    }
}