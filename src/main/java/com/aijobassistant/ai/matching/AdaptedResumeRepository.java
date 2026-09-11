package com.aijobassistant.ai.matching;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AdaptedResumeRepository extends JpaRepository<AdaptedResume, UUID> {
    Optional<AdaptedResume> findByMatchAnalysisId(UUID matchAnalysisId);
}