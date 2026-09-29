package com.sahib._1ragfundamentals.service;

import com.sahib._1ragfundamentals.exceptions.ModelCallException;
import com.sahib._1ragfundamentals.model.DocumentChunk;
import com.sahib._1ragfundamentals.model.RagAnswer;
import com.sahib._1ragfundamentals.model.SearchResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RagServiceTest {
    @Mock RetrievalService retrievalService;
    @Mock PromptBuilder promptBuilder;
    @Mock ChatClient.Builder builder;
    private ChatClient client;
    private RagService service;

    @BeforeEach
    void setUp() {
        client = mock(ChatClient.class, RETURNS_DEEP_STUBS);
        when(builder.build()).thenReturn(client);
        service = new RagService(retrievalService, promptBuilder, builder);
    }

    @Test
    void testAnswer_ValidQuestion_ReturnsMetadataDerivedCitations() {
        // Given
        List<SearchResult> results = List.of(new SearchResult(
                new DocumentChunk("doc.md#2", "seven days", "doc.md", 2, new float[]{1}), 0.9));
        when(retrievalService.search("When?", 3)).thenReturn(results);
        when(promptBuilder.build("When?", results)).thenReturn("grounded prompt");
        when(client.prompt().user("grounded prompt").call().content())
                .thenReturn("Seven days [made-up.md#99].");
        // When
        RagAnswer answer = service.answer("When?");
        // Then
        assertEquals("Seven days [made-up.md#99].", answer.answer());
        assertEquals("doc.md", answer.citations().getFirst().source());
        assertEquals(2, answer.citations().getFirst().chunkIndex());
        assertEquals(0.9, answer.citations().getFirst().score());
    }

    @Test
    void testAnswer_ChatFails_ReportsModelError() {
        // Given
        when(retrievalService.search("When?", 3)).thenReturn(List.of());
        when(promptBuilder.build("When?", List.of())).thenReturn("prompt");
        when(client.prompt().user(anyString()).call().content())
                .thenThrow(new IllegalStateException("private details"));
        // When
        ModelCallException error = assertThrows(ModelCallException.class,
                () -> service.answer("When?"));
        // Then
        assertEquals("Answer generation failed", error.getMessage());
    }

    @Test
    void testAnswer_BlankModelResponse_ReportsModelError() {
        // Given
        when(retrievalService.search("When?", 3)).thenReturn(List.of());
        when(promptBuilder.build("When?", List.of())).thenReturn("prompt");
        when(client.prompt().user("prompt").call().content()).thenReturn(" ");
        // When
        ModelCallException error = assertThrows(ModelCallException.class,
                () -> service.answer("When?"));
        // Then
        assertEquals("Answer generation returned empty content", error.getMessage());
    }
}
