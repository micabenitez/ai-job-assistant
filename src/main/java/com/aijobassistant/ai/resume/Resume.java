package com.aijobassistant.ai.resume;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "resumes")
public class Resume {

    @Id
    private UUID id;

    @Column(name = "original_filename", nullable = false)
    private String originalFilename;

    @Column(name = "raw_text", nullable = false, columnDefinition = "TEXT")
    private String rawText;

    @Column(name = "structured_data", columnDefinition = "TEXT")
    private String structuredData;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Resume() {
    }

    public Resume(String originalFilename, String rawText) {
        this.id = UUID.randomUUID();
        this.originalFilename = Objects.requireNonNull(originalFilename, "El nombre del archivo no puede ser nulo");
        this.rawText = Objects.requireNonNull(rawText, "El texto plano del CV no puede ser nulo");
        this.createdAt = Instant.now();
    }

    public void updateStructuredData(String structuredDataJson) {
        this.structuredData = structuredDataJson;
    }

    public UUID getId() {
        return id;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public String getRawText() {
        return rawText;
    }

    public String getStructuredData() {
        return structuredData;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Resume resume)) return false;
        return Objects.equals(id, resume.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}