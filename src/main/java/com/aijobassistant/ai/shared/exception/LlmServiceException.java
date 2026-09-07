package com.aijobassistant.ai.shared.exception;

/**
 * Lanzada cuando ocurre un error en la comunicación, timeout o procesamiento con el proveedor de IA.
 */
public class LlmServiceException extends RuntimeException {

    public LlmServiceException(String message) {
        super(message);
    }

    public LlmServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}