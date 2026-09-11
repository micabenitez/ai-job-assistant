package com.aijobassistant.ai.resume.model;

import java.util.List;

public record StructuredResumeData(
        String fullName,
        String contactInfo,
        String professionalSummary,
        List<ProjectItem> projects,
        List<ExperienceItem> experiences,
        List<EducationInstitution> education,
        List<ComplementaryEducationItem> complementaryEducation,
        List<SkillCategory> technicalSkillsCategories,
        List<String> technicalSkills,
        List<String> languages
) {
    public record ProjectItem(
            String name,
            String year,
            List<String> overviewParagraphs,
            List<String> highlights,
            List<String> technologiesUsed,
            String repoLink,
            String moreProjectsLink
    ) {}

    public record ExperienceItem(
            String role,
            String company,
            String duration,
            List<String> responsibilities,
            List<String> technologiesUsed
    ) {}

    public record EducationInstitution(
            String institution,
            List<String> items
    ) {}

    public record ComplementaryEducationItem(
            String title,
            String entity,
            String period
    ) {}

    public record SkillCategory(
            String category,
            String skills
    ) {}
}