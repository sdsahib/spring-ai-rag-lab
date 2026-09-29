package com.sahib._1ragfundamentals.api;

import com.sahib._1ragfundamentals.model.RagAnswer;
import com.sahib._1ragfundamentals.service.RagService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/questions")
public class QuestionController {
    private final RagService ragService;

    public QuestionController(RagService ragService) {
        this.ragService = ragService;
    }

    @PostMapping
    public RagAnswer answer(@RequestBody QuestionRequest request) {
        return ragService.answer(request.question());
    }

    public record QuestionRequest(String question) {
    }
}
