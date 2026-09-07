package com.aijobassistant.ai.matching.model;

import java.util.List;

public record DetailedMatchReport(
        int overallScore,
        String compatibilityLevel,
        List<RequirementEvaluation> requiredSkillsEvaluation,
        List<RequirementEvaluation> optionalSkillsEvaluation,
        List<RequirementEvaluation> softSkillsAndLanguagesEvaluation,
        List<String> keyStrengths,
        List<String> criticalGaps
) {}