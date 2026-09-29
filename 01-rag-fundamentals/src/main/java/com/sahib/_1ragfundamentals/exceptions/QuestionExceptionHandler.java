package com.sahib._1ragfundamentals.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class QuestionExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> badQuestion(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(new ErrorResponse(exception.getMessage()));
    }

    @ExceptionHandler(IndexNotReadyException.class)
    public ResponseEntity<ErrorResponse> indexUnavailable(IndexNotReadyException exception) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ErrorResponse(exception.getMessage()));
    }

    @ExceptionHandler(ModelCallException.class)
    public ResponseEntity<ErrorResponse> modelUnavailable(ModelCallException exception) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(new ErrorResponse("Model service is unavailable"));
    }

    public record ErrorResponse(String error) {
    }
}
