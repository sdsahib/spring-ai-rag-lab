package com.sahib._1ragfundamentals.exceptions;

public class ModelCallException extends RuntimeException {
    public ModelCallException(String message, Throwable cause) {
        super(message, cause);
    }

    public ModelCallException(String message) {
        super(message);
    }
}
