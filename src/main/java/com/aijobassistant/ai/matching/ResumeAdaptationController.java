package com.aijobassistant.ai.matching;

import com.aijobassistant.ai.matching.dto.AdaptResumeRequest;
import com.aijobassistant.ai.matching.dto.AdaptedResumeResponse;
import com.aijobassistant.ai.resume.model.StructuredResumeData;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/resumes/adapt")
public class ResumeAdaptationController {

    private final ResumeAdaptationService adaptationService;

    public ResumeAdaptationController(ResumeAdaptationService adaptationService) {
        this.adaptationService = adaptationService;
    }

    @PostMapping
    public ResponseEntity<AdaptedResumeResponse> adaptResume(@Valid @RequestBody AdaptResumeRequest request) {
        AdaptedResumeResponse response = adaptationService.adaptResume(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/adapted/{id}")
    @Operation(
            summary = "Actualizar el CV adaptado en tiempo real",
            description = "Permite modificar datos de contacto, enlaces, viñetas o texto del CV antes de la descarga final, manteniendo la validación de integridad."
    )
    public ResponseEntity<AdaptedResumeResponse> updateAdaptedResume(
            @PathVariable UUID id,
            @RequestBody StructuredResumeData updatedData) {

        AdaptedResumeResponse response = adaptationService.updateAdaptedResumeContent(id, updatedData);
        return ResponseEntity.ok(response);
    }
}