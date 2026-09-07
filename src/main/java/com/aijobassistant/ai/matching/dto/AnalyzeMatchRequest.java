package com.aijobassistant.ai.matching.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AnalyzeMatchRequest(
        @NotNull(message = "El ID del CV es obligatorio")
        UUID resumeId,

        @NotNull(message = "El ID de la oferta laboral es obligatorio")
        UUID jobOfferId
) {}