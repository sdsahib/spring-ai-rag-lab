package com.sahib._1ragfundamentals.service;

import com.sahib._1ragfundamentals.exceptions.IndexNotReadyException;
import com.sahib._1ragfundamentals.exceptions.ModelCallException;
import com.sahib._1ragfundamentals.model.DocumentChunk;
import com.sahib._1ragfundamentals.model.SearchResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.embedding.EmbeddingModel;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RetrievalServiceTest {
    @Mock InMemoryEmbeddingIndex index;
    @Mock EmbeddingModel embeddingModel;
    @InjectMocks RetrievalService service;

    @Test
    void testSearch_RankedChunks_RespectsTopK() {
        // Given
        when(index.chunks()).thenReturn(List.of(
                chunk("low", new float[]{-1, 0}),
                chunk("high", new float[]{1, 0}),
                chunk("medium", new float[]{1, 1})));
        when(embeddingModel.embed("question")).thenReturn(new float[]{1, 0});
        // When
        List<SearchResult> results = service.search("question", 2);
        // Then
        assertEquals(List.of("high", "medium"),
                results.stream().map(result -> result.chunk().id()).toList());
        assertEquals(1, results.getFirst().score(), 0.000001);
    }

    @Test
    void testSearch_TopKLargerThanCorpus_ReturnsAvailableChunks() {
        // Given
        when(index.chunks()).thenReturn(List.of(chunk("only", new float[]{1, 0})));
        when(embeddingModel.embed("question")).thenReturn(new float[]{1, 0});
        // When
        List<SearchResult> results = service.search("question", 3);
        // Then
        assertEquals(1, results.size());
    }

    @Test
    void testSearch_BlankQuestion_RejectsWithoutModelCall() {
        // Given
        String question = " ";
        // When
        assertThrows(IllegalArgumentException.class, () -> service.search(question, 3));
        // Then
        verifyNoInteractions(index, embeddingModel);
    }

    @Test
    void testSearch_InvalidTopK_RejectsWithoutModelCall() {
        // Given
        int topK = 0;
        // When
        assertThrows(IllegalArgumentException.class, () -> service.search("question", topK));
        // Then
        verifyNoInteractions(index, embeddingModel);
    }

    @Test
    void testSearch_UninitializedIndex_RejectsWithoutModelCall() {
        // Given
        when(index.chunks()).thenReturn(List.of());
        // When
        assertThrows(IndexNotReadyException.class, () -> service.search("question", 3));
        // Then
        verifyNoInteractions(embeddingModel);
    }

    @Test
    void testSearch_EmbeddingFailure_ReportsModelError() {
        // Given
        when(index.chunks()).thenReturn(List.of(chunk("only", new float[]{1})));
        when(embeddingModel.embed("question")).thenThrow(new IllegalStateException("secret"));
        // When
        ModelCallException error = assertThrows(ModelCallException.class,
                () -> service.search("question", 3));
        // Then
        assertEquals("Question embedding failed", error.getMessage());
    }

    @Test
    void testSearch_DimensionMismatch_ReportsModelError() {
        // Given
        when(index.chunks()).thenReturn(List.of(chunk("only", new float[]{1, 0})));
        when(embeddingModel.embed("question")).thenReturn(new float[]{1});
        // When
        ModelCallException error = assertThrows(ModelCallException.class,
                () -> service.search("question", 3));
        // Then
        assertEquals("Question embedding dimension differs from the index", error.getMessage());
    }

    private DocumentChunk chunk(String id, float[] vector) {
        return new DocumentChunk(id, id, "doc.md", 0, vector);
    }
}
