package com.aijobassistant.ai.matching;

import com.aijobassistant.ai.matching.dto.AdaptResumeRequest;
import com.aijobassistant.ai.matching.dto.AdaptedResumeResponse;
import com.aijobassistant.ai.resume.model.StructuredResumeData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/resumes/adapt")
@Tag(name = "Resume Adaptation & Export", description = "Endpoints para adaptar CVs, editarlos en vivo y exportar a PDF")
public class ResumeAdaptationController {

    private final ResumeAdaptationService adaptationService;
    private final PdfResumeExportService pdfExportService;

    public ResumeAdaptationController(
            ResumeAdaptationService adaptationService,
            PdfResumeExportService pdfExportService) {
        this.adaptationService = adaptationService;
        this.pdfExportService = pdfExportService;
    }

    @PostMapping
    @Operation(summary = "Adaptar CV al puesto mediante IA", description = "Genera una versión optimizada para ATS basada en el análisis de afinidad.")
    public ResponseEntity<AdaptedResumeResponse> adaptResume(@Valid @RequestBody AdaptResumeRequest request) {
        AdaptedResumeResponse response = adaptationService.adaptResume(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener el CV adaptado por ID", description = "Devuelve el JSON estructurado del CV adaptado para alimentar el formulario del editor en el frontend.")
    public ResponseEntity<AdaptedResumeResponse> getAdaptedResumeById(@PathVariable UUID id) {
        AdaptedResumeResponse response = adaptationService.getAdaptedResumeById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Actualizar el CV adaptado en tiempo real",
            description = "Permite modificar datos de contacto, enlaces, viñetas o texto del CV antes de la descarga final, manteniendo la validación de integridad."
    )
    public ResponseEntity<AdaptedResumeResponse> updateAdaptedResume(
            @PathVariable UUID id,
            @Valid @RequestBody StructuredResumeData updatedData) {

        AdaptedResumeResponse response = adaptationService.updateAdaptedResumeContent(id, updatedData);
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(
            summary = "Exportar el CV adaptado a PDF",
            description = "Genera el binario PDF en formato Harvard ATS. Soporta 'inline' para el visor embebido o 'attachment' para descarga directa."
    )
    public ResponseEntity<byte[]> exportAdaptedResumeToPdf(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "inline") String disposition) {

        byte[] pdfBytes = pdfExportService.exportAdaptedResumeToPdf(id);

        String filename = "CV_Adaptado_ATS.pdf";
        String contentDisposition = "attachment".equalsIgnoreCase(disposition)
                ? "attachment; filename=\"" + filename + "\""
                : "inline; filename=\"" + filename + "\"";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdfBytes.length)
                .body(pdfBytes);
    }
}