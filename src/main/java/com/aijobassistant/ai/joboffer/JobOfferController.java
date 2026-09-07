package com.aijobassistant.ai.joboffer;

import com.aijobassistant.ai.joboffer.dto.CreateJobOfferRequest;
import com.aijobassistant.ai.joboffer.dto.JobOfferResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/job-offers")
public class JobOfferController {

    private final JobOfferService jobOfferService;

    public JobOfferController(JobOfferService jobOfferService) {
        this.jobOfferService = jobOfferService;
    }

    @PostMapping
    public ResponseEntity<JobOfferResponse> createJobOffer(@Valid @RequestBody CreateJobOfferRequest request) {
        JobOfferResponse response = jobOfferService.createJobOffer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}