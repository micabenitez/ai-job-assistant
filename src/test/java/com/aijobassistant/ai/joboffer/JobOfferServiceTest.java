package com.aijobassistant.ai.joboffer;

import com.aijobassistant.ai.joboffer.dto.CreateJobOfferRequest;
import com.aijobassistant.ai.joboffer.dto.JobOfferResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobOfferServiceTest {

    @Mock
    private JobOfferRepository jobOfferRepository;

    private JobOfferService jobOfferService;

    @BeforeEach
    void setUp() {
        jobOfferService = new JobOfferService(jobOfferRepository);
    }

    @Test
    @DisplayName("Debe persistir la oferta laboral y retornar el DTO correspondiente")
    void shouldCreateJobOfferSuccessfully() {
        CreateJobOfferRequest request = new CreateJobOfferRequest(
                "Buscamos desarrollador Java con conocimientos en Spring Boot y PostgreSQL.",
                "Java Backend Trainee",
                "Tech Solutions"
        );

        when(jobOfferRepository.save(any(JobOffer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        JobOfferResponse response = jobOfferService.createJobOffer(request);

        assertNotNull(response);
        assertNotNull(response.id());
        assertEquals("Java Backend Trainee", response.title());
        assertEquals("Tech Solutions", response.company());
        assertTrue(response.rawTextLength() > 0);

        verify(jobOfferRepository, times(1)).save(any(JobOffer.class));
    }
}