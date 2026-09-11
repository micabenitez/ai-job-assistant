package com.aijobassistant.ai.matching;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "adapted_resumes")
public class AdaptedResume {

    @Id
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "match_analysis_id", nullable = false, unique = true)
    private MatchAnalysis matchAnalysis;

    @Column(name = "adapted_content", nullable = false, columnDefinition = "TEXT")
    private String adaptedContent;

    @Column(name = "adaptation_notes", columnDefinition = "TEXT")
    private String adaptationNotes;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected AdaptedResume() {
    }

    public AdaptedResume(MatchAnalysis matchAnalysis, String adaptedContent, String adaptationNotes) {
        this.id = UUID.randomUUID();
        this.matchAnalysis = Objects.requireNonNull(matchAnalysis, "El análisis de coincidencia no puede ser nulo");
        this.adaptedContent = Objects.requireNonNull(adaptedContent, "El contenido adaptado no puede ser nulo");
        this.adaptationNotes = adaptationNotes;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public MatchAnalysis getMatchAnalysis() {
        return matchAnalysis;
    }

    public String getAdaptedContent() {
        return adaptedContent;
    }

    public String getAdaptationNotes() {
        return adaptationNotes;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void updateContent(String newAdaptedContent) {
        this.adaptedContent = Objects.requireNonNull(newAdaptedContent, "El contenido adaptado no puede ser nulo");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AdaptedResume that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}