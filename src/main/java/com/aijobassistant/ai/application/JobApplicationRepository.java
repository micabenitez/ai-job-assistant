package com.aijobassistant.ai.application.repository;

import com.aijobassistant.ai.application.model.ApplicationStatus;
import com.aijobassistant.ai.application.model.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JobApplicationRepository extends 
        JpaRepository<JobApplication, UUID> {

    @Query("SELECT j FROM JobApplication j " +
            "LEFT JOIN FETCH j.adaptedResume " +
            "LEFT JOIN FETCH j.matchAnalysis " +
            "WHERE (:status IS NULL OR j.status = :status) " +
            "AND (CAST(:search AS string) IS NULL OR (" +
            "  LOWER(j.companyName) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) " +
            "  OR LOWER(j.roleTitle) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))" +
            ")) " +
            "ORDER BY j.createdAt DESC")
    List<JobApplication> findAllWithFilters(@Param("status") ApplicationStatus status, @Param("search") String search);
}