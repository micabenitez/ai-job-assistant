package com.aijobassistant.ai.joboffer;

import com.aijobassistant.ai.joboffer.model.StructuredJobOfferRequirements;
import com.aijobassistant.ai.shared.domain.LlmGateway;
import com.aijobassistant.ai.shared.exception.DocumentProcessingException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class JobOfferStructuringService {

    private static final String SYSTEM_PROMPT = """
            Eres un analizador técnico de ofertas de empleo en tecnología.
            Tu función es analizar la descripción de una vacante y extraer sus requerimientos de manera estructurada en formato JSON.
            
            Debes responder exclusivamente con el siguiente esquema JSON:
            {
              "roleTitle": "Título del puesto detectado",
              "seniorityLevel": "Trainee / Junior / Semi-Senior / Senior / No especificado",
              "requiredTechnicalSkills": ["tecnología obligatoria/excluyente"],
              "optionalTechnicalSkills": ["tecnología deseable/plus"],
              "softSkills": ["habilidad blanda requerida"],
              "responsibilities": ["responsabilidad principal"],
              "languages": ["idioma requerido"]
            }
            """;

    private final JobOfferRepository jobOfferRepository;
    private final LlmGateway llmGateway;
    private final ObjectMapper objectMapper;

    public JobOfferStructuringService(JobOfferRepository jobOfferRepository, LlmGateway llmGateway, ObjectMapper objectMapper) {
        this.jobOfferRepository = jobOfferRepository;
        this.llmGateway = llmGateway;
        this.objectMapper = objectMapper.copy()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Transactional
    public StructuredJobOfferRequirements structureJobOffer(UUID jobOfferId) {
        JobOffer jobOffer = jobOfferRepository.findById(jobOfferId)
                .orElseThrow(() -> new DocumentProcessingException("No se encontró la oferta laboral con ID: " + jobOfferId, null));

        String userPrompt = "Analiza la siguiente oferta laboral y extrae los requisitos:\n\n" + jobOffer.getRawText();

        String rawJsonResponse = llmGateway.generateStructuredResponse(SYSTEM_PROMPT, userPrompt);

        try {
            StructuredJobOfferRequirements requirements = objectMapper.readValue(rawJsonResponse, StructuredJobOfferRequirements.class);
            jobOffer.updateStructuredRequirements(rawJsonResponse);
            jobOfferRepository.save(jobOffer);
            return requirements;
        } catch (JsonProcessingException e) {
            throw new DocumentProcessingException("Error al deserializar los requerimientos de la oferta emitidos por el LLM.", e);
        }
    }
}