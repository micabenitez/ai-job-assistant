package com.aijobassistant.ai.joboffer;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "job_offers")
public class JobOffer {

    @Id
    private UUID id;

    @Column(name = "title")
    private String title;

    @Column(name = "company")
    private String company;

    @Column(name = "raw_text", nullable = false, columnDefinition = "TEXT")
    private String rawText;

    @Column(name = "structured_requirements", columnDefinition = "TEXT")
    private String structuredRequirements;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected JobOffer() {
        // Constructor JPA
    }

    public JobOffer(String rawText, String title, String company) {
        this.id = UUID.randomUUID();
        this.rawText = Objects.requireNonNull(rawText, "La descripción de la oferta no puede ser nula");
        this.title = title;
        this.company = company;
        this.createdAt = Instant.now();
    }

    public void updateStructuredRequirements(String structuredRequirementsJson) {
        this.structuredRequirements = structuredRequirementsJson;
    }

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getCompany() {
        return company;
    }

    public String getRawText() {
        return rawText;
    }

    public String getStructuredRequirements() {
        return structuredRequirements;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JobOffer jobOffer)) return false;
        return Objects.equals(id, jobOffer.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}