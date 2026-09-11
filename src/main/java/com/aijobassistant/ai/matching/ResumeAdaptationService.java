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
            Eres un especialista en redacción técnica estratégica de CVs.
            Tu objetivo es adaptar el currículum conservando con exactitud la identidad visual y estructura del documento original.

            REGLA DE INTEGRIDAD:
            NO INVENTES NADA. Mantén estrictamente los datos de contacto, educación y proyectos reales provistos en el CV original.
            
            DIRECTIVAS DE ADAPTACIÓN:
            1. CONTACTO Y PERFIL:
               - Conserva la línea de contacto exacta (Ubicación | Email | Teléfono | LinkedIn).
               - Redacta el resumen profesional inmediatamente después del contacto (sin título previo), en primera persona, destacando el perfil del candidato adaptado al rol.
            2. EXPERIENCIA EN PROYECTOS:
               - Usa el título "PROYECTO DESTACADO" o "EXPERIENCIA EN PROYECTOS".
               - Encabezado: [Nombre] | [Año].
               - Viñetas en primera persona ("Desarrollé", "Implementé", "Configuré") estructuradas como [Verbo de Acción] + [Tecnología] + [Impacto/Resultado].
               - Conserva la viñeta final de Tecnologías y los enlaces a GitHub.
            3. EDUCACIÓN Y EDUCACIÓN COMPLEMENTARIA:
               - Mantén la institución y las viñetas estructuradas como: [Título/Carrera] | [Porcentaje/Estado] | [Periodo].
            4. HABILIDADES TÉCNICAS (AGRUPADAS):
               - Agrupa en categorías: "Programación", "Bases de datos", "Herramientas", "Control de versiones", "Metodologías ágiles". Prioriza en cada categoría las tecnologías afines a la oferta.
            5. IDIOMAS: Conserva el nivel real (ej. "Inglés (B1)").

            Responde estrictamente en formato JSON con la siguiente estructura:
            {
              "adaptationNotes": "Notas de adaptación",
              "adaptedResume": {
                "fullName": "Juan Perez",
                "contactInfo": "Moreno, Buenos Aires | email@gmail.com | +54 9 1112344465 | linkedin.com/in/juanperez",
                "professionalSummary": "Texto del perfil adaptado en primera persona...",
                "projects": [
                  {
                    "name": "ForoHub - REST API Backend",
                    "year": "2026",
                    "overviewParagraphs": ["Desarrollé una API REST..."],
                    "highlights": ["Implementé...", "Incorporé...", "Dockericé..."],
                    "technologiesUsed": ["Java", "Spring Boot", "PostgreSQL", "Flyway", "Spring Security", "JWT", "JUnit", "Docker", "GitHub Actions", "Maven"],
                    "repoLink": "GitHub: forohub",
                    "moreProjectsLink": "Más proyectos en github.com/juanperez"
                  }
                ],
                "experiences": [],
                "education": [
                  {
                    "institution": "Universidad Nacional de General Sarmiento",
                    "items": [
                      "Licenciatura en Sistemas | 81% aprobado | Mar. 2019 - Actualidad",
                      "Tecnicatura Universitaria en Informática | Graduada | Ago. 2025"
                    ]
                  }
                ],
                "complementaryEducation": [
                  {
                    "title": "PROGRAMA ONE: TECH FOUNDATION - Especialización Backend",
                    "entity": "Oracle Next Education",
                    "period": "Jul. 2025 – Mar. 2026"
                  }
                ],
                "technicalSkillsCategories": [
                  {"category": "Programación", "skills": "Java, Spring Boot"},
                  {"category": "Bases de datos", "skills": "PostgreSQL, MySQL"},
                  {"category": "Herramientas", "skills": "Postman, Swagger, Excel, Word"},
                  {"category": "Control de versiones", "skills": "Git, GitHub"},
                  {"category": "Metodologías ágiles", "skills": "Scrum"}
                ],
                "technicalSkills": ["Java", "Spring Boot", "PostgreSQL", "MySQL", "Git", "Docker"],
                "languages": ["Inglés (B1)"]
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