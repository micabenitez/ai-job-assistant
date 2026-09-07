package com.aijobassistant.ai.joboffer.model;

import java.util.List;

public record StructuredJobOfferRequirements(
        String roleTitle,
        String seniorityLevel,
        List<String> requiredTechnicalSkills,
        List<String> optionalTechnicalSkills,
        List<String> softSkills,
        List<String> responsibilities,
        List<String> languages
) {}