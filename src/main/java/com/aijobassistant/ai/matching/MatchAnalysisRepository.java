package com.aijobassistant.ai.matching;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MatchAnalysisRepository extends JpaRepository<MatchAnalysis, UUID> {
    List<MatchAnalysis> findByResumeId(UUID resumeId);
}