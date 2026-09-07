package com.aijobassistant.ai.matching;

import com.aijobassistant.ai.joboffer.JobOffer;
import com.aijobassistant.ai.joboffer.JobOfferRepository;
import com.aijobassistant.ai.joboffer.JobOfferStructuringService;
import com.aijobassistant.ai.matching.dto.AnalyzeMatchRequest;
import com.aijobassistant.ai.matching.dto.MatchAnalysisResponse;
import com.aijobassistant.ai.matching.model.DetailedMatchReport;
import com.aijobassistant.ai.matching.model.RequirementEvaluation;
import com.aijobassistant.ai.matching.model.RequirementEvaluation.MatchStatus;
import com.aijobassistant.ai.resume.Resume;
import com.aijobassistant.ai.resume.ResumeRepository;
import com.aijobassistant.ai.resume.ResumeStructuringService;
import com.aijobassistant.ai.shared.domain.LlmGateway;
import com.aijobassistant.ai.shared.exception.DocumentProcessingException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class MatchAnalysisService {

    private static final String MATCHING_SYSTEM_PROMPT = """
            Eres un motor experto en reclutamiento técnico y evaluación objetiva de compatibilidad.
            Tu función es contrastar rigurosamente los requisitos de una oferta de empleo frente al contenido estructurado de un currículum vitae.
            
            REGLAS INQUEBRANTABLES:
            1. NO INVENTES NADA.
            2. Clasifica cada ítem usando estrictamente uno de estos tres valores de status:
               - "MATCH": El CV presenta evidencia clara y explícita de poseer esa competencia o una equivalente directa.
               - "PARTIAL": El candidato demuestra experiencia en tecnologías o conceptos muy cercanos, pero no exactos.
               - "MISSING": No existe evidencia alguna en el CV.
            3. Si un requerimiento obligatorio no se encuentra en el CV, márcalo como MISSING.
            
            Debes responder exclusivamente con el siguiente esquema JSON:
            {
              "requiredSkillsEvaluation": [
                {
                  "requirement": "nombre del requisito",
                  "status": "MATCH | PARTIAL | MISSING",
                  "evidenceFound": "fragmento o mención hallada en el CV o 'Ninguna'",
                  "observation": "justificación breve y técnica"
                }
              ],
              "optionalSkillsEvaluation": [],
              "softSkillsAndLanguagesEvaluation": [],
              "keyStrengths": ["fortaleza técnica relevante del candidato frente a la oferta"],
              "criticalGaps": ["requisito excluyente o brecha técnica no cubierta"]
            }
            """;

    private final ResumeRepository resumeRepository;
    private final JobOfferRepository jobOfferRepository;
    private final MatchAnalysisRepository matchAnalysisRepository;
    private final ResumeStructuringService resumeStructuringService;
    private final JobOfferStructuringService jobOfferStructuringService;
    private final LlmGateway llmGateway;
    private final ObjectMapper objectMapper;

    public MatchAnalysisService(
            ResumeRepository resumeRepository,
            JobOfferRepository jobOfferRepository,
            MatchAnalysisRepository matchAnalysisRepository,
            ResumeStructuringService resumeStructuringService,
            JobOfferStructuringService jobOfferStructuringService,
            LlmGateway llmGateway,
            ObjectMapper objectMapper) {
        this.resumeRepository = resumeRepository;
        this.jobOfferRepository = jobOfferRepository;
        this.matchAnalysisRepository = matchAnalysisRepository;
        this.resumeStructuringService = resumeStructuringService;
        this.jobOfferStructuringService = jobOfferStructuringService;
        this.llmGateway = llmGateway;
        this.objectMapper = objectMapper.copy().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Transactional
    public MatchAnalysisResponse analyze(AnalyzeMatchRequest request) {
        Resume resume = resumeRepository.findById(request.resumeId())
                .orElseThrow(() -> new DocumentProcessingException("No se encontró el CV con ID: " + request.resumeId(), null));

        JobOffer jobOffer = jobOfferRepository.findById(request.jobOfferId())
                .orElseThrow(() -> new DocumentProcessingException("No se encontró la oferta con ID: " + request.jobOfferId(), null));

        // Asegura que ambos registros cuenten con su estructura semántica procesada
        if (resume.getStructuredData() == null) {
            resumeStructuringService.structureResume(resume.getId());
            resume = resumeRepository.findById(resume.getId()).orElseThrow();
        }

        if (jobOffer.getStructuredRequirements() == null) {
            jobOfferStructuringService.structureJobOffer(jobOffer.getId());
            jobOffer = jobOfferRepository.findById(jobOffer.getId()).orElseThrow();
        }

        String userPrompt = String.format(
                "--- CURRICULUM VITAE (ESTRUCTURADO) ---\n%s\n\n--- REQUISITOS OFERTA LABORAL ---\n%s",
                resume.getStructuredData(),
                jobOffer.getStructuredRequirements()
        );

        String llmEvaluationJson = llmGateway.generateStructuredResponse(MATCHING_SYSTEM_PROMPT, userPrompt);

        try {
            LlmEvaluationIntermediate intermediate = objectMapper.readValue(llmEvaluationJson, LlmEvaluationIntermediate.class);

            // Cálculo matemático del score en Java
            int score = calculateDeterministicScore(
                    intermediate.requiredSkillsEvaluation(),
                    intermediate.optionalSkillsEvaluation(),
                    intermediate.softSkillsAndLanguagesEvaluation()
            );

            String compatibilityLevel = determineLevel(score);

            DetailedMatchReport report = new DetailedMatchReport(
                    score,
                    compatibilityLevel,
                    intermediate.requiredSkillsEvaluation(),
                    intermediate.optionalSkillsEvaluation(),
                    intermediate.softSkillsAndLanguagesEvaluation(),
                    intermediate.keyStrengths(),
                    intermediate.criticalGaps()
            );

            String finalReportJson = objectMapper.writeValueAsString(report);

            MatchAnalysis analysis = new MatchAnalysis(
                    resume,
                    jobOffer,
                    score,
                    compatibilityLevel,
                    finalReportJson
            );

            MatchAnalysis savedAnalysis = matchAnalysisRepository.save(analysis);

            return MatchAnalysisResponse.of(savedAnalysis, report);

        } catch (JsonProcessingException e) {
            throw new DocumentProcessingException("Error al parsear el informe de compatibilidad emitido por el LLM.", e);
        }
    }

    /**
     * Fórmula ponderada:
     * - Requisitos obligatorios: 60%
     * - Requisitos opcionales: 20%
     * - Habilidades blandas / idiomas: 20%
     */
    private int calculateDeterministicScore(
            List<RequirementEvaluation> required,
            List<RequirementEvaluation> optional,
            List<RequirementEvaluation> soft) {

        double requiredScore = calculateCategoryScore(required);
        double optionalScore = calculateCategoryScore(optional);
        double softScore = calculateCategoryScore(soft);

        double total = (requiredScore * 0.60) + (optionalScore * 0.20) + (softScore * 0.20);
        return (int) Math.round(total);
    }

    private double calculateCategoryScore(List<RequirementEvaluation> list) {
        if (list == null || list.isEmpty()) {
            return 100.0; // Si la oferta no exigía requerimientos en esta categoría, no penaliza
        }
        double points = 0;
        for (RequirementEvaluation item : list) {
            if (item.status() == MatchStatus.MATCH) {
                points += 1.0;
            } else if (item.status() == MatchStatus.PARTIAL) {
                points += 0.5;
            }
        }
        return (points / list.size()) * 100.0;
    }

    private String determineLevel(int score) {
        if (score >= 75) return "ALTO";
        if (score >= 50) return "MEDIO";
        return "BAJO";
    }

    // Record interno intermedio para deserializar la respuesta del LLM antes de incorporar score y level calculados
    private record LlmEvaluationIntermediate(
            List<RequirementEvaluation> requiredSkillsEvaluation,
            List<RequirementEvaluation> optionalSkillsEvaluation,
            List<RequirementEvaluation> softSkillsAndLanguagesEvaluation,
            List<String> keyStrengths,
            List<String> criticalGaps
    ) {
        public LlmEvaluationIntermediate {
            if (requiredSkillsEvaluation == null) requiredSkillsEvaluation = new ArrayList<>();
            if (optionalSkillsEvaluation == null) optionalSkillsEvaluation = new ArrayList<>();
            if (softSkillsAndLanguagesEvaluation == null) softSkillsAndLanguagesEvaluation = new ArrayList<>();
            if (keyStrengths == null) keyStrengths = new ArrayList<>();
            if (criticalGaps == null) criticalGaps = new ArrayList<>();
        }
    }
}