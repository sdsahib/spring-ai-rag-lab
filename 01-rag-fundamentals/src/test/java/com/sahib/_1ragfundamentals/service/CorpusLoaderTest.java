package com.sahib._1ragfundamentals.service;

import com.sahib._1ragfundamentals.model.DocumentChunk;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CorpusLoaderTest {
    private final CorpusLoader loader = new CorpusLoader();

    @Test
    void testLoad_CorpusFiles_ReturnsChunksWithMetadataAndPreservedContent() throws IOException {
        // Given
        Resource[] resources = new PathMatchingResourcePatternResolver()
                .getResources("classpath:corpus/*.md");

        // When
        List<DocumentChunk> chunks = loader.load();

        // Then
        Map<String, List<DocumentChunk>> bySource = chunks.stream()
                .collect(Collectors.groupingBy(DocumentChunk::source));
        assertEquals(10, bySource.size());
        assertEquals(chunks.stream().map(DocumentChunk::source).sorted().toList(),
                chunks.stream().map(DocumentChunk::source).toList());
        assertEquals(chunks.size(), chunks.stream().map(DocumentChunk::id).distinct().count());
        for (Resource resource : resources) {
            String source = resource.getFilename();
            List<DocumentChunk> fromFile = bySource.get(source);
            assertFalse(fromFile == null || fromFile.isEmpty());
            for (int index = 0; index < fromFile.size(); index++) {
                DocumentChunk chunk = fromFile.get(index);
                assertEquals(source + "#" + index, chunk.id());
                assertEquals(index, chunk.chunkIndex());
                assertFalse(chunk.content().isBlank());
                assertNull(chunk.embeddings());
            }
            String original;
            try (var input = resource.getInputStream()) {
                original = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            }
            String expected = String.join("\n\n", loader.splitIntoParagraphs(original));
            String actual = fromFile.stream().map(DocumentChunk::content)
                    .collect(Collectors.joining("\n\n"));
            assertEquals(expected, actual, source);
            for (DocumentChunk chunk : fromFile) {
                if (loader.splitIntoParagraphs(chunk.content()).size() > 1) {
                    assertTrue(chunk.content().length() <= 800, source);
                }
            }
        }
        assertEquals(Set.of("doc-01.md", "doc-02.md", "doc-03.md", "doc-04.md",
                "doc-05.md", "doc-06.md", "doc-07.md", "doc-08.md",
                "doc-09.md", "doc-10.md"), bySource.keySet());
    }

    @Test
    void testSplitIntoParagraphs_MixedLineEndings_PreservesLinesWithinParagraphs() {
        // Given
        String text = " Title\r\nfirst line\r\n\r\n second paragraph \r\r\n";

        // When
        List<String> paragraphs = loader.splitIntoParagraphs(text);

        // Then
        assertEquals(List.of("Title\nfirst line", "second paragraph"), paragraphs);
    }

    @Test
    void testPackParagraphs_EmptyInput_ReturnsNoChunks() {
        // Given
        List<String> paragraphs = List.of();

        // When
        List<String> chunks = loader.packParagraphs(paragraphs);

        // Then
        assertTrue(chunks.isEmpty());
    }

    @Test
    void testPackParagraphs_ThresholdAndOversizedParagraph_KeepsParagraphsWhole() {
        // Given
        String full = "a".repeat(800);
        String oversized = "b".repeat(801);

        // When
        List<String> chunks = loader.packParagraphs(List.of(full, "small", oversized, "tail"));

        // Then
        assertEquals(List.of(full, "small", oversized, "tail"), chunks);
        assertEquals(List.of("first\n\nsecond"),
                loader.packParagraphs(List.of("first", "second")));
    }
}
