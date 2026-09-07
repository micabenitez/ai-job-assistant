package com.aijobassistant.ai.matching;

import com.aijobassistant.ai.matching.dto.AnalyzeMatchRequest;
import com.aijobassistant.ai.matching.dto.MatchAnalysisResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/matches")
public class MatchAnalysisController {

    private final MatchAnalysisService matchAnalysisService;

    public MatchAnalysisController(MatchAnalysisService matchAnalysisService) {
        this.matchAnalysisService = matchAnalysisService;
    }

    @PostMapping("/analyze")
    public ResponseEntity<MatchAnalysisResponse> analyzeMatch(@Valid @RequestBody AnalyzeMatchRequest request) {
        MatchAnalysisResponse response = matchAnalysisService.analyze(request);
        return ResponseEntity.ok(response);
    }
}