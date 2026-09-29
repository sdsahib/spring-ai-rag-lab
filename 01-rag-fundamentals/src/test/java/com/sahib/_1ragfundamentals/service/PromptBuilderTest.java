package com.sahib._1ragfundamentals.service;

import com.sahib._1ragfundamentals.model.DocumentChunk;
import com.sahib._1ragfundamentals.model.SearchResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PromptBuilderTest {
    @Test
    void testBuild_RetrievedChunks_ContainsRulesLabelsAndQuestion() {
        // Given
        List<SearchResult> results = List.of(
                new SearchResult(new DocumentChunk("a#0", "First fact", "a.md", 0, null), 0.8),
                new SearchResult(new DocumentChunk("b#1", "Second fact", "b.md", 1, null), 0.6));
        // When
        String prompt = new PromptBuilder().build("What happened?", results);
        // Then
        assertTrue(prompt.contains("I don't know based on the provided documents."));
        assertTrue(prompt.contains("[a.md#0]\nFirst fact"));
        assertTrue(prompt.contains("[b.md#1]\nSecond fact"));
        assertTrue(prompt.contains("Question:\nWhat happened?"));
    }
}
