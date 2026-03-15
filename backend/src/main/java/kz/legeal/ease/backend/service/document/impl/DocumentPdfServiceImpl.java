package kz.legeal.ease.backend.service.document.impl;

import kz.legeal.ease.backend.domain.Document;
import kz.legeal.ease.backend.domain.TemplateField;
import kz.legeal.ease.backend.service.document.DocumentPdfService;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Generates a structured PDF for a completed legal document using Apache PDFBox 3.x.
 *
 * <h3>Font / Unicode support</h3>
 * <p>To enable full Unicode support (Cyrillic / Kazakh characters), place a TrueType
 * font file at {@code src/main/resources/fonts/FreeSans.ttf} (or another Unicode-capable
 * font). The implementation automatically detects the bundled font and falls back to
 * the standard Helvetica Type1 font if the resource is absent.
 * <br>
 * Quick setup (Alpine Docker): in the {@code Dockerfile} add
 * {@code RUN apk add --no-cache font-freefont-ttf} and copy the font to resources,
 * or bundle it directly in the project.
 *
 * <h3>PDF layout</h3>
 * <pre>
 *  ┌──────────────────────────────┐
 *  │  [logo-like header area]     │
 *  │  DOCUMENT TITLE              │
 *  │  Template: template name     │
 *  │  ────────────────────────── │
 *  │  Label 1 :  Value 1          │
 *  │  Label 2 :  Value 2          │
 *  │  …                           │
 *  │  ────────────────────────── │
 *  │  Created: 15.03.2026 12:34   │
 *  └──────────────────────────────┘
 * </pre>
 */
@Slf4j
@Service
public class DocumentPdfServiceImpl implements DocumentPdfService {

    private static final float MARGIN         = 50f;
    private static final float PAGE_WIDTH     = PDRectangle.A4.getWidth();
    private static final float PAGE_HEIGHT    = PDRectangle.A4.getHeight();
    private static final float USABLE_WIDTH   = PAGE_WIDTH - 2 * MARGIN;

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    @Override
    public byte[] generate(Document document) {
        try (PDDocument pdf = new PDDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            final PDFont regular = loadFont(pdf, "/fonts/FreeSans.ttf");
            final PDFont bold    = loadFont(pdf, "/fonts/FreeSansBold.ttf");

            final PDPage page = new PDPage(PDRectangle.A4);
            pdf.addPage(page);

            // Build label → value map from template fields
            final Map<String, String> labelMap = document.getTemplate().getTemplateFields()
                    .stream()
                    .collect(Collectors.toMap(TemplateField::getFieldKey, TemplateField::getLabel));

            try (PDPageContentStream cs = new PDPageContentStream(pdf, page)) {
                float y = PAGE_HEIGHT - MARGIN;

                // ── Header band ──────────────────────────────────────────────
                cs.setNonStrokingColor(0.18f, 0.35f, 0.58f); // dark blue
                cs.addRect(MARGIN, y - 5, USABLE_WIDTH, 2);
                cs.fill();

                y -= 20;

                // ── Document title ───────────────────────────────────────────
                y = writeText(cs, bold, 18, document.getTitle(), MARGIN, y, 0f, 0f, 0f);
                y -= 6;

                // ── Template name (secondary) ────────────────────────────────
                y = writeText(cs, regular, 11,
                        "Шаблон: " + document.getTemplate().getTitle(),
                        MARGIN, y, 0.4f, 0.4f, 0.4f);
                y -= 14;

                // ── Separator ────────────────────────────────────────────────
                y = drawLine(cs, MARGIN, PAGE_WIDTH - MARGIN, y, 0.7f, 0.7f, 0.7f);
                y -= 14;

                // ── Field values ─────────────────────────────────────────────
                for (final var fv : document.getFieldValues()) {
                    final var label = labelMap.getOrDefault(fv.getFieldKey(), fv.getFieldKey());
                    final var value = fv.getFieldValue() != null ? fv.getFieldValue() : "—";

                    // Label in bold, value in regular, side by side
                    y = writeText(cs, bold,    11, label + ":", MARGIN, y, 0.1f, 0.1f, 0.1f);
                    y = writeText(cs, regular, 11, "   " + truncate(value, 80), MARGIN, y, 0.15f, 0.15f, 0.15f);
                    y -= 4;

                    if (y < MARGIN + 60) {
                        // Simple overflow guard: stop adding fields if near page bottom
                        y = writeText(cs, regular, 9, "...", MARGIN, y, 0.5f, 0.5f, 0.5f);
                        break;
                    }
                }

                // ── Footer separator ─────────────────────────────────────────
                final float footerY = MARGIN + 30;
                drawLine(cs, MARGIN, PAGE_WIDTH - MARGIN, footerY, 0.7f, 0.7f, 0.7f);

                // ── Footer text ──────────────────────────────────────────────
                final var created = document.getCreatedDate() != null
                        ? document.getCreatedDate().format(DATE_FMT)
                        : LocalDateTime.now().format(DATE_FMT);
                writeText(cs, regular, 8,
                        "Создан: " + created + "  |  LegalEase",
                        MARGIN, footerY - 12, 0.5f, 0.5f, 0.5f);
            }

            pdf.save(out);
            return out.toByteArray();

        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate PDF for document id=" + document.getId(), e);
        }
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    /**
     * Attempt to load a TrueType font from the classpath (for full Unicode/Cyrillic support).
     * Falls back to standard Helvetica (Latin only) when the resource is absent.
     */
    private PDFont loadFont(PDDocument doc, String classpathResource) {
        try (InputStream is = getClass().getResourceAsStream(classpathResource)) {
            if (is != null) {
                return PDType0Font.load(doc, is);
            }
        } catch (Exception e) {
            log.warn("Could not load font from {}: {}", classpathResource, e.getMessage());
        }
        // Fallback: standard Helvetica Type1 — Latin characters only
        log.debug("Using fallback Helvetica font (no Cyrillic support). " +
                "Place a Unicode TTF at src/main/resources/fonts/FreeSans.ttf to enable full Unicode.");
        try {
            return new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        } catch (Exception e) {
            throw new IllegalStateException("Could not load fallback font", e);
        }
    }

    /** Write a single line and return the new Y position (after the line). */
    private float writeText(PDPageContentStream cs, PDFont font, float size,
                            String text, float x, float y,
                            float r, float g, float b) throws Exception {
        cs.beginText();
        cs.setFont(font, size);
        cs.setNonStrokingColor(r, g, b);
        cs.newLineAtOffset(x, y);
        cs.showText(sanitize(text));
        cs.endText();
        return y - (size + 4);
    }

    /** Draw a horizontal line and return the new Y position. */
    private float drawLine(PDPageContentStream cs,
                           float x1, float x2, float y,
                           float r, float g, float b) throws Exception {
        cs.setStrokingColor(r, g, b);
        cs.setLineWidth(0.5f);
        cs.moveTo(x1, y);
        cs.lineTo(x2, y);
        cs.stroke();
        return y;
    }

    /**
     * Replace characters that are illegal in PDF strings or that the fallback
     * Helvetica font cannot encode (non-Latin). For production, use a Unicode font.
     */
    private String sanitize(String text) {
        if (text == null) return "";
        // Remove non-printable control chars; keep printable ASCII + Latin-1 supplement
        return text.replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F]", "");
    }

    private String truncate(String text, int maxLen) {
        if (text == null) return "";
        return text.length() > maxLen ? text.substring(0, maxLen - 1) + "…" : text;
    }
}
