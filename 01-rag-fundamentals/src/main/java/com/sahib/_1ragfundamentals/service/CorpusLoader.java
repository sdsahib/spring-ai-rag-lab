package com.sahib._1ragfundamentals.service;

import com.sahib._1ragfundamentals.model.DocumentChunk;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Service
public class CorpusLoader {
    private static final int MAX_CHARS = 800;

    public List<DocumentChunk> load() throws IOException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath:corpus/*.md");
        if (resources.length == 0) {
            throw new IllegalStateException("No Markdown files found in classpath:corpus/");
        }
        Arrays.sort(resources, Comparator.comparing(Resource::getFilename));

        List<DocumentChunk> chunks = new ArrayList<>();
        for (Resource resource : resources) {
            String source = resource.getFilename();
            String content;
            try (var input = resource.getInputStream()) {
                content = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            }
            List<String> paragraphs = splitIntoParagraphs(content);
            if (paragraphs.isEmpty()) {
                throw new IllegalStateException("Empty corpus document: " + source);
            }
            List<String> packed = packParagraphs(paragraphs);
            for (int index = 0; index < packed.size(); index++) {
                chunks.add(new DocumentChunk(source + "#" + index, packed.get(index), source, index, null));
            }
        }
        return chunks;
    }

    List<String> splitIntoParagraphs(String text) {
        return Arrays.stream(text.replace("\r\n", "\n").replace('\r', '\n').split("\n\\s*\n"))
                .map(String::strip)
                .filter(paragraph -> !paragraph.isBlank())
                .toList();
    }

    List<String> packParagraphs(List<String> paragraphs) {
        List<String> packed = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String paragraph : paragraphs) {
            if (!current.isEmpty() && current.length() + 2 + paragraph.length() > MAX_CHARS) {
                packed.add(current.toString());
                current.setLength(0);
            }
            if (!current.isEmpty()) {
                current.append("\n\n");
            }
            current.append(paragraph);
        }
        if (!current.isEmpty()) {
            packed.add(current.toString());
        }
        return packed;
    }
}
