package com.aijobassistant.ai.resume;

import com.aijobassistant.ai.resume.model.StructuredResumeData;
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
class ResumeStructuringServiceTest {

    @Mock
    private ResumeRepository resumeRepository;

    @Mock
    private LlmGateway llmGateway;

    private ResumeStructuringService structuringService;

    @BeforeEach
    void setUp() {
        structuringService = new ResumeStructuringService(resumeRepository, llmGateway, new ObjectMapper());
    }

    @Test
    @DisplayName("Debe transformar el rawText del CV en un objeto StructuredResumeData tipado y actualizar la entidad")
    void shouldStructureResumeSuccessfully() {
        // Arrange
        UUID resumeId = UUID.randomUUID();
        Resume resume = new Resume("cv.pdf", "Micaela Benitez - Java Backend Trainee con Spring Boot");

        String fakeLlmJson = """
                {
                  "fullName": "Micaela Benitez",
                  "professionalSummary": "Java Backend Trainee",
                  "experiences": [],
                  "education": [],
                  "technicalSkills": ["Java", "Spring Boot"],
                  "softSkills": ["Trabajo en equipo"],
                  "languages": ["Ingles B1"],
                  "certifications": []
                }
                """;

        when(resumeRepository.findById(resumeId)).thenReturn(Optional.of(resume));
        when(llmGateway.generateStructuredResponse(anyString(), anyString())).thenReturn(fakeLlmJson);
        when(resumeRepository.save(any(Resume.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        StructuredResumeData result = structuringService.structureResume(resumeId);

        // Assert
        assertNotNull(result);
        assertEquals("Micaela Benitez", result.fullName());
        assertEquals(2, result.technicalSkills().size());
        assertTrue(result.technicalSkills().contains("Java"));
        assertTrue(result.technicalSkills().contains("Spring Boot"));
        assertEquals(fakeLlmJson, resume.getStructuredData());

        verify(resumeRepository, times(1)).save(resume);
    }
}