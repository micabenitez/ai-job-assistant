package com.aijobassistant.ai.matching;

import com.aijobassistant.ai.joboffer.JobOffer;
import com.aijobassistant.ai.resume.Resume;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "match_analyses")
public class MatchAnalysis {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_offer_id", nullable = false)
    private JobOffer jobOffer;

    @Column(name = "match_score")
    private Integer matchScore;

    @Column(name = "compatibility_level", length = 50)
    private String compatibilityLevel;

    @Column(name = "analysis_result", columnDefinition = "TEXT")
    private String analysisResult;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected MatchAnalysis() {
    }

    public MatchAnalysis(Resume resume, JobOffer jobOffer, Integer matchScore, String compatibilityLevel, String analysisResult) {
        this.id = UUID.randomUUID();
        this.resume = Objects.requireNonNull(resume, "El CV no puede ser nulo");
        this.jobOffer = Objects.requireNonNull(jobOffer, "La oferta de trabajo no puede ser nula");
        this.matchScore = matchScore;
        this.compatibilityLevel = compatibilityLevel;
        this.analysisResult = analysisResult;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public Resume getResume() {
        return resume;
    }

    public JobOffer getJobOffer() {
        return jobOffer;
    }

    public Integer getMatchScore() {
        return matchScore;
    }

    public String getCompatibilityLevel() {
        return compatibilityLevel;
    }

    public String getAnalysisResult() {
        return analysisResult;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MatchAnalysis that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}