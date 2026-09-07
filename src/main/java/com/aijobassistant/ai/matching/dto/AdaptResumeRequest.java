package com.aijobassistant.ai.matching.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AdaptResumeRequest(
        @NotNull(message = "El ID del análisis de coincidencia es obligatorio")
        UUID matchAnalysisId
) {}