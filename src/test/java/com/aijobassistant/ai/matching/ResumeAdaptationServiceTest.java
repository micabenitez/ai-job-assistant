package com.aijobassistant.ai.matching;

import com.aijobassistant.ai.joboffer.JobOffer;
import com.aijobassistant.ai.matching.dto.AdaptResumeRequest;
import com.aijobassistant.ai.matching.dto.AdaptedResumeResponse;
import com.aijobassistant.ai.resume.Resume;
import com.aijobassistant.ai.shared.domain.LlmGateway;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResumeAdaptationServiceTest {

    @Mock
    private MatchAnalysisRepository matchAnalysisRepository;
    @Mock
    private AdaptedResumeRepository adaptedResumeRepository;
    @Mock
    private AntiHallucinationValidator antiHallucinationValidator;
    @Mock
    private LlmGateway llmGateway;

    private ResumeAdaptationService adaptationService;

    @BeforeEach
    void setUp() {
        adaptationService = new ResumeAdaptationService(
                matchAnalysisRepository,
                adaptedResumeRepository,
                antiHallucinationValidator,
                llmGateway,
                new ObjectMapper()
        );
    }

    @Test
    @DisplayName("Debe adaptar el CV con éxito y llamar al validador anti-alucinación")
    void shouldAdaptResumeSuccessfully() {
        // Arrange
        UUID analysisId = UUID.randomUUID();
        Resume resume = new Resume("cv.pdf", "Experiencia con Java y Spring Boot.");
        JobOffer jobOffer = new JobOffer("Buscamos Java", "Java Dev", "Empresa");
        MatchAnalysis analysis = new MatchAnalysis(resume, jobOffer, 90, "ALTO", "{}");

        when(matchAnalysisRepository.findById(analysisId)).thenReturn(Optional.of(analysis));

        String fakeAdaptedJson = """
                {
                  "adaptationNotes": "Se destacó la experiencia en Java.",
                  "adaptedResume": {
                    "fullName": "Micaela",
                    "professionalSummary": "Desarrolladora Java orientada a backend.",
                    "experiences": [],
                    "education": [],
                    "technicalSkills": ["Java", "Spring Boot"],
                    "softSkills": [],
                    "languages": [],
                    "certifications": []
                  }
                }
                """;

        when(llmGateway.generateStructuredResponse(anyString(), anyString())).thenReturn(fakeAdaptedJson);
        when(adaptedResumeRepository.save(any(AdaptedResume.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        AdaptedResumeResponse response = adaptationService.adaptResume(new AdaptResumeRequest(analysisId));

        // Assert
        assertNotNull(response);
        assertEquals("Se destacó la experiencia en Java.", response.adaptationNotes());
        assertEquals("Micaela", response.adaptedContent().fullName());

        // Verifica que la barrera anti-alucinación fue ejecutada
        verify(antiHallucinationValidator, times(1)).validate(eq(resume), any());
        verify(adaptedResumeRepository, times(1)).save(any(AdaptedResume.class));
    }
}