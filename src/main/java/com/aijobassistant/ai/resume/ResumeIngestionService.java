package com.aijobassistant.ai.resume;

import com.aijobassistant.ai.resume.dto.ResumeResponseDto;
import com.aijobassistant.ai.shared.exception.InvalidDocumentException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ResumeIngestionService {

    private final PdfTextExtractor pdfTextExtractor;
    private final ResumeRepository resumeRepository;

    public ResumeIngestionService(PdfTextExtractor pdfTextExtractor, ResumeRepository resumeRepository) {
        this.pdfTextExtractor = pdfTextExtractor;
        this.resumeRepository = resumeRepository;
    }

    @Transactional
    public ResumeResponseDto ingestResume(String filename, byte[] fileBytes) {
        if (filename == null || filename.isBlank()) {
            throw new InvalidDocumentException("El nombre del archivo no puede estar vacío.");
        }

        String extractedText = pdfTextExtractor.extractText(fileBytes);
        Resume resume = new Resume(filename, extractedText);
        Resume savedResume = resumeRepository.save(resume);

        return ResumeResponseDto.fromEntity(savedResume);
    }
}