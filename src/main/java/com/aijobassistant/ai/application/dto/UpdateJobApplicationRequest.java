package com.aijobassistant.ai.application.dto;

import com.aijobassistant.ai.application.model.ApplicationStatus;

public record UpdateJobApplicationRequest(
        ApplicationStatus status,
        String notes
) {}