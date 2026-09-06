package com.aijobassistant.ai.shared.exception;

/**
 * Lanzada cuando ocurre una falla de I/O o procesamiento interno al manipular el documento.
 */
public class DocumentProcessingException extends RuntimeException {
    public DocumentProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
