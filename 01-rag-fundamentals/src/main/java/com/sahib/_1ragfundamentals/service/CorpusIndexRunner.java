package com.sahib._1ragfundamentals.service;

import com.sahib._1ragfundamentals.model.DocumentChunk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Component
public class CorpusIndexRunner implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(CorpusIndexRunner.class);

    private final CorpusLoader corpusLoader;
    private final InMemoryEmbeddingIndex index;

    public CorpusIndexRunner(CorpusLoader corpusLoader, InMemoryEmbeddingIndex index) {
        this.corpusLoader = corpusLoader;
        this.index = index;
    }

    @Override
    public void run(ApplicationArguments args) throws IOException {
        Instant start = Instant.now();
        List<DocumentChunk> chunks = corpusLoader.load();
        index.index(chunks);
        log.info("Indexed {} documents, {} chunks, dimension {} in {} ms",
                chunks.stream().map(DocumentChunk::source).distinct().count(),
                chunks.size(), index.chunks().getFirst().embeddings().length,
                Duration.between(start, Instant.now()).toMillis());
    }
}
