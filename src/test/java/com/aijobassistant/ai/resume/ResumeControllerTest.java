package com.aijobassistant.ai.resume;

import com.aijobassistant.ai.resume.dto.ResumeResponseDto;
import com.aijobassistant.ai.shared.exception.InvalidDocumentException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ResumeController.class)
class ResumeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ResumeIngestionService resumeIngestionService;

    @Test
    @DisplayName("POST /api/v1/resumes/upload debe retornar 201 Created cuando el archivo es válido")
    void shouldReturn201WhenUploadIsSuccessful() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "cv.pdf", "application/pdf", new byte[]{0x25, 0x50, 0x44, 0x46}
        );

        ResumeResponseDto mockResponse = new ResumeResponseDto(
                UUID.randomUUID(), "cv.pdf", 120, Instant.now()
        );

        when(resumeIngestionService.ingestResume(eq("cv.pdf"), any(byte[].class)))
                .thenReturn(mockResponse);

        mockMvc.perform(multipart("/api/v1/resumes/upload").file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(mockResponse.id().toString()))
                .andExpect(jsonPath("$.originalFilename").value("cv.pdf"))
                .andExpect(jsonPath("$.rawTextLength").value(120));
    }

    @Test
    @DisplayName("POST /api/v1/resumes/upload debe retornar 400 Bad Request cuando el archivo está vacío")
    void shouldReturn400WhenFileIsEmpty() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file", "empty.pdf", "application/pdf", new byte[0]
        );

        mockMvc.perform(multipart("/api/v1/resumes/upload").file(emptyFile))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Debe proporcionar un archivo PDF no vacío."));
    }
}