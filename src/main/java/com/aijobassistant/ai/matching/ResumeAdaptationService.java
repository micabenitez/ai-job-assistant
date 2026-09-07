package com.aijobassistant.ai.matching;

import com.aijobassistant.ai.matching.dto.AdaptResumeRequest;
import com.aijobassistant.ai.matching.dto.AdaptedResumeResponse;
import com.aijobassistant.ai.resume.Resume;
import com.aijobassistant.ai.resume.model.StructuredResumeData;
import com.aijobassistant.ai.shared.domain.LlmGateway;
import com.aijobassistant.ai.shared.exception.DocumentProcessingException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ResumeAdaptationService {

    private static final String ADAPTATION_SYSTEM_PROMPT = """
            Eres un redactor y optimizador profesional de currículums para el sector tecnológico.
            Tu función es generar una versión adaptada del currículum original para maximizar su alineación con una oferta de empleo específica.
            
            REGLAS INQUEBRANTABLES:
            1. REGLA FUNDAMENTAL: NO INVENTAR INFORMACIÓN.
            2. NO agregues bajo ninguna circunstancia tecnologías, certificaciones, empresas, cargos o años de experiencia que no estén presentes en el CV original.
            3. Si la vacante requiere tecnologías que el candidato NO posee, NO las agregues.
            4. SOLAMENTE PUEDES:
               - Reorganizar las secciones para destacar las experiencias más afines a la vacante.
               - Reescribir el resumen profesional enfatizando las fortalezas reales del candidato frente al rol.
               - Mejorar la redacción y síntesis de las responsabilidades reales.
               - Priorizar las palabras clave que el candidato verdaderamente domina y que la vacante solicita.
            
            Debes responder estrictamente en formato JSON con la siguiente estructura:
            {
              "adaptationNotes": "Explicación breve de los cambios realizados y secciones priorizadas",
              "adaptedResume": {
                "fullName": "Nombre original",
                "professionalSummary": "Resumen adaptado destacando fortalezas reales",
                "experiences": [
                  {
                    "role": "Cargo",
                    "company": "Empresa",
                    "duration": "Periodo",
                    "responsibilities": ["responsabilidades redactadas con impacto"],
                    "technologiesUsed": ["tecnologías reales"]
                  }
                ],
                "education": [],
                "technicalSkills": ["habilidades técnicas reales coincidentes o relevantes"],
                "softSkills": ["habilidades blandas"],
                "languages": ["idiomas"],
                "certifications": []
              }
            }
            """;

    private final MatchAnalysisRepository matchAnalysisRepository;
    private final AdaptedResumeRepository adaptedResumeRepository;
    private final AntiHallucinationValidator antiHallucinationValidator;
    private final LlmGateway llmGateway;
    private final ObjectMapper objectMapper;

    public ResumeAdaptationService(
            MatchAnalysisRepository matchAnalysisRepository,
            AdaptedResumeRepository adaptedResumeRepository,
            AntiHallucinationValidator antiHallucinationValidator,
            LlmGateway llmGateway,
            ObjectMapper objectMapper) {
        this.matchAnalysisRepository = matchAnalysisRepository;
        this.adaptedResumeRepository = adaptedResumeRepository;
        this.antiHallucinationValidator = antiHallucinationValidator;
        this.llmGateway = llmGateway;
        this.objectMapper = objectMapper.copy().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Transactional
    public AdaptedResumeResponse adaptResume(AdaptResumeRequest request) {
        MatchAnalysis analysis = matchAnalysisRepository.findById(request.matchAnalysisId())
                .orElseThrow(() -> new DocumentProcessingException("No se encontró el análisis de coincidencia con ID: " + request.matchAnalysisId(), null));

        Resume originalResume = analysis.getResume();

        String userPrompt = String.format(
                "--- CURRICULUM ORIGINAL ---\n%s\n\n--- OFERTA LABORAL OBJETIVO ---\n%s\n\n--- INFORME DE COINCIDENCIAS Y BRECHAS ---\n%s",
                originalResume.getRawText(),
                analysis.getJobOffer().getRawText(),
                analysis.getAnalysisResult()
        );

        String rawJsonResponse = llmGateway.generateStructuredResponse(ADAPTATION_SYSTEM_PROMPT, userPrompt);

        try {
            AdaptationOutput output = objectMapper.readValue(rawJsonResponse, AdaptationOutput.class);

            // Validación determinística anti-alucinación en código Java
            antiHallucinationValidator.validate(originalResume, output.adaptedResume());

            String adaptedContentJson = objectMapper.writeValueAsString(output.adaptedResume());

            AdaptedResume adaptedResume = new AdaptedResume(
                    analysis,
                    adaptedContentJson,
                    output.adaptationNotes()
            );

            AdaptedResume savedEntity = adaptedResumeRepository.save(adaptedResume);

            return AdaptedResumeResponse.of(savedEntity, output.adaptedResume());

        } catch (JsonProcessingException e) {
            throw new DocumentProcessingException("Error al procesar el JSON emitido en la adaptación del CV.", e);
        }
    }

    private record AdaptationOutput(
            String adaptationNotes,
            StructuredResumeData adaptedResume
    ) {}
}