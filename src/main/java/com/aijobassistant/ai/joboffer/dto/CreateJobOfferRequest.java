package com.aijobassistant.ai.joboffer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateJobOfferRequest(
        @NotBlank(message = "El texto de la oferta de trabajo no puede estar vacío")
        @Size(min = 30, message = "La descripción de la oferta debe contener al menos 30 caracteres para permitir un análisis válido")
        String rawText,

        @Size(max = 255, message = "El título del puesto no puede exceder 255 caracteres")
        String title,

        @Size(max = 255, message = "El nombre de la empresa no puede exceder 255 caracteres")
        String company
) {
}