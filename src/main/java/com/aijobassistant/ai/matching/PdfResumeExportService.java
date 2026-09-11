package com.aijobassistant.ai.matching;

import com.aijobassistant.ai.infrastructure.pdf.PdfBoxResumeGenerator;
import com.aijobassistant.ai.resume.model.StructuredResumeData;
import com.aijobassistant.ai.shared.exception.DocumentProcessingException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class PdfResumeExportService {

    private final AdaptedResumeRepository adaptedResumeRepository;
    private final PdfBoxResumeGenerator pdfBoxResumeGenerator;
    private final ObjectMapper objectMapper;

    public PdfResumeExportService(
            AdaptedResumeRepository adaptedResumeRepository,
            PdfBoxResumeGenerator pdfBoxResumeGenerator,
            ObjectMapper objectMapper) {
        this.adaptedResumeRepository = adaptedResumeRepository;
        this.pdfBoxResumeGenerator = pdfBoxResumeGenerator;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public byte[] exportAdaptedResumeToPdf(UUID adaptedResumeId) {
        AdaptedResume adaptedResume = adaptedResumeRepository.findById(adaptedResumeId)
                .orElseThrow(() -> new DocumentProcessingException("No se encontró el CV adaptado con ID: " + adaptedResumeId, null));

        try {
            StructuredResumeData structuredData = objectMapper.readValue(
                    adaptedResume.getAdaptedContent(),
                    StructuredResumeData.class
            );
            return pdfBoxResumeGenerator.generatePdf(structuredData);
        } catch (JsonProcessingException e) {
            throw new DocumentProcessingException("No se pudo deserializar el contenido del CV adaptado para compilar el PDF.", e);
        }
    }
}