package com.aijobassistant.ai.application;

import com.aijobassistant.ai.application.dto.CreateJobApplicationRequest;
import com.aijobassistant.ai.application.dto.JobApplicationResponse;
import com.aijobassistant.ai.application.dto.UpdateJobApplicationRequest;
import com.aijobassistant.ai.application.model.ApplicationStatus;
import com.aijobassistant.ai.application.model.JobApplication;
import com.aijobassistant.ai.application.repository.JobApplicationRepository;
import com.aijobassistant.ai.matching.AdaptedResume;
import com.aijobassistant.ai.matching.AdaptedResumeRepository;
import com.aijobassistant.ai.matching.MatchAnalysis;
import com.aijobassistant.ai.matching.MatchAnalysisRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class JobApplicationService {

    private final JobApplicationRepository jobApplicationRepository;
    private final AdaptedResumeRepository adaptedResumeRepository;
    private final MatchAnalysisRepository matchAnalysisRepository;

    public JobApplicationService(JobApplicationRepository jobApplicationRepository,
                                 AdaptedResumeRepository adaptedResumeRepository,
                                 MatchAnalysisRepository matchAnalysisRepository) {
        this.jobApplicationRepository = jobApplicationRepository;
        this.adaptedResumeRepository = adaptedResumeRepository;
        this.matchAnalysisRepository = matchAnalysisRepository;
    }

    @Transactional(readOnly = true)
    public List<JobApplicationResponse> listApplications(ApplicationStatus status, String search) {
        String cleanSearch = (search != null && !search.isBlank()) ? search.trim() : null;
        return jobApplicationRepository.findAllWithFilters(status, cleanSearch)
                .stream()
                .map(JobApplicationResponse::fromEntity)
                .toList();
    }

    @Transactional
    public JobApplicationResponse createApplication(CreateJobApplicationRequest request) {
        AdaptedResume adaptedResume = null;
        if (request.adaptedResumeId() != null) {
            adaptedResume = adaptedResumeRepository.findById(request.adaptedResumeId())
                    .orElseThrow(() -> new EntityNotFoundException("No se encontró el CV adaptado con ID: " + request.adaptedResumeId()));
        }

        MatchAnalysis matchAnalysis = null;
        if (request.matchAnalysisId() != null) {
            matchAnalysis = matchAnalysisRepository.findById(request.matchAnalysisId())
                    .orElseThrow(() -> new EntityNotFoundException("No se encontró el análisis de coincidencia con ID: " + request.matchAnalysisId()));
        }

        ApplicationStatus initialStatus = request.status() != null ? request.status() : ApplicationStatus.ADAPTED;

        JobApplication entity = new JobApplication(
                request.companyName().trim(),
                request.roleTitle().trim(),
                initialStatus,
                request.matchScore(),
                Instant.now(),
                adaptedResume,
                matchAnalysis,
                request.notes() != null ? request.notes().trim() : null
        );

        JobApplication saved = jobApplicationRepository.save(entity);
        return JobApplicationResponse.fromEntity(saved);
    }

    @Transactional
    public JobApplicationResponse updateApplication(UUID id, UpdateJobApplicationRequest request) {
        JobApplication application = jobApplicationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró la postulación con ID: " + id));

        if (request.status() != null) {
            if (request.status() == ApplicationStatus.APPLIED && application.getStatus() == ApplicationStatus.ADAPTED) {
                application.setAppliedAt(Instant.now());
            }
            application.setStatus(request.status());
        }

        if (request.notes() != null) {
            application.setNotes(request.notes().trim());
        }

        JobApplication updated = jobApplicationRepository.save(application);
        return JobApplicationResponse.fromEntity(updated);
    }

    @Transactional
    public void deleteApplication(UUID id) {
        if (!jobApplicationRepository.existsById(id)) {
            throw new EntityNotFoundException("No se encontró la postulación con ID: " + id);
        }
        jobApplicationRepository.deleteById(id);
    }
}