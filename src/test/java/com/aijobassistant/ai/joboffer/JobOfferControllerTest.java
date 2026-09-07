package com.aijobassistant.ai.joboffer;

import com.aijobassistant.ai.joboffer.dto.CreateJobOfferRequest;
import com.aijobassistant.ai.joboffer.dto.JobOfferResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(JobOfferController.class)
class JobOfferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JobOfferService jobOfferService;

    @Test
    @DisplayName("POST /api/v1/job-offers debe retornar 201 Created cuando el payload es válido")
    void shouldReturn201WhenPayloadIsValid() throws Exception {
        CreateJobOfferRequest request = new CreateJobOfferRequest(
                "Se busca desarrollador Java Junior con experiencia en APIs REST y bases de datos relacionales.",
                "Java Developer",
                "Empresa SRL"
        );

        JobOfferResponse response = new JobOfferResponse(
                UUID.randomUUID(),
                "Java Developer",
                "Empresa SRL",
                request.rawText().length(),
                Instant.now()
        );

        when(jobOfferService.createJobOffer(any(CreateJobOfferRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/job-offers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(response.id().toString()))
                .andExpect(jsonPath("$.title").value("Java Developer"))
                .andExpect(jsonPath("$.company").value("Empresa SRL"));
    }

    @Test
    @DisplayName("POST /api/v1/job-offers debe retornar 400 Bad Request cuando la descripción es menor a 30 caracteres")
    void shouldReturn400WhenDescriptionIsTooShort() throws Exception {
        CreateJobOfferRequest invalidRequest = new CreateJobOfferRequest(
                "Texto corto",
                "Puesto",
                "Empresa"
        );

        mockMvc.perform(post("/api/v1/job-offers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.errors.rawText").exists());
    }
}