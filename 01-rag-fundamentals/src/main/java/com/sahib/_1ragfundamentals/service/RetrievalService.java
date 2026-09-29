package com.sahib._1ragfundamentals.service;

import com.sahib._1ragfundamentals.exceptions.IndexNotReadyException;
import com.sahib._1ragfundamentals.exceptions.ModelCallException;
import com.sahib._1ragfundamentals.model.DocumentChunk;
import com.sahib._1ragfundamentals.model.SearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class RetrievalService {
    private static final Logger log = LoggerFactory.getLogger(RetrievalService.class);

    private final InMemoryEmbeddingIndex index;
    private final EmbeddingModel embeddingModel;

    public RetrievalService(InMemoryEmbeddingIndex index, EmbeddingModel embeddingModel) {
        this.index = index;
        this.embeddingModel = embeddingModel;
    }

    public List<SearchResult> search(String question, int topK) {
        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException("Question must not be blank");
        }
        if (topK < 1) {
            throw new IllegalArgumentException("topK must be positive");
        }
        List<DocumentChunk> chunks = index.chunks();
        if (chunks.isEmpty()) {
            throw new IndexNotReadyException();
        }
        float[] query;
        try {
            query = embeddingModel.embed(question);
        } catch (RuntimeException e) {
            throw new ModelCallException("Question embedding failed", e);
        }
        if (query == null || query.length == 0) {
            throw new ModelCallException("Question embedding returned an empty vector");
        }
        if (query.length != chunks.getFirst().embeddings().length) {
            throw new ModelCallException("Question embedding dimension differs from the index");
        }
        List<SearchResult> results = chunks.stream()
                .map(chunk -> new SearchResult(chunk,
                        CosineSimilarity.between(query, chunk.embeddings())))
                .sorted(Comparator.comparingDouble(SearchResult::score).reversed()
                        .thenComparing(result -> result.chunk().id()))
                .limit(topK)
                .toList();
        if (log.isDebugEnabled()) {
            for (int i = 0; i < results.size(); i++) {
                SearchResult result = results.get(i);
                log.debug("Retrieval rank {}: {} score={}", i + 1, result.chunk().id(), result.score());
            }
        }
        return results;
    }
}
