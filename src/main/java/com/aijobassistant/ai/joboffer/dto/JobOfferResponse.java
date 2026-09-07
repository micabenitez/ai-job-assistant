package com.aijobassistant.ai.joboffer.dto;

import com.aijobassistant.ai.joboffer.JobOffer;

import java.time.Instant;
import java.util.UUID;

public record JobOfferResponse(
        UUID id,
        String title,
        String company,
        int rawTextLength,
        Instant createdAt
) {
    public static JobOfferResponse fromEntity(JobOffer jobOffer) {
        return new JobOfferResponse(
                jobOffer.getId(),
                jobOffer.getTitle(),
                jobOffer.getCompany(),
                jobOffer.getRawText() != null ? jobOffer.getRawText().length() : 0,
                jobOffer.getCreatedAt()
        );
    }
}