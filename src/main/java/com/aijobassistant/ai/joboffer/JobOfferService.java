package com.aijobassistant.ai.joboffer;

import com.aijobassistant.ai.joboffer.dto.CreateJobOfferRequest;
import com.aijobassistant.ai.joboffer.dto.JobOfferResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class JobOfferService {

    private final JobOfferRepository jobOfferRepository;

    public JobOfferService(JobOfferRepository jobOfferRepository) {
        this.jobOfferRepository = jobOfferRepository;
    }

    @Transactional
    public JobOfferResponse createJobOffer(CreateJobOfferRequest request) {
        JobOffer jobOffer = new JobOffer(
                request.rawText().strip(),
                request.title() != null ? request.title().strip() : null,
                request.company() != null ? request.company().strip() : null
        );

        JobOffer savedJobOffer = jobOfferRepository.save(jobOffer);
        return JobOfferResponse.fromEntity(savedJobOffer);
    }
}