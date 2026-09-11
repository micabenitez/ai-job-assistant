package com.aijobassistant.ai.shared.exception;

import java.util.List;

/**
 * Lanzada cuando el proceso de adaptación intenta incorporar tecnologías o datos
 * no respaldados por la información original provista por el candidato.
 */
public class HallucinationDetectedException extends RuntimeException {

    private final List<String> unverifiedSkills;

    public HallucinationDetectedException(String message, List<String> unverifiedSkills) {
        super(message);
        this.unverifiedSkills = unverifiedSkills;
    }

    public List<String> getUnverifiedSkills() {
        return unverifiedSkills;
    }
}