package com.aijobassistant.ai.matching.dto;

import com.aijobassistant.ai.matching.MatchAnalysis;
import com.aijobassistant.ai.matching.model.DetailedMatchReport;

import java.time.Instant;
import java.util.UUID;

public record MatchAnalysisResponse(
        UUID analysisId,
        UUID resumeId,
        UUID jobOfferId,
        int matchScore,
        String compatibilityLevel,
        DetailedMatchReport report,
        Instant createdAt
) {
    public static MatchAnalysisResponse of(MatchAnalysis analysis, DetailedMatchReport report) {
        return new MatchAnalysisResponse(
                analysis.getId(),
                analysis.getResume().getId(),
                analysis.getJobOffer().getId(),
                analysis.getMatchScore(),
                analysis.getCompatibilityLevel(),
                report,
                analysis.getCreatedAt()
        );
    }
}