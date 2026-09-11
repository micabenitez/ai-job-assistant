package com.aijobassistant.ai.matching;

import com.aijobassistant.ai.resume.Resume;
import com.aijobassistant.ai.resume.model.StructuredResumeData;
import com.aijobassistant.ai.shared.exception.HallucinationDetectedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AntiHallucinationValidatorTest {

    private AntiHallucinationValidator validator;

    @BeforeEach
    void setUp() {
        validator = new AntiHallucinationValidator();
    }

    @Test
    @DisplayName("Debe aceptar el CV adaptado cuando todas las skills están respaldadas por el texto original")
    void shouldPassWhenAllSkillsAreBackedByOriginalText() {
        Resume originalResume = new Resume("cv.pdf", "Experiencia con Java, Spring Boot y PostgreSQL.");

        StructuredResumeData adaptedData = new StructuredResumeData(
                "Micaela Benitez",
                "Moreno, Buenos Aires | mabenittez@gmail.com",
                "Resumen profesional...",
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                List.of("Java", "PostgreSQL"),
                Collections.emptyList()
        );

        assertDoesNotThrow(() -> validator.validate(originalResume, adaptedData));
    }

    @Test
    @DisplayName("Debe lanzar HallucinationDetectedException cuando se incluye una tecnología inexistente en el CV original")
    void shouldThrowWhenSkillIsNotPresentInOriginalText() {
        Resume originalResume = new Resume("cv.pdf", "Experiencia con Java y Spring Boot.");

        StructuredResumeData adaptedDataWithHallucination = new StructuredResumeData(
                "Micaela Benitez",
                "Moreno, Buenos Aires | mabenittez@gmail.com",
                "Resumen profesional...",
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                List.of("Java", "PostgreSQL"),
                Collections.emptyList()
        );

        HallucinationDetectedException ex = assertThrows(
                HallucinationDetectedException.class,
                () -> validator.validate(originalResume, adaptedDataWithHallucination)
        );

        assertTrue(ex.getUnverifiedSkills().contains("PostgreSQL"));
    }
}