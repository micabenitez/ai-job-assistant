package com.aijobassistant.ai.resume;

import com.aijobassistant.ai.resume.dto.ResumeResponseDto;
import com.aijobassistant.ai.shared.exception.InvalidDocumentException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/resumes")
public class ResumeController {

    private final ResumeIngestionService resumeIngestionService;

    public ResumeController(ResumeIngestionService resumeIngestionService) {
        this.resumeIngestionService = resumeIngestionService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResumeResponseDto> uploadResume(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidDocumentException("Debe proporcionar un archivo PDF no vacío.");
        }

        try {
            ResumeResponseDto response = resumeIngestionService.ingestResume(
                    file.getOriginalFilename(),
                    file.getBytes()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IOException e) {
            throw new InvalidDocumentException("No se pudo leer el contenido del archivo subido.");
        }
    }
}