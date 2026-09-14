package com.aijobassistant.ai.application.dto;

import com.aijobassistant.ai.application.model.ApplicationStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateJobApplicationRequest(
        @NotBlank(message = "El nombre de la empresa no puede estar vacío")
        @Size(max = 255, message = "El nombre de la empresa no puede superar los 255 caracteres")
        String companyName,

        @NotBlank(message = "El título del rol no puede estar vacío")
        @Size(max = 255, message = "El título del rol no puede superar los 255 caracteres")
        String roleTitle,

        ApplicationStatus status,

        @NotNull(message = "El match score es obligatorio")
        @Min(value = 0, message = "El match score mínimo es 0")
        @Max(value = 100, message = "El match score máximo es 100")
        Integer matchScore,

        UUID adaptedResumeId,

        UUID matchAnalysisId,

        String notes
) {}