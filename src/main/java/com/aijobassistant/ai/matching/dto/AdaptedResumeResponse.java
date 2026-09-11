package com.aijobassistant.ai.matching.dto;

import com.aijobassistant.ai.matching.AdaptedResume;
import com.aijobassistant.ai.resume.model.StructuredResumeData;

import java.time.Instant;
import java.util.UUID;

public record AdaptedResumeResponse(
        UUID adaptedResumeId,
        UUID matchAnalysisId,
        StructuredResumeData adaptedContent,
        String adaptationNotes,
        Instant createdAt
) {
    public static AdaptedResumeResponse of(AdaptedResume entity, StructuredResumeData content) {
        return new AdaptedResumeResponse(
                entity.getId(),
                entity.getMatchAnalysis().getId(),
                content,
                entity.getAdaptationNotes(),
                entity.getCreatedAt()
        );
    }
}