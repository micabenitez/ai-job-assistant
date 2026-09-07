package com.aijobassistant.ai.resume;

import com.aijobassistant.ai.resume.dto.ResumeResponseDto;
import com.aijobassistant.ai.shared.exception.InvalidDocumentException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResumeIngestionServiceTest {

    @Mock
    private PdfTextExtractor pdfTextExtractor;

    @Mock
    private ResumeRepository resumeRepository;

    private ResumeIngestionService ingestionService;

    @BeforeEach
    void setUp() {
        ingestionService = new ResumeIngestionService(pdfTextExtractor, resumeRepository);
    }

    @Test
    @DisplayName("Debe procesar y persistir el CV exitosamente cuando el archivo es válido")
    void shouldIngestResumeSuccessfully() {
        // Arrange
        String filename = "cv.pdf";
        byte[] bytes = new byte[]{0x25, 0x50, 0x44, 0x46};
        String extractedText = "Desarrollador Java con experiencia.";

        when(pdfTextExtractor.extractText(bytes)).thenReturn(extractedText);
        when(resumeRepository.save(any(Resume.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ResumeResponseDto response = ingestionService.ingestResume(filename, bytes);

        // Assert
        assertNotNull(response);
        assertNotNull(response.id());
        assertEquals(filename, response.originalFilename());
        assertEquals(extractedText.length(), response.rawTextLength());

        verify(pdfTextExtractor, times(1)).extractText(bytes);
        verify(resumeRepository, times(1)).save(any(Resume.class));
    }

    @Test
    @DisplayName("Debe lanzar InvalidDocumentException si el nombre del archivo es nulo o vacío")
    void shouldThrowWhenFilenameIsBlank() {
        assertThrows(InvalidDocumentException.class, () ->
                ingestionService.ingestResume("   ", new byte[]{1, 2, 3})
        );
        verifyNoInteractions(pdfTextExtractor);
        verifyNoInteractions(resumeRepository);
    }
}