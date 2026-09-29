package com.sahib._1ragfundamentals.service;

import com.sahib._1ragfundamentals.exceptions.ModelCallException;
import com.sahib._1ragfundamentals.model.Citation;
import com.sahib._1ragfundamentals.model.RagAnswer;
import com.sahib._1ragfundamentals.model.SearchResult;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RagService {
    private final RetrievalService retrievalService;
    private final PromptBuilder promptBuilder;
    private final ChatClient chatClient;

    public RagService(RetrievalService retrievalService, PromptBuilder promptBuilder,
                      ChatClient.Builder chatClientBuilder) {
        this.retrievalService = retrievalService;
        this.promptBuilder = promptBuilder;
        this.chatClient = chatClientBuilder.build();
    }

    public RagAnswer answer(String question) {
        List<SearchResult> results = retrievalService.search(question, 3);
        String prompt = promptBuilder.build(question, results);
        String answer;
        try {
            answer = chatClient.prompt().user(prompt).call().content();
        } catch (RuntimeException e) {
            throw new ModelCallException("Answer generation failed", e);
        }
        if (answer == null || answer.isBlank()) {
            throw new ModelCallException("Answer generation returned empty content");
        }
        List<Citation> citations = results.stream()
                .map(result -> new Citation(result.chunk().source(),
                        result.chunk().chunkIndex(), result.score()))
                .toList();
        return new RagAnswer(answer, citations);
    }
}
