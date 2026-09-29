package com.sahib._1ragfundamentals.api;

import com.sahib._1ragfundamentals.exceptions.QuestionExceptionHandler;
import com.sahib._1ragfundamentals.model.Citation;
import com.sahib._1ragfundamentals.model.RagAnswer;
import com.sahib._1ragfundamentals.exceptions.IndexNotReadyException;
import com.sahib._1ragfundamentals.exceptions.ModelCallException;
import com.sahib._1ragfundamentals.service.RagService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class QuestionControllerTest {
    @Mock RagService ragService;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new QuestionController(ragService))
                .setControllerAdvice(new QuestionExceptionHandler()).build();
    }

    @Test
    void testAnswer_ValidQuestion_ReturnsAnswerAndCitations() throws Exception {
        // Given
        when(ragService.answer("When?")).thenReturn(new RagAnswer("Seven days",
                List.of(new Citation("doc-10.md", 0, 0.82))));
        // When / Then
        mvc.perform(post("/questions").contentType("application/json")
                        .content("{\"question\":\"When?\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").value("Seven days"))
                .andExpect(jsonPath("$.citations[0].source").value("doc-10.md"))
                .andExpect(jsonPath("$.citations[0].chunkIndex").value(0));
    }

    @Test
    void testAnswer_BlankQuestion_ReturnsBadRequest() throws Exception {
        // Given
        when(ragService.answer(" ")).thenThrow(new IllegalArgumentException("Question must not be blank"));
        // When / Then
        mvc.perform(post("/questions").contentType("application/json")
                        .content("{\"question\":\" \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Question must not be blank"));
    }

    @Test
    void testAnswer_IndexUnavailable_ReturnsServiceUnavailable() throws Exception {
        // Given
        when(ragService.answer("When?")).thenThrow(new IndexNotReadyException());
        // When / Then
        mvc.perform(post("/questions").contentType("application/json")
                        .content("{\"question\":\"When?\"}"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error").value("Document index is not ready"));
    }

    @Test
    void testAnswer_ModelFails_ReturnsSanitizedBadGateway() throws Exception {
        // Given
        when(ragService.answer("When?")).thenThrow(new ModelCallException("secret internal error"));
        // When / Then
        mvc.perform(post("/questions").contentType("application/json")
                        .content("{\"question\":\"When?\"}"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.error").value("Model service is unavailable"));
    }
}
