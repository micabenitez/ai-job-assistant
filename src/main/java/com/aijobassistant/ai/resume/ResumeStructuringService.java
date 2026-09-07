package com.aijobassistant.ai.resume;

import com.aijobassistant.ai.resume.model.StructuredResumeData;
import com.aijobassistant.ai.shared.domain.LlmGateway;
import com.aijobassistant.ai.shared.exception.DocumentProcessingException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ResumeStructuringService {

    private static final String SYSTEM_PROMPT = """
            Eres un analizador técnico y estricto de currículums vitae.
            Tu función es extraer la información contenida en el CV y devolverla estrictamente en formato JSON válido.
            
            REGLA DE ORO INQUEBRANTABLE:
            NO INVENTES NADA. Si una información (tecnología, empresa, certificación) no está explícitamente escrita en el texto, NO la asumas ni la agregues.
            
            Debes responder exclusivamente con el siguiente esquema JSON:
            {
              "fullName": "Nombre y apellido o null",
              "professionalSummary": "Resumen o null",
              "experiences": [
                {
                  "role": "Puesto",
                  "company": "Empresa",
                  "duration": "Fechas o periodo",
                  "responsibilities": ["responsabilidad 1"],
                  "technologiesUsed": ["tecnología 1"]
                }
              ],
              "education": [
                {
                  "degree": "Título académico",
                  "institution": "Institución educativa",
                  "year": "Año de finalización o cursada"
                }
              ],
              "technicalSkills": ["skill técnica explícitamente mencionada"],
              "softSkills": ["habilidad blanda"],
              "languages": ["idioma y nivel si figura"],
              "certificaciones": ["certificación o curso"]
            }
            """;

    private final ResumeRepository resumeRepository;
    private final LlmGateway llmGateway;
    private final ObjectMapper objectMapper;

    public ResumeStructuringService(ResumeRepository resumeRepository, LlmGateway llmGateway, ObjectMapper objectMapper) {
        this.resumeRepository = resumeRepository;
        this.llmGateway = llmGateway;
        this.objectMapper = objectMapper.copy()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Transactional
    public StructuredResumeData structureResume(UUID resumeId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new DocumentProcessingException("No se encontró el CV con ID: " + resumeId, null));

        String userPrompt = "Analiza el siguiente texto de currículum y extrae los datos requeridos:\n\n" + resume.getRawText();

        String rawJsonResponse = llmGateway.generateStructuredResponse(SYSTEM_PROMPT, userPrompt);

        try {
            StructuredResumeData structuredData = objectMapper.readValue(rawJsonResponse, StructuredResumeData.class);
            resume.updateStructuredData(rawJsonResponse);
            resumeRepository.save(resume);
            return structuredData;
        } catch (JsonProcessingException e) {
            throw new DocumentProcessingException("Error al deserializar la respuesta estructurada del CV emitida por el LLM.", e);
        }
    }
}