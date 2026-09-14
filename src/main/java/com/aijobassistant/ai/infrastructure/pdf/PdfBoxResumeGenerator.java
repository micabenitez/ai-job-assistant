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
import org.apache.pdfbox.pdmodel.interactive.action.PDActionURI;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDBorderStyleDictionary;
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
    private static final float PAGE_HEIGHT = PDRectangle.A4.getHeight();

    // Márgenes más seguros para ATS e impresión
    private static final float MARGIN_LEFT = 50f;
    private static final float MARGIN_RIGHT = 50f;
    private static final float MARGIN_TOP = 48f;
    private static final float MARGIN_BOTTOM = 45f;
    private static final float USABLE_WIDTH = PAGE_WIDTH - MARGIN_LEFT - MARGIN_RIGHT;

    // Tamaños de fuente ATS-friendly
    private static final float FONT_NAME = 15f;
    private static final float FONT_CONTACT = 9.5f;
    private static final float FONT_SECTION = 11f;
    private static final float FONT_BODY = 10f;
    private static final float FONT_SMALL = 9.5f;

    // Interlineado
    private static final float LEADING_BODY = 13f;
    private static final float LEADING_TIGHT = 12f;

    public byte[] generatePdf(StructuredResumeData resume) {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDPageContentStream stream = new PDPageContentStream(document, page);
            float y = PAGE_HEIGHT - MARGIN_TOP;

            try {
                // 1. NOMBRE
                String name = (resume.fullName() != null && !resume.fullName().isBlank())
                        ? sanitize(resume.fullName())
                        : "Nombre del candidato";
                y = drawCenteredText(stream, name, FONT_BOLD, FONT_NAME, y);
                y -= 14f;

                // 2. CONTACTO
                if (resume.contactInfo() != null && !resume.contactInfo().isBlank()) {
                    y = drawCenteredText(stream, sanitize(resume.contactInfo()), FONT_REGULAR, FONT_CONTACT, y);
                    y -= 16f;
                }

                // 3. RESUMEN PROFESIONAL
                if (resume.professionalSummary() != null && !resume.professionalSummary().isBlank()) {
                    y = drawWrappedParagraph(stream, sanitize(resume.professionalSummary()),
                            FONT_REGULAR, FONT_BODY, LEADING_BODY, MARGIN_LEFT, USABLE_WIDTH, y);
                    y -= 8f;
                }

                // 4. EXPERIENCIA / PROYECTOS
                if (resume.projects() != null && !resume.projects().isEmpty()) {
                    y = ensureSpace(document, stream, y, 80f);
                    y = drawSectionTitle(stream, "EXPERIENCIA EN PROYECTOS", y);

                    for (var proj : resume.projects()) {
                        y = ensureSpace(document, stream, y, 60f);

                        // Título del proyecto
                        String projHeader = sanitize(proj.name());
                        if (proj.year() != null && !proj.year().isBlank()) {
                            projHeader += " | " + sanitize(proj.year());
                        }
                        drawText(stream, projHeader, FONT_BOLD, 10.5f, MARGIN_LEFT, y);
                        y -= 14f;

                        // Párrafos de overview
                        if (proj.overviewParagraphs() != null) {
                            for (String p : proj.overviewParagraphs()) {
                                String clean = sanitize(p).replaceFirst("^[\\*\\-•]\\s*", "");
                                if (clean.isBlank()) continue;

                                if (looksLikeBullet(p)) {
                                    y = drawBulletPoint(stream, clean, FONT_REGULAR, FONT_BODY, LEADING_BODY, MARGIN_LEFT, USABLE_WIDTH, y);
                                } else {
                                    y = drawWrappedParagraph(stream, clean, FONT_REGULAR, FONT_BODY, LEADING_BODY, MARGIN_LEFT, USABLE_WIDTH, y);
                                }
                            }
                        }

                        // Highlights (viñetas)
                        if (proj.highlights() != null) {
                            for (String hl : proj.highlights()) {
                                String clean = sanitize(hl).replaceFirst("^[\\*\\-•]\\s*", "");
                                if (!clean.isBlank()) {
                                    y = drawBulletPoint(stream, clean, FONT_REGULAR, FONT_BODY, LEADING_BODY, MARGIN_LEFT, USABLE_WIDTH, y);
                                }
                            }
                        }

                        // Tecnologías
                        if (proj.technologiesUsed() != null && !proj.technologiesUsed().isEmpty()) {
                            String techLine = "Tecnologías: " + String.join(", ", proj.technologiesUsed());
                            y = drawWrappedParagraph(stream, sanitize(techLine),
                                    FONT_REGULAR, FONT_SMALL, LEADING_TIGHT, MARGIN_LEFT, USABLE_WIDTH, y);
                            y -= 2f;
                        }

                        // Links del proyecto
                        if (proj.repoLink() != null && !proj.repoLink().isBlank()) {
                            String repo = sanitize(proj.repoLink());
                            drawText(stream, repo, FONT_REGULAR, FONT_SMALL, MARGIN_LEFT, y);

                            if (repo.startsWith("http") || repo.contains("github.com")) {
                                addLink(document, page, repo, repo, MARGIN_LEFT, y, FONT_SMALL);
                            }
                            y -= 12f;
                        }
                        if (proj.moreProjectsLink() != null && !proj.moreProjectsLink().isBlank()) {
                            drawText(stream, sanitize(proj.moreProjectsLink()), FONT_REGULAR, FONT_SMALL, MARGIN_LEFT, y);
                            y -= 12f;
                        }

                        y -= 6f;
                    }
                }

                // 5. EDUCACIÓN
                if (resume.education() != null && !resume.education().isEmpty()) {
                    y = ensureSpace(document, stream, y, 50f);
                    y = drawSectionTitle(stream, "EDUCACIÓN", y);

                    for (var edu : resume.education()) {
                        drawText(stream, sanitize(edu.institution()), FONT_BOLD, 10f, MARGIN_LEFT, y);
                        y -= 13f;

                        if (edu.items() != null) {
                            for (String item : edu.items()) {
                                y = drawBulletPoint(stream, sanitize(item), FONT_REGULAR, FONT_BODY, LEADING_BODY, MARGIN_LEFT, USABLE_WIDTH, y);
                            }
                        }
                        y -= 4f;
                    }
                }

                // 6. EDUCACIÓN COMPLEMENTARIA
                if (resume.complementaryEducation() != null && !resume.complementaryEducation().isEmpty()) {
                    y = ensureSpace(document, stream, y, 40f);
                    y = drawSectionTitle(stream, "EDUCACIÓN COMPLEMENTARIA", y);

                    for (var comp : resume.complementaryEducation()) {
                        String title = sanitize(comp.title());
                        String entity = sanitize(comp.entity());
                        String line = title + (entity.isBlank() ? "" : " | " + entity);

                        y = drawBulletPoint(stream, line, FONT_REGULAR, FONT_BODY, LEADING_BODY, MARGIN_LEFT, USABLE_WIDTH, y);

                        if (comp.period() != null && !comp.period().isBlank()) {
                            drawText(stream, sanitize(comp.period()), FONT_REGULAR, FONT_SMALL, MARGIN_LEFT + 12f, y);
                            y -= 12f;
                        }
                    }
                }

                // 7. HABILIDADES TÉCNICAS
                if (resume.technicalSkillsCategories() != null && !resume.technicalSkillsCategories().isEmpty()) {
                    y = ensureSpace(document, stream, y, 40f);
                    y = drawSectionTitle(stream, "HABILIDADES TÉCNICAS", y);

                    for (var cat : resume.technicalSkillsCategories()) {
                        String category = sanitize(cat.category());
                        String skills = sanitize(cat.skills());

                        String label = category + ": ";
                        drawText(stream, label, FONT_BOLD, FONT_BODY, MARGIN_LEFT, y);
                        float labelWidth = getTextWidth(label, FONT_BOLD, FONT_BODY);

                        float remaining = USABLE_WIDTH - labelWidth;
                        if (getTextWidth(skills, FONT_REGULAR, FONT_BODY) <= remaining) {
                            drawText(stream, skills, FONT_REGULAR, FONT_BODY, MARGIN_LEFT + labelWidth, y);
                            y -= LEADING_BODY;
                        } else {
                            y -= LEADING_BODY;
                            y = drawWrappedParagraph(stream, skills, FONT_REGULAR, FONT_BODY, LEADING_BODY,
                                    MARGIN_LEFT + 10f, USABLE_WIDTH - 10f, y);
                        }
                    }
                }

                // 8. IDIOMAS
                if (resume.languages() != null && !resume.languages().isEmpty()) {
                    y = ensureSpace(document, stream, y, 30f);
                    y = drawSectionTitle(stream, "IDIOMAS", y);

                    for (String lang : resume.languages()) {
                        drawText(stream, sanitize(lang), FONT_REGULAR, FONT_BODY, MARGIN_LEFT, y);
                        y -= LEADING_BODY;
                    }
                }

            } finally {
                stream.close();
            }

            document.save(baos);
            return baos.toByteArray();

        } catch (IOException e) {
            throw new DocumentProcessingException("Error al generar el documento PDF.", e);
        }
    }

    private float ensureSpace(PDDocument document, PDPageContentStream stream, float y, float needed) throws IOException {
        if (y - needed < MARGIN_BOTTOM) {
            stream.close();
            PDPage newPage = new PDPage(PDRectangle.A4);
            document.addPage(newPage);
            return PAGE_HEIGHT - MARGIN_TOP;
        }
        return y;
    }

    private float drawSectionTitle(PDPageContentStream stream, String title, float y) throws IOException {
        y -= 16f;
        drawText(stream, title.toUpperCase(), FONT_BOLD, FONT_SECTION, MARGIN_LEFT, y);
        y -= 4f;

        stream.setLineWidth(0.6f);
        stream.setStrokingColor(0.3f);
        stream.moveTo(MARGIN_LEFT, y);
        stream.lineTo(PAGE_WIDTH - MARGIN_RIGHT, y);
        stream.stroke();
        stream.setStrokingColor(0f);

        return y - 12f;
    }

    private float drawBulletPoint(PDPageContentStream stream, String text, PDFont font,
                                  float fontSize, float leading, float x, float width, float y) throws IOException {
        drawText(stream, "•", font, fontSize, x, y);
        return drawWrappedParagraph(stream, text, font, fontSize, leading, x + 12f, width - 12f, y);
    }

    private float drawWrappedParagraph(PDPageContentStream stream, String text, PDFont font,
                                       float fontSize, float leading, float x, float width, float y) throws IOException {
        List<String> lines = wrapText(text, font, fontSize, width);
        for (String line : lines) {
            drawText(stream, line, font, fontSize, x, y);
            y -= leading;
        }
        return y;
    }

    private float drawCenteredText(PDPageContentStream stream, String text, PDFont font, float fontSize, float y) throws IOException {
        float textWidth = getTextWidth(text, font, fontSize);
        float x = (PAGE_WIDTH - textWidth) / 2f;
        drawText(stream, text, font, fontSize, x, y);
        return y;
    }

    private void drawText(PDPageContentStream stream, String text, PDFont font, float fontSize, float x, float y) throws IOException {
        stream.beginText();
        stream.setFont(font, fontSize);
        stream.newLineAtOffset(x, y);
        stream.showText(text);
        stream.endText();
    }

    private float getTextWidth(String text, PDFont font, float fontSize) throws IOException {
        return font.getStringWidth(text) / 1000f * fontSize;
    }

    private List<String> wrapText(String text, PDFont font, float fontSize, float maxWidth) throws IOException {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isBlank()) return lines;

        String[] words = text.split("\\s+");
        StringBuilder current = new StringBuilder();

        for (String word : words) {
            String candidate = current.isEmpty() ? word : current + " " + word;
            if (getTextWidth(candidate, font, fontSize) <= maxWidth) {
                current = new StringBuilder(candidate);
            } else {
                if (!current.isEmpty()) {
                    lines.add(current.toString());
                }
                current = new StringBuilder(word);
            }
        }
        if (!current.isEmpty()) {
            lines.add(current.toString());
        }
        return lines;
    }

    private boolean looksLikeBullet(String text) {
        if (text == null) return false;
        String t = text.trim();
        return t.startsWith("•") || t.startsWith("-") || t.startsWith("*");
    }

    private String sanitize(String input) {
        if (input == null) return "";
        return input
                .replace('\u00A0', ' ')
                .replace('\u2022', '•')
                .replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "")
                .trim();
    }

    private void addLink(PDDocument document, PDPage page, String displayText, String uri,
                         float x, float y, float fontSize) throws IOException {
        try {
            float textWidth = getTextWidth(displayText, FONT_REGULAR, fontSize);
            PDRectangle linkBox = new PDRectangle(x, y - 2f, textWidth, fontSize + 4f);

            PDAnnotationLink link = new PDAnnotationLink();
            link.setRectangle(linkBox);

            PDBorderStyleDictionary border = new PDBorderStyleDictionary();
            border.setWidth(0);
            link.setBorderStyle(border);

            PDActionURI action = new PDActionURI();
            // Normalizar URL
            String finalUri = uri.startsWith("http") ? uri : "https://" + uri;
            action.setURI(finalUri);
            link.setAction(action);

            page.getAnnotations().add(link);
        } catch (Exception ignored) {
            // Si falla el link, el texto igual se muestra
        }
    }
}