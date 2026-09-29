package com.sahib._1ragfundamentals.exceptions;

public class IndexNotReadyException extends RuntimeException {
    public IndexNotReadyException() {
        super("Document index is not ready");
    }
}
