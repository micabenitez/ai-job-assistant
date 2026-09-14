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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobApplicationServiceTest {

    @Mock
    private JobApplicationRepository jobApplicationRepository;

    @Mock
    private AdaptedResumeRepository adaptedResumeRepository;

    @Mock
    private MatchAnalysisRepository matchAnalysisRepository;

    @InjectMocks
    private JobApplicationService jobApplicationService;

    private UUID applicationId;
    private UUID adaptedResumeId;
    private UUID matchAnalysisId;

    @BeforeEach
    void setUp() {
        applicationId = UUID.randomUUID();
        adaptedResumeId = UUID.randomUUID();
        matchAnalysisId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Debe crear una postulación exitosamente con status ADAPTED por defecto")
    void shouldCreateApplicationSuccessfully() {
        CreateJobApplicationRequest request = new CreateJobApplicationRequest(
                "Fintech Corp",
                "Backend Engineer",
                null,
                88,
                adaptedResumeId,
                matchAnalysisId,
                "CV adaptado con foco en Spring Boot"
        );

        AdaptedResume mockAdaptedResume = mock(AdaptedResume.class);
        MatchAnalysis mockMatchAnalysis = mock(MatchAnalysis.class);

        when(adaptedResumeRepository.findById(adaptedResumeId)).thenReturn(Optional.of(mockAdaptedResume));
        when(matchAnalysisRepository.findById(matchAnalysisId)).thenReturn(Optional.of(mockMatchAnalysis));
        when(jobApplicationRepository.save(any(JobApplication.class))).thenAnswer(invocation -> {
            JobApplication app = invocation.getArgument(0);
            app.setId(applicationId);
            return app;
        });

        JobApplicationResponse response = jobApplicationService.createApplication(request);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(applicationId);
        assertThat(response.companyName()).isEqualTo("Fintech Corp");
        assertThat(response.status()).isEqualTo(ApplicationStatus.ADAPTED);
        assertThat(response.matchScore()).isEqualTo(88);
        verify(jobApplicationRepository, times(1)).save(any(JobApplication.class));
    }

    @Test
    @DisplayName("Debe lanzar EntityNotFoundException si el adaptedResumeId no existe")
    void shouldThrowWhenAdaptedResumeNotFound() {
        CreateJobApplicationRequest request = new CreateJobApplicationRequest(
                "Fintech Corp",
                "Backend Engineer",
                ApplicationStatus.ADAPTED,
                88,
                adaptedResumeId,
                null,
                null
        );

        when(adaptedResumeRepository.findById(adaptedResumeId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jobApplicationService.createApplication(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(adaptedResumeId.toString());

        verify(jobApplicationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe actualizar status a APPLIED y setear appliedAt si transiciona desde ADAPTED")
    void shouldUpdateStatusToAppliedAndRefreshAppliedAt() {
        JobApplication existingApp = new JobApplication(
                "Fintech Corp",
                "Backend Engineer",
                ApplicationStatus.ADAPTED,
                88,
                Instant.now().minusSeconds(3600),
                null,
                null,
                "Inicial"
        );
        existingApp.setId(applicationId);

        when(jobApplicationRepository.findById(applicationId)).thenReturn(Optional.of(existingApp));
        when(jobApplicationRepository.save(any(JobApplication.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateJobApplicationRequest updateRequest = new UpdateJobApplicationRequest(
                ApplicationStatus.APPLIED,
                "Postulado vía LinkedIn"
        );

        JobApplicationResponse updated = jobApplicationService.updateApplication(applicationId, updateRequest);

        assertThat(updated.status()).isEqualTo(ApplicationStatus.APPLIED);
        assertThat(updated.notes()).isEqualTo("Postulado vía LinkedIn");
    }

    @Test
    @DisplayName("Debe listar postulaciones filtradas llamando al repositorio")
    void shouldListApplications() {
        when(jobApplicationRepository.findAllWithFilters(ApplicationStatus.APPLIED, "fintech"))
                .thenReturn(List.of());

        List<JobApplicationResponse> result = jobApplicationService.listApplications(ApplicationStatus.APPLIED, " fintech ");

        assertThat(result).isEmpty();
        verify(jobApplicationRepository).findAllWithFilters(ApplicationStatus.APPLIED, "fintech");
    }

    @Test
    @DisplayName("Debe eliminar postulación existente sin afectar registros relacionados")
    void shouldDeleteApplicationWhenExists() {
        when(jobApplicationRepository.existsById(applicationId)).thenReturn(true);

        jobApplicationService.deleteApplication(applicationId);

        verify(jobApplicationRepository, times(1)).deleteById(applicationId);
    }
}