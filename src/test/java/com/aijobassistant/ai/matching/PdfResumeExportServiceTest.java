package com.aijobassistant.ai.matching;

import com.aijobassistant.ai.infrastructure.pdf.PdfBoxResumeGenerator;
import com.aijobassistant.ai.joboffer.JobOffer;
import com.aijobassistant.ai.resume.Resume;
import com.aijobassistant.ai.resume.model.StructuredResumeData;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PdfResumeExportServiceTest {

    @Mock
    private AdaptedResumeRepository adaptedResumeRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final PdfBoxResumeGenerator pdfBoxResumeGenerator = new PdfBoxResumeGenerator();

    private PdfResumeExportService pdfResumeExportService;

    @BeforeEach
    void setUp() {
        pdfResumeExportService = new PdfResumeExportService(
                adaptedResumeRepository,
                pdfBoxResumeGenerator,
                objectMapper
        );
    }

    @Test
    @DisplayName("Debe exportar el CV adaptado a un arreglo de bytes de PDF válido")
    void shouldExportAdaptedResumeToValidPdfBytes() throws Exception {
        UUID adaptedResumeId = UUID.randomUUID();

        StructuredResumeData structuredData = new StructuredResumeData(
                "Micaela Benitez",
                "Moreno, Buenos Aires | mabenittez@gmail.com",
                "Resumen profesional adaptado",
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                List.of("Java", "Spring Boot"),
                List.of("Inglés (B1)")
        );

        String adaptedContentJson = objectMapper.writeValueAsString(structuredData);

        Resume resume = new Resume("cv.pdf", "Texto original");
        JobOffer jobOffer = new JobOffer("Java Dev", "Tech Co", "Descripcion de prueba...");
        MatchAnalysis matchAnalysis = new MatchAnalysis(resume, jobOffer, 90, "ALTO", "{}");

        AdaptedResume adaptedResume = new AdaptedResume(
                matchAnalysis,
                adaptedContentJson,
                "Notas de adaptación"
        );

        when(adaptedResumeRepository.findById(adaptedResumeId)).thenReturn(Optional.of(adaptedResume));

        byte[] pdfBytes = pdfResumeExportService.exportAdaptedResumeToPdf(adaptedResumeId);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
        String header = new String(pdfBytes, 0, Math.min(pdfBytes.length, 5));
        assertTrue(header.startsWith("%PDF"));
    }
}