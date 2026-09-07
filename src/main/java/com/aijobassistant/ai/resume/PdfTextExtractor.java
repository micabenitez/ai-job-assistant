package com.aijobassistant.ai.resume;

public interface PdfTextExtractor {

    /**
     * Extrae y normaliza el texto legible de un archivo PDF provisto en bytes.
     *
     * @param pdfBytes Contenido binario del archivo PDF.
     * @return Texto plano extraído y sanitizado.
     * @throws com.aijobassistant.ai.shared.exception.InvalidDocumentException si el documento no es un PDF válido o está protegido.
     * @throws com.aijobassistant.ai.shared.exception.DocumentProcessingException si ocurre un error inesperado durante la lectura.
     */
    String extractText(byte[] pdfBytes);
}