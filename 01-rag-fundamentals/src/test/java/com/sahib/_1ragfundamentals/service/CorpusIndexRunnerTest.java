package com.sahib._1ragfundamentals.service;

import com.sahib._1ragfundamentals.model.DocumentChunk;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationArguments;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CorpusIndexRunnerTest {
    @Mock CorpusLoader loader;
    @Mock InMemoryEmbeddingIndex index;
    @Mock ApplicationArguments arguments;
    @InjectMocks CorpusIndexRunner runner;

    @Test
    void testRun_ValidCorpus_IndexesLoadedChunks() throws IOException {
        // Given
        List<DocumentChunk> chunks = List.of(new DocumentChunk("doc.md#0", "text", "doc.md", 0, null));
        when(loader.load()).thenReturn(chunks);
        when(index.chunks()).thenReturn(List.of(
                new DocumentChunk("doc.md#0", "text", "doc.md", 0, new float[]{1})));
        // When
        runner.run(arguments);
        // Then
        verify(index).index(chunks);
    }

    @Test
    void testRun_LoaderFails_PropagatesFailure() throws IOException {
        // Given
        when(loader.load()).thenThrow(new IOException("unreadable"));
        // When
        assertThrows(IOException.class, () -> runner.run(arguments));
        // Then
        org.mockito.Mockito.verifyNoInteractions(index);
    }

    @Test
    void testRun_EmbeddingFails_PropagatesFailure() throws IOException {
        // Given
        List<DocumentChunk> chunks = List.of(new DocumentChunk("doc.md#0", "text", "doc.md", 0, null));
        when(loader.load()).thenReturn(chunks);
        doThrow(new IllegalStateException("model offline")).when(index).index(chunks);
        // When
        assertThrows(IllegalStateException.class, () -> runner.run(arguments));
        // Then
        verify(index).index(chunks);
    }
}
