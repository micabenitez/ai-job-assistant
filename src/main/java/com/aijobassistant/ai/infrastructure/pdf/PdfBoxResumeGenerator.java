package com.aijobassistant.ai.infrastructure.pdf;

import com.aijobassistant.ai.resume.model.StructuredResumeData;
import com.aijobassistant.ai.shared.exception.DocumentProcessingException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class PdfBoxResumeGenerator {

    private static final PDFont FONT_REGULAR = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    private static final PDFont FONT_BOLD = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

    private static final float PAGE_WIDTH = PDRectangle.A4.getWidth();
    private static final float MARGIN_LEFT = 48f;
    private static final float MARGIN_RIGHT = 48f;
    private static final float USABLE_WIDTH = PAGE_WIDTH - MARGIN_LEFT - MARGIN_RIGHT;

    public byte[] generatePdf(StructuredResumeData resume) {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream stream = new PDPageContentStream(document, page)) {
                float y = 800f;

                // 1. NOMBRE (Centrado, Grande)
                String name = (resume.fullName() != null && !resume.fullName().isBlank())
                        ? sanitize(resume.fullName())
                        : "Micaela Benitez";
                drawCenteredText(stream, name, FONT_BOLD, 16f, y);
                y -= 14f;

                // 2. CONTACTO (Centrado)
                if (resume.contactInfo() != null && !resume.contactInfo().isBlank()) {
                    drawCenteredText(stream, sanitize(resume.contactInfo()), FONT_REGULAR, 8.5f, y);
                    y -= 14f;
                }

                // 3. RESUMEN PROFESIONAL (En bloque)
                if (resume.professionalSummary() != null && !resume.professionalSummary().isBlank()) {
                    y = drawWrappedParagraph(stream, sanitize(resume.professionalSummary()), FONT_REGULAR, 8.8f, 11.8f, MARGIN_LEFT, USABLE_WIDTH, y);
                }

                // 4. PROYECTO DESTACADO
                if (resume.projects() != null && !resume.projects().isEmpty()) {
                    y = drawSectionTitle(stream, "EXPERIENCIA EN PROYECTOS", y);
                    for (var proj : resume.projects()) {
                        String projHeader = sanitize(proj.name()) + (proj.year() != null && !proj.year().isBlank() ? " | " + sanitize(proj.year()) : "");
                        drawText(stream, projHeader, FONT_BOLD, 9.5f, MARGIN_LEFT, y);
                        y -= 13f;

                        if (proj.overviewParagraphs() != null) {
                            for (String p : proj.overviewParagraphs()) {
                                String cleanP = sanitize(p);
                                // Si el párrafo empieza con verbo de acción típico o viñeta residual, dibujarlo como bullet
                                if (cleanP.startsWith("*") || cleanP.startsWith("-") || cleanP.startsWith("•")) {
                                    cleanP = cleanP.replaceFirst("^[\\*\\-•]\\s*", "");
                                    y = drawBulletPoint(stream, cleanP, FONT_REGULAR, 8.8f, 11.5f, MARGIN_LEFT, USABLE_WIDTH, y);
                                } else {
                                    y = drawWrappedParagraph(stream, cleanP, FONT_REGULAR, 8.8f, 11.5f, MARGIN_LEFT, USABLE_WIDTH, y);
                                }
                            }
                        }

                        if (proj.highlights() != null) {
                            for (String hl : proj.highlights()) {
                                String cleanHl = sanitize(hl).replaceFirst("^[\\*\\-•]\\s*", "");
                                y = drawBulletPoint(stream, cleanHl, FONT_REGULAR, 8.8f, 11.5f, MARGIN_LEFT, USABLE_WIDTH, y);
                            }
                        }

                        if (proj.technologiesUsed() != null && !proj.technologiesUsed().isEmpty()) {
                            String techLine = "Tecnologías: " + String.join(", ", proj.technologiesUsed());
                            y = drawBulletPoint(stream, sanitize(techLine), FONT_REGULAR, 8.8f, 11.5f, MARGIN_LEFT, USABLE_WIDTH, y);
                        }

                        if (proj.repoLink() != null && !proj.repoLink().isBlank()) {
                            drawText(stream, sanitize(proj.repoLink()), FONT_REGULAR, 8.8f, MARGIN_LEFT, y);
                            y -= 11f;
                        }
                        if (proj.moreProjectsLink() != null && !proj.moreProjectsLink().isBlank()) {
                            drawText(stream, sanitize(proj.moreProjectsLink()), FONT_REGULAR, 8.8f, MARGIN_LEFT, y);
                            y -= 11f;
                        }
                    }
                }

                // 5. EDUCACIÓN
                if (resume.education() != null && !resume.education().isEmpty()) {
                    y = drawSectionTitle(stream, "EDUCACIÓN", y);
                    for (var edu : resume.education()) {
                        drawText(stream, sanitize(edu.institution()), FONT_BOLD, 9.2f, MARGIN_LEFT, y);
                        y -= 12f;

                        if (edu.items() != null) {
                            for (String item : edu.items()) {
                                y = drawBulletPoint(stream, sanitize(item), FONT_REGULAR, 8.8f, 11.5f, MARGIN_LEFT, USABLE_WIDTH, y);
                            }
                        }
                    }
                }

                // 6. EDUCACIÓN COMPLEMENTARIA
                if (resume.complementaryEducation() != null && !resume.complementaryEducation().isEmpty()) {
                    y = drawSectionTitle(stream, "EDUCACIÓN COMPLEMENTARIA", y);
                    for (var comp : resume.complementaryEducation()) {
                        String compLine = String.format("%s  %s", sanitize(comp.title()), sanitize(comp.entity()));
                        y = drawBulletPoint(stream, compLine, FONT_REGULAR, 8.8f, 11.5f, MARGIN_LEFT, USABLE_WIDTH, y);
                        if (comp.period() != null && !comp.period().isBlank()) {
                            drawText(stream, "   " + sanitize(comp.period()), FONT_REGULAR, 8.5f, MARGIN_LEFT + 10f, y);
                            y -= 11f;
                        }
                    }
                }

                // 7. HABILIDADES TÉCNICAS
                if (resume.technicalSkillsCategories() != null && !resume.technicalSkillsCategories().isEmpty()) {
                    y = drawSectionTitle(stream, "HABILIDADES TÉCNICAS", y);
                    for (var cat : resume.technicalSkillsCategories()) {
                        drawText(stream, "• " + sanitize(cat.category()) + ":", FONT_BOLD, 8.8f, MARGIN_LEFT, y);
                        float catWidth = getTextWidth("• " + sanitize(cat.category()) + ": ", FONT_BOLD, 8.8f);
                        drawText(stream, sanitize(cat.skills()), FONT_REGULAR, 8.8f, MARGIN_LEFT + catWidth, y);
                        y -= 12f;
                    }
                }

                // 8. IDIOMAS
                if (resume.languages() != null && !resume.languages().isEmpty()) {
                    y = drawSectionTitle(stream, "IDIOMAS", y);
                    for (String lang : resume.languages()) {
                        drawText(stream, sanitize(lang), FONT_REGULAR, 8.8f, MARGIN_LEFT, y);
                        y -= 11f;
                    }
                }
            }

            document.save(baos);
            return baos.toByteArray();

        } catch (IOException e) {
            throw new DocumentProcessingException("Error al generar el documento PDF.", e);
        }
    }

    private float drawSectionTitle(PDPageContentStream stream, String title, float y) throws IOException {
        y -= 16f;

        drawText(stream, title.toUpperCase(), FONT_BOLD, 9.5f, MARGIN_LEFT, y);

        y -= 3.5f;

        stream.setLineWidth(0.5f);
        stream.moveTo(MARGIN_LEFT, y);
        stream.lineTo(PAGE_WIDTH - MARGIN_RIGHT, y);
        stream.stroke();

        return y - 14f;
    }

    private float drawBulletPoint(PDPageContentStream stream, String text, PDFont font, float fontSize, float leading, float x, float width, float y) throws IOException {
        drawText(stream, "•", font, fontSize, x, y);
        float textX = x + 10f;
        float availableWidth = width - 10f;
        return drawWrappedParagraph(stream, text, font, fontSize, leading, textX, availableWidth, y);
    }

    private float drawWrappedParagraph(PDPageContentStream stream, String text, PDFont font, float fontSize, float leading, float x, float width, float y) throws IOException {
        List<String> lines = wrapText(text, font, fontSize, width);
        for (String line : lines) {
            drawText(stream, line, font, fontSize, x, y);
            y -= leading;
        }
        return y;
    }

    private void drawText(PDPageContentStream stream, String text, PDFont font, float fontSize, float x, float y) throws IOException {
        stream.beginText();
        stream.setFont(font, fontSize);
        stream.newLineAtOffset(x, y);
        stream.showText(text);
        stream.endText();
    }

    private void drawCenteredText(PDPageContentStream stream, String text, PDFont font, float fontSize, float y) throws IOException {
        float textWidth = getTextWidth(text, font, fontSize);
        float x = (PAGE_WIDTH - textWidth) / 2f;
        drawText(stream, text, font, fontSize, x, y);
    }

    private float getTextWidth(String text, PDFont font, float fontSize) throws IOException {
        return (font.getStringWidth(text) / 1000f) * fontSize;
    }

    private List<String> wrapText(String text, PDFont font, float fontSize, float maxWidth) throws IOException {
        List<String> lines = new ArrayList<>();
        String[] words = text.split("\\s+");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            String candidate = currentLine.isEmpty() ? word : currentLine + " " + word;
            float candidateWidth = getTextWidth(candidate, font, fontSize);

            if (candidateWidth <= maxWidth) {
                currentLine = new StringBuilder(candidate);
            } else {
                if (!currentLine.isEmpty()) {
                    lines.add(currentLine.toString());
                }
                currentLine = new StringBuilder(word);
            }
        }

        if (!currentLine.isEmpty()) {
            lines.add(currentLine.toString());
        }

        return lines;
    }

    private String sanitize(String input) {
        if (input == null) return "";
        return input.replace("\u2022", "*")
                .replaceAll("[^\\x20-\\x7EáéíóúÁÉÍÓÚñÑüÜ°%–—()\\[\\],.:;\"'/?!@#&*+-]", "")
                .trim();
    }
}