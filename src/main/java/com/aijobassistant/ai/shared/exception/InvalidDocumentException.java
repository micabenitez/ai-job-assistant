package com.aijobassistant.ai.shared.exception;

/**
 * Lanzada cuando el documento no cumple con los formatos, firmas o estados requeridos (ej. archivo vacío, corrupto o encriptado).
 */
public class InvalidDocumentException extends RuntimeException {
    public InvalidDocumentException(String message) {
        super(message);
    }
}