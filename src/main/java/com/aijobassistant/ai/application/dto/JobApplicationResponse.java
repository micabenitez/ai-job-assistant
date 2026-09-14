package com.aijobassistant.ai.application.dto;

import com.aijobassistant.ai.application.model.ApplicationStatus;
import com.aijobassistant.ai.application.model.JobApplication;

import java.time.Instant;
import java.util.UUID;

public record JobApplicationResponse(
        UUID id,
        String companyName,
        String roleTitle,
        ApplicationStatus status,
        Integer matchScore,
        Instant appliedAt,
        UUID adaptedResumeId,
        UUID matchAnalysisId,
        String notes,
        Instant createdAt
) {
    public static JobApplicationResponse fromEntity(JobApplication entity) {
        return new JobApplicationResponse(
                entity.getId(),
                entity.getCompanyName(),
                entity.getRoleTitle(),
                entity.getStatus(),
                entity.getMatchScore(),
                entity.getAppliedAt(),
                entity.getAdaptedResume() != null ? entity.getAdaptedResume().getId() : null,
                entity.getMatchAnalysis() != null ? entity.getMatchAnalysis().getId() : null,
                entity.getNotes(),
                entity.getCreatedAt()
        );
    }
}