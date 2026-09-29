package com.sahib._1ragfundamentals.service;

import com.sahib._1ragfundamentals.model.DocumentChunk;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class InMemoryEmbeddingIndex {

    private final EmbeddingModel embeddingModel;
    private volatile List<DocumentChunk> indexedChunks = List.of();

    public InMemoryEmbeddingIndex(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    public void index(List<DocumentChunk> chunks) {
        if (chunks.isEmpty()) {
            throw new IllegalArgumentException("Cannot index an empty corpus");
        }
        List<DocumentChunk> completed = new ArrayList<>(chunks.size());
        int dimensions = -1;
        for (DocumentChunk chunk : chunks) {
            float[] embeddedChunk = embeddingModel.embed(chunk.content());
            if (embeddedChunk == null || embeddedChunk.length == 0) {
                throw new IllegalStateException("Embedding model returned an empty vector");
            }
            if (dimensions != -1 && embeddedChunk.length != dimensions) {
                throw new IllegalStateException("Inconsistent embedding dimensions");
            }
            dimensions = embeddedChunk.length;
            completed.add(chunk.withEmbeddings(embeddedChunk));
        }
        indexedChunks = List.copyOf(completed);
    }

    public List<DocumentChunk> chunks() {
        return indexedChunks;
    }
}
