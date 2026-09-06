package com.aijobassistant.ai.infrastructure.pdf;

import com.aijobassistant.ai.resume.PdfTextExtractor;
import com.aijobassistant.ai.shared.exception.DocumentProcessingException;
import com.aijobassistant.ai.shared.exception.InvalidDocumentException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class PdfBoxTextExtractor implements PdfTextExtractor {

    // Firma estándar de cabecera PDF: %PDF (en ASCII: 0x25, 0x50, 0x44, 0x46)
    private static final byte[] PDF_MAGIC_BYTES = new byte[]{0x25, 0x50, 0x44, 0x46};

    @Override
    public String extractText(byte[] pdfBytes) {
        validatePdfBytes(pdfBytes);

        // PDFBox 3.x utiliza Loader.loadPDF() para crear el PDDocument de forma segura
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            if (document.isEncrypted()) {
                throw new InvalidDocumentException("El documento PDF está protegido con contraseña y no puede ser procesado.");
            }

            PDFTextStripper textStripper = new PDFTextStripper();
            textStripper.setSortByPosition(true); // Mantiene el orden de lectura visual

            String rawText = textStripper.getText(document);
            String normalizedText = sanitizeText(rawText);

            if (normalizedText.isBlank()) {
                throw new InvalidDocumentException("El archivo PDF no contiene texto digital legible (puede ser una imagen escaneada o estar vacío).");
            }

            return normalizedText;

        } catch (InvalidDocumentException e) {
            throw e;
        } catch (IOException e) {
            throw new DocumentProcessingException("Error de I/O al procesar el documento PDF.", e);
        } catch (Exception e) {
            throw new DocumentProcessingException("Falla inesperada durante la lectura del PDF: " + e.getMessage(), e);
        }
    }

    private void validatePdfBytes(byte[] bytes) {
        if (bytes == null || bytes.length < 4) {
            throw new InvalidDocumentException("El archivo proporcionado está vacío o es demasiado pequeño para ser un PDF válido.");
        }

        for (int i = 0; i < PDF_MAGIC_BYTES.length; i++) {
            if (bytes[i] != PDF_MAGIC_BYTES[i]) {
                throw new InvalidDocumentException("El archivo no tiene una cabecera PDF válida (firma binaria incorrecta).");
            }
        }
    }

    private String sanitizeText(String text) {
        if (text == null) {
            return "";
        }
        // Normaliza saltos de línea Windows/Mac a formato estándar Unix y elimina espacios redundantes continuos
        return text.replace("\r\n", "\n")
                .replace("\r", "\n")
                .replaceAll("[ \t]+", " ")
                .strip();
    }
}