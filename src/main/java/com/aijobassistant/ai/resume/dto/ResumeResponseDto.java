package com.aijobassistant.ai.resume.dto;

import com.aijobassistant.ai.resume.Resume;

import java.time.Instant;
import java.util.UUID;

public record ResumeResponseDto(
        UUID id,
        String originalFilename,
        int rawTextLength,
        Instant createdAt
) {
    public static ResumeResponseDto fromEntity(Resume resume) {
        return new ResumeResponseDto(
                resume.getId(),
                resume.getOriginalFilename(),
                resume.getRawText() != null ? resume.getRawText().length() : 0,
                resume.getCreatedAt()
        );
    }
}