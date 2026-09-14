package com.aijobassistant.ai.application.model;

import com.aijobassistant.ai.matching.AdaptedResume;
import com.aijobassistant.ai.matching.MatchAnalysis;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "job_applications")
public class JobApplication {

    @Id
    private UUID id;

    @Column(name = "company_name", nullable = false, length = 255)
    private String companyName;

    @Column(name = "role_title", nullable = false, length = 255)
    private String roleTitle;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private ApplicationStatus status = ApplicationStatus.ADAPTED;

    @Column(name = "match_score", nullable = false)
    private Integer matchScore;

    @Column(name = "applied_at", nullable = false)
    private Instant appliedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "adapted_resume_id", foreignKey = @ForeignKey(name = "fk_job_app_adapted_resume"))
    private AdaptedResume adaptedResume;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_analysis_id", foreignKey = @ForeignKey(name = "fk_job_app_match_analysis"))
    private MatchAnalysis matchAnalysis;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public JobApplication() {
    }

    public JobApplication(String companyName,
                          String roleTitle,
                          ApplicationStatus status,
                          Integer matchScore,
                          Instant appliedAt,
                          AdaptedResume adaptedResume,
                          MatchAnalysis matchAnalysis,
                          String notes) {
        this.companyName = companyName;
        this.roleTitle = roleTitle;
        this.status = status != null ? status : ApplicationStatus.ADAPTED;
        this.matchScore = matchScore;
        this.appliedAt = appliedAt != null ? appliedAt : Instant.now();
        this.adaptedResume = adaptedResume;
        this.matchAnalysis = matchAnalysis;
        this.notes = notes;
    }

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.appliedAt == null) {
            this.appliedAt = now;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getRoleTitle() {
        return roleTitle;
    }

    public void setRoleTitle(String roleTitle) {
        this.roleTitle = roleTitle;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public Integer getMatchScore() {
        return matchScore;
    }

    public void setMatchScore(Integer matchScore) {
        this.matchScore = matchScore;
    }

    public Instant getAppliedAt() {
        return appliedAt;
    }

    public void setAppliedAt(Instant appliedAt) {
        this.appliedAt = appliedAt;
    }

    public AdaptedResume getAdaptedResume() {
        return adaptedResume;
    }

    public void setAdaptedResume(AdaptedResume adaptedResume) {
        this.adaptedResume = adaptedResume;
    }

    public MatchAnalysis getMatchAnalysis() {
        return matchAnalysis;
    }

    public void setMatchAnalysis(MatchAnalysis matchAnalysis) {
        this.matchAnalysis = matchAnalysis;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}