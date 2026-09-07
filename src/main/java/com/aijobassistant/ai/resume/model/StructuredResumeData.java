package com.aijobassistant.ai.resume.model;

import java.util.List;

public record StructuredResumeData(
        String fullName,
        String professionalSummary,
        List<ExperienceItem> experiences,
        List<EducationItem> education,
        List<String> technicalSkills,
        List<String> softSkills,
        List<String> languages,
        List<String> certifications
) {
    public record ExperienceItem(
            String role,
            String company,
            String duration,
            List<String> responsibilities,
            List<String> technologiesUsed
    ) {}

    public record EducationItem(
            String degree,
            String institution,
            String year
    ) {}
}