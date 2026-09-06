package com.aijobassistant.ai.infrastructure.pdf;

import com.aijobassistant.ai.shared.exception.InvalidDocumentException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class PdfBoxTextExtractorTest {

    private PdfBoxTextExtractor extractor;

    @BeforeEach
    void setUp() {
        extractor = new PdfBoxTextExtractor();
    }

    @Test
    @DisplayName("Debe lanzar InvalidDocumentException si el array de bytes es nulo o menor a 4 bytes")
    void shouldThrowWhenBytesAreNullOrTooSmall() {
        assertThrows(InvalidDocumentException.class, () -> extractor.extractText(null));
        assertThrows(InvalidDocumentException.class, () -> extractor.extractText(new byte[]{0x25, 0x50}));
    }

    @Test
    @DisplayName("Debe lanzar InvalidDocumentException si los magic bytes no corresponden a un PDF")
    void shouldThrowWhenMagicBytesAreInvalid() {
        byte[] fakeExe = "MZThisIsNotAPdf".getBytes(StandardCharsets.UTF_8);

        InvalidDocumentException exception = assertThrows(
                InvalidDocumentException.class,
                () -> extractor.extractText(fakeExe)
        );
        assertTrue(exception.getMessage().contains("firma binaria incorrecta"));
    }

    @Test
    @DisplayName("Debe extraer texto correctamente de un PDF válido en memoria")
    void shouldExtractTextFromValidPdf() throws IOException {
        byte[] validPdf = createSamplePdf("Experiencia en Java 21 y Spring Boot.");

        String extractedText = extractor.extractText(validPdf);

        assertNotNull(extractedText);
        assertTrue(extractedText.contains("Experiencia en Java 21 y Spring Boot."));
    }

    @Test
    @DisplayName("Debe lanzar InvalidDocumentException si el PDF es válido pero no contiene texto (página en blanco)")
    void shouldThrowWhenPdfHasNoText() throws IOException {
        byte[] emptyPdf = createEmptyPdf();

        InvalidDocumentException exception = assertThrows(
                InvalidDocumentException.class,
                () -> extractor.extractText(emptyPdf)
        );
        assertTrue(exception.getMessage().contains("no contiene texto digital legible"));
    }

    // --- Métodos utilitarios de prueba para fabricar PDFs sintéticos en memoria ---

    private byte[] createSamplePdf(String content) throws IOException {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                contentStream.newLineAtOffset(50, 700);
                contentStream.showText(content);
                contentStream.endText();
            }

            document.save(baos);
            return baos.toByteArray();
        }
    }

    private byte[] createEmptyPdf() throws IOException {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            PDPage page = new PDPage();
            document.addPage(page);
            document.save(baos);
            return baos.toByteArray();
        }
    }
}