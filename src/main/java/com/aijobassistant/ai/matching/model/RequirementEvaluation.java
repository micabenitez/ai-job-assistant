package com.aijobassistant.ai.matching.model;

public record RequirementEvaluation(
        String requirement,
        MatchStatus status, // MATCH, PARTIAL, MISSING
        String evidenceFound,
        String observation
) {
    public enum MatchStatus {
        MATCH,
        PARTIAL,
        MISSING
    }
}