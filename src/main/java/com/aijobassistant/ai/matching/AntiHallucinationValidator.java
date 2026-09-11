package com.aijobassistant.ai.matching;

import com.aijobassistant.ai.resume.Resume;
import com.aijobassistant.ai.resume.model.StructuredResumeData;
import com.aijobassistant.ai.shared.exception.HallucinationDetectedException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class AntiHallucinationValidator {

    /**
     * Valida que ninguna habilidad técnica presente en el CV adaptado haya sido inventada.
     * Toda skill técnica debe encontrarse presente en el texto original del CV.
     */
    public void validate(Resume originalResume, StructuredResumeData adaptedData) {
        if (adaptedData == null || adaptedData.technicalSkills() == null) {
            return;
        }

        String rawOriginalTextLower = originalResume.getRawText().toLowerCase(Locale.ROOT);
        List<String> fabricatedSkills = new ArrayList<>();

        for (String skill : adaptedData.technicalSkills()) {
            if (skill == null || skill.isBlank()) continue;

            String normalizedSkill = skill.toLowerCase(Locale.ROOT).trim();

            // Verificación determinística: la tecnología debe existir en el texto original
            if (!rawOriginalTextLower.contains(normalizedSkill)) {
                fabricatedSkills.add(skill);
            }
        }

        if (!fabricatedSkills.isEmpty()) {
            throw new HallucinationDetectedException(
                    "Se detectaron tecnologías agregadas que no figuran en el CV original provisto por el usuario.",
                    fabricatedSkills
            );
        }
    }
}