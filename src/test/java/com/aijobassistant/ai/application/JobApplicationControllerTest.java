package com.aijobassistant.ai.application;

import com.aijobassistant.ai.application.dto.CreateJobApplicationRequest;
import com.aijobassistant.ai.application.dto.JobApplicationResponse;
import com.aijobassistant.ai.application.dto.UpdateJobApplicationRequest;
import com.aijobassistant.ai.application.model.ApplicationStatus;
import com.aijobassistant.ai.application.JobApplicationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(JobApplicationController.class)
class JobApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JobApplicationService jobApplicationService;

    @Test
    @DisplayName("GET /api/v1/applications - Debe retornar 200 OK y la lista de aplicaciones")
    void shouldReturnListOfApplications() throws Exception {
        UUID id = UUID.randomUUID();
        JobApplicationResponse response = new JobApplicationResponse(
                id,
                "Google",
                "Software Engineer",
                ApplicationStatus.APPLIED,
                92,
                Instant.now(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Referido",
                Instant.now()
        );

        when(jobApplicationService.listApplications(any(), any())).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/applications")
                        .param("status", "APPLIED")
                        .param("search", "Google"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].companyName").value("Google"))
                .andExpect(jsonPath("$[0].matchScore").value(92));
    }

    @Test
    @DisplayName("POST /api/v1/applications - 201 Created al enviar request válido")
    void shouldCreateApplicationSuccessfully() throws Exception {
        UUID id = UUID.randomUUID();
        CreateJobApplicationRequest request = new CreateJobApplicationRequest(
                "Mercado Libre",
                "Backend Trainee",
                ApplicationStatus.ADAPTED,
                85,
                UUID.randomUUID(),
                null,
                "Testing"
        );

        JobApplicationResponse response = new JobApplicationResponse(
                id,
                "Mercado Libre",
                "Backend Trainee",
                ApplicationStatus.ADAPTED,
                85,
                Instant.now(),
                request.adaptedResumeId(),
                null,
                "Testing",
                Instant.now()
        );

        when(jobApplicationService.createApplication(any(CreateJobApplicationRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.companyName").value("Mercado Libre"));
    }

    @Test
    @DisplayName("POST /api/v1/applications - 400 Bad Request si fallan las validaciones de Bean Validation")
    void shouldReturnBadRequestWhenInputIsInvalid() throws Exception {
        CreateJobApplicationRequest invalidRequest = new CreateJobApplicationRequest(
                "", // Blank companyName
                "Backend Trainee",
                ApplicationStatus.ADAPTED,
                150, // Score > 100
                null,
                null,
                null
        );

        mockMvc.perform(post("/api/v1/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /api/v1/applications/{id} - 200 OK al actualizar status o notas")
    void shouldPatchApplication() throws Exception {
        UUID id = UUID.randomUUID();
        UpdateJobApplicationRequest updateRequest = new UpdateJobApplicationRequest(
                ApplicationStatus.INTERVIEWING,
                "Entrevista agendada"
        );

        JobApplicationResponse response = new JobApplicationResponse(
                id,
                "Mercado Libre",
                "Backend Trainee",
                ApplicationStatus.INTERVIEWING,
                85,
                Instant.now(),
                null,
                null,
                "Entrevista agendada",
                Instant.now()
        );

        when(jobApplicationService.updateApplication(eq(id), any(UpdateJobApplicationRequest.class)))
                .thenReturn(response);

        mockMvc.perform(patch("/api/v1/applications/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INTERVIEWING"));
    }

    @Test
    @DisplayName("DELETE /api/v1/applications/{id} - 204 No Content")
    void shouldDeleteApplication() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(jobApplicationService).deleteApplication(id);

        mockMvc.perform(delete("/api/v1/applications/{id}", id))
                .andExpect(status().isNoContent());
    }
}