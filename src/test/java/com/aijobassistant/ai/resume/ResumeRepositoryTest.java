package com.aijobassistant.ai.resume;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ResumeRepositoryTest {

    @Autowired
    private ResumeRepository resumeRepository;

    @Test
    @DisplayName("Debe persistir un Resume y recuperarlo por ID con sus datos intactos")
    void shouldSaveAndFindResumeById() {
        // Arrange
        String filename = "cv-candidato.pdf";
        String rawText = "Juan Perez - Desarrollador Java Backend";
        Resume resume = new Resume(filename, rawText);

        // Act
        Resume savedResume = resumeRepository.save(resume);
        Optional<Resume> retrievedResume = resumeRepository.findById(savedResume.getId());

        // Assert
        assertTrue(retrievedResume.isPresent());
        assertEquals(savedResume.getId(), retrievedResume.get().getId());
        assertEquals(filename, retrievedResume.get().getOriginalFilename());
        assertEquals(rawText, retrievedResume.get().getRawText());
        assertNotNull(retrievedResume.get().getCreatedAt());
    }
}