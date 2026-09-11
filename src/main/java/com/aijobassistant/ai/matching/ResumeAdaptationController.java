package com.aijobassistant.ai.matching;

import com.aijobassistant.ai.matching.dto.AdaptResumeRequest;
import com.aijobassistant.ai.matching.dto.AdaptedResumeResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/resumes/adapt")
public class ResumeAdaptationController {

    private final ResumeAdaptationService adaptationService;

    public ResumeAdaptationController(ResumeAdaptationService adaptationService) {
        this.adaptationService = adaptationService;
    }

    @PostMapping
    public ResponseEntity<AdaptedResumeResponse> adaptResume(@Valid @RequestBody AdaptResumeRequest request) {
        AdaptedResumeResponse response = adaptationService.adaptResume(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}