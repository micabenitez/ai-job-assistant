package com.aijobassistant.ai.application;

import com.aijobassistant.ai.application.dto.CreateJobApplicationRequest;
import com.aijobassistant.ai.application.dto.JobApplicationResponse;
import com.aijobassistant.ai.application.dto.UpdateJobApplicationRequest;
import com.aijobassistant.ai.application.model.ApplicationStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/applications")
@Tag(name = "job-applications", description = "Endpoints para el seguimiento de postulaciones laborales")
public class JobApplicationController {

    private final JobApplicationService jobApplicationService;

    public JobApplicationController(JobApplicationService jobApplicationService) {
        this.jobApplicationService = jobApplicationService;
    }

    @GetMapping
    @Operation(summary = "Listar postulaciones", description = "Retorna el historial de postulaciones ordenadas por fecha más reciente, con filtros opcionales.")
    public ResponseEntity<List<JobApplicationResponse>> listApplications(
            @RequestParam(required = false) ApplicationStatus status,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(jobApplicationService.listApplications(status, search));
    }

    @PostMapping
    @Operation(summary = "Registrar postulación", description = "Guarda una nueva postulación en el tablero tras adaptar el CV.")
    public ResponseEntity<JobApplicationResponse> createApplication(
            @Valid @RequestBody CreateJobApplicationRequest request) {
        JobApplicationResponse response = jobApplicationService.createApplication(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Actualizar estado o notas", description = "Actualiza de forma granular la etapa de selección (Kanban) y/o las notas de la postulación.")
    public ResponseEntity<JobApplicationResponse> updateApplication(
            @PathVariable UUID id,
            @RequestBody UpdateJobApplicationRequest request) {
        return ResponseEntity.ok(jobApplicationService.updateApplication(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar postulación", description = "Elimina la postulación del tablero sin borrar el CV adaptado asociado.")
    public ResponseEntity<Void> deleteApplication(@PathVariable UUID id) {
        jobApplicationService.deleteApplication(id);
        return ResponseEntity.noContent().build();
    }
}