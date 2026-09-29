package com.sahib._1ragfundamentals.service;

import com.sahib._1ragfundamentals.model.DocumentChunk;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.embedding.EmbeddingModel;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InMemoryEmbeddingIndexTest {
    @Mock EmbeddingModel embeddingModel;
    @InjectMocks InMemoryEmbeddingIndex index;

    @Test
    void testIndex_ValidChunks_PublishesEmbeddedSnapshot() {
        // Given
        DocumentChunk chunk = new DocumentChunk("a#0", "alpha", "a", 0, null);
        when(embeddingModel.embed("alpha")).thenReturn(new float[]{1, 2});
        // When
        index.index(List.of(chunk));
        // Then
        assertArrayEquals(new float[]{1, 2}, index.chunks().getFirst().embeddings());
        assertEquals("a#0", index.chunks().getFirst().id());
        assertThrows(UnsupportedOperationException.class, () -> index.chunks().clear());
    }

    @Test
    void testIndex_ModelFails_PreservesPreviousSnapshot() {
        // Given
        DocumentChunk first = new DocumentChunk("a#0", "alpha", "a", 0, null);
        when(embeddingModel.embed("alpha")).thenReturn(new float[]{1});
        index.index(List.of(first));
        when(embeddingModel.embed("broken")).thenThrow(new IllegalStateException("offline"));
        // When
        assertThrows(IllegalStateException.class, () -> index.index(List.of(
                first, new DocumentChunk("b#0", "broken", "b", 0, null))));
        // Then
        assertEquals(List.of("a#0"), index.chunks().stream().map(DocumentChunk::id).toList());
    }

    @Test
    void testIndex_EmptyCorpus_RejectsIt() {
        // Given
        List<DocumentChunk> chunks = List.of();
        // When
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> index.index(chunks));
        // Then
        assertEquals("Cannot index an empty corpus", error.getMessage());
    }

    @Test
    void testIndex_InconsistentDimensions_RejectsWithoutPublishing() {
        // Given
        when(embeddingModel.embed("alpha")).thenReturn(new float[]{1});
        when(embeddingModel.embed("beta")).thenReturn(new float[]{1, 2});
        List<DocumentChunk> chunks = List.of(
                new DocumentChunk("a#0", "alpha", "a", 0, null),
                new DocumentChunk("b#0", "beta", "b", 0, null));
        // When
        assertThrows(IllegalStateException.class, () -> index.index(chunks));
        // Then
        assertEquals(List.of(), index.chunks());
    }
}
