package com.aijobassistant.ai.matching;

import com.aijobassistant.ai.joboffer.JobOffer;
import com.aijobassistant.ai.joboffer.JobOfferRepository;
import com.aijobassistant.ai.joboffer.JobOfferStructuringService;
import com.aijobassistant.ai.matching.dto.AnalyzeMatchRequest;
import com.aijobassistant.ai.matching.dto.MatchAnalysisResponse;
import com.aijobassistant.ai.resume.Resume;
import com.aijobassistant.ai.resume.ResumeRepository;
import com.aijobassistant.ai.resume.ResumeStructuringService;
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
class MatchAnalysisServiceTest {

    @Mock
    private ResumeRepository resumeRepository;
    @Mock
    private JobOfferRepository jobOfferRepository;
    @Mock
    private MatchAnalysisRepository matchAnalysisRepository;
    @Mock
    private ResumeStructuringService resumeStructuringService;
    @Mock
    private JobOfferStructuringService jobOfferStructuringService;
    @Mock
    private LlmGateway llmGateway;

    private MatchAnalysisService matchAnalysisService;

    @BeforeEach
    void setUp() {
        matchAnalysisService = new MatchAnalysisService(
                resumeRepository,
                jobOfferRepository,
                matchAnalysisRepository,
                resumeStructuringService,
                jobOfferStructuringService,
                llmGateway,
                new ObjectMapper()
        );
    }

    @Test
    @DisplayName("Debe calcular correctamente el score ponderado y guardar el análisis")
    void shouldCalculateScoreAndSaveMatchAnalysis() {
        // Arrange
        UUID resumeId = UUID.randomUUID();
        UUID jobOfferId = UUID.randomUUID();

        Resume resume = new Resume("cv.pdf", "Texto CV");
        resume.updateStructuredData("{\"technicalSkills\":[\"Java\"]}");

        JobOffer jobOffer = new JobOffer("Texto oferta", "Backend Dev", "Empresa");
        jobOffer.updateStructuredRequirements("{\"requiredTechnicalSkills\":[\"Java\"]}");

        when(resumeRepository.findById(resumeId)).thenReturn(Optional.of(resume));
        when(jobOfferRepository.findById(jobOfferId)).thenReturn(Optional.of(jobOffer));

        // Simulamos evaluación donde el requerimiento obligatorio cumple al 100%
        String fakeLlmEvaluation = """
                {
                  "requiredSkillsEvaluation": [
                    {
                      "requirement": "Java",
                      "status": "MATCH",
                      "evidenceFound": "Java en skills",
                      "observation": "Coincidencia exacta"
                    }
                  ],
                  "optionalSkillsEvaluation": [],
                  "softSkillsAndLanguagesEvaluation": [],
                  "keyStrengths": ["Fuerte en Java"],
                  "criticalGaps": []
                }
                """;

        when(llmGateway.generateStructuredResponse(anyString(), anyString())).thenReturn(fakeLlmEvaluation);
        when(matchAnalysisRepository.save(any(MatchAnalysis.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        MatchAnalysisResponse response = matchAnalysisService.analyze(new AnalyzeMatchRequest(resumeId, jobOfferId));

        // Assert
        assertNotNull(response);
        assertEquals(100, response.matchScore()); // 60% por required (1/1) + 20% opcional vacio + 20% soft vacio = 100
        assertEquals("ALTO", response.compatibilityLevel());
        assertFalse(response.report().requiredSkillsEvaluation().isEmpty());
        assertEquals("MATCH", response.report().requiredSkillsEvaluation().getFirst().status().name());

        verify(matchAnalysisRepository, times(1)).save(any(MatchAnalysis.class));
    }
}