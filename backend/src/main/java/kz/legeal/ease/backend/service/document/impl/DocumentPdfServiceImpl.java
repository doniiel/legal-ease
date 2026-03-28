package kz.legeal.ease.backend.service.document.impl;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import kz.legeal.ease.backend.domain.Document;
import kz.legeal.ease.backend.domain.DocumentFieldValue;
import kz.legeal.ease.backend.domain.TemplateField;
import kz.legeal.ease.backend.service.document.DocumentPdfService;
import kz.legeal.ease.backend.service.document.TemplateBodyRenderer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.apache.pdfbox.util.Matrix;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Generates a professional legal-grade PDF for a completed document.
 *
 * <h3>Layout (per page)</h3>
 * <pre>
 *  ┌────────────────────────────────────────────────────────────────┐
 *  │  HEADER  — LegalEase brand, doc ID, date                      │
 *  ├────────────────────────────────────────────────────────────────┤
 *  │  BODY    — rendered template text (narrative paragraphs)       │
 *  │            OR legacy field table when template has no body     │
 *  ├────────────────────────────────────────────────────────────────┤
 *  │  SEAL    — QR code + "Сканируйте для проверки" (last page)    │
 *  ├────────────────────────────────────────────────────────────────┤
 *  │  FOOTER  — page number only                                    │
 *  └────────────────────────────────────────────────────────────────┘
 * </pre>
 *
 * <h3>Rendering modes</h3>
 * <ul>
 *   <li><b>Narrative</b> — {@code template.body} is non-blank: renders full document
 *       text, then the verification seal.</li>
 *   <li><b>Legacy</b> — {@code template.body} is null/blank: renders the two-column
 *       field table, then the verification seal. Backward-compatible.</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentPdfServiceImpl implements DocumentPdfService {

    private final TemplateBodyRenderer bodyRenderer;

    @Value("${app.base-url:https://legalease.kz}")
    private String baseUrl = "https://legalease.kz";

    // ─────────────────────────────────────────────────────────────────────────
    // 8pt spacing grid
    // ─────────────────────────────────────────────────────────────────────────
    private static final float SP1 =  4f;
    private static final float SP2 =  8f;
    private static final float SP3 = 16f;
    private static final float SP4 = 24f;

    // ─────────────────────────────────────────────────────────────────────────
    // Page geometry
    // ─────────────────────────────────────────────────────────────────────────
    private static final float PAGE_W     = PDRectangle.A4.getWidth();   // 595.28pt
    private static final float PAGE_H     = PDRectangle.A4.getHeight();  // 841.89pt
    private static final float MARGIN_X   = 56f;
    private static final float USABLE_W   = PAGE_W - 2 * MARGIN_X;

    private static final float HEADER_H   = 80f;
    private static final float FOOTER_H   = 36f;
    private static final float CONTENT_TOP = PAGE_H - HEADER_H - SP4;
    private static final float CONTENT_BOT = FOOTER_H + SP2;

    // ─────────────────────────────────────────────────────────────────────────
    // Two-column field grid  (label 36% | gap | value remainder) — legacy mode
    // ─────────────────────────────────────────────────────────────────────────
    private static final float COL_LABEL_W = USABLE_W * 0.36f;
    private static final float COL_GAP     = 12f;
    private static final float COL_VALUE_X = MARGIN_X + COL_LABEL_W + COL_GAP;
    private static final float COL_VALUE_W = USABLE_W - COL_LABEL_W - COL_GAP;

    // Field row geometry
    private static final float ROW_PAD_V  = 8f;
    private static final float LINE_LEAD  = 3.5f;
    private static final float MIN_ROW_H  = 28f;

    // Narrative paragraph geometry
    private static final float BODY_LEAD  = 5.5f;
    private static final float PARA_GAP   = SP2;

    // ─────────────────────────────────────────────────────────────────────────
    // Brand palette
    // ─────────────────────────────────────────────────────────────────────────
    private static final float[] NAVY    = rgb(0x0F, 0x2A, 0x44);
    private static final float[] BLUE    = rgb(0x4D, 0xA3, 0xFF);
    private static final float[] C_TEXT  = rgb(0x2E, 0x2E, 0x2E);
    private static final float[] MUTED   = rgb(0x7A, 0x89, 0x99);
    private static final float[] BORDER  = rgb(0xD0, 0xD6, 0xDC);
    private static final float[] BG_CARD = rgb(0xF7, 0xF9, 0xFC);
    private static final float[] WHITE   = {1f, 1f, 1f};
    private static final float[] WHITE_D = {0.62f, 0.72f, 0.82f};

    // ─────────────────────────────────────────────────────────────────────────
    // Typography scale
    // ─────────────────────────────────────────────────────────────────────────
    private static final float F_BRAND   = 20f;
    private static final float F_TITLE   = 22f;
    private static final float F_SECTION =  9f;
    private static final float F_BODY    = 10.5f;
    private static final float F_VALUE   = 10f;
    private static final float F_LABEL   =  9f;
    private static final float F_META    =  8.5f;
    private static final float F_FOOTER  =  7.5f;

    // Section card height — defined after F_SECTION
    private static final float SECTION_CARD_H = F_SECTION + SP2 * 2;

    // ─────────────────────────────────────────────────────────────────────────
    // Verification seal (QR block)
    // ─────────────────────────────────────────────────────────────────────────

    /** QR code rendered size in points (~80×80 as required). */
    private static final float QR_SIZE = 80f;

    /**
     * Height of the seal content area (QR + padding + caption).
     * <pre>
     *   SP2 (top pad) + QR_SIZE (80) + SP1 (4) + F_META (8.5) + SP2 (8) + SP1 (4) = 112.5 → 114
     * </pre>
     */
    private static final float SEAL_CONTENT_H = 114f;

    /**
     * Total height consumed by the seal render item including section card.
     * <pre>
     *   SECTION_CARD_H (25) + SP3 gap (16) + SEAL_CONTENT_H (114) + SP2 (8) = 163
     * </pre>
     */
    private static final float SEAL_H = SECTION_CARD_H + SP3 + SEAL_CONTENT_H + SP2;

    // ─────────────────────────────────────────────────────────────────────────
    // Watermark
    // ─────────────────────────────────────────────────────────────────────────
    private static final float WATERMARK_ALPHA = 0.06f;
    private static final float WATERMARK_SIZE  = 62f;

    // ─────────────────────────────────────────────────────────────────────────
    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy");

    // ─────────────────────────────────────────────────────────────────────────
    // Render queue — sealed type hierarchy
    // ─────────────────────────────────────────────────────────────────────────

    private sealed interface RenderItem
            permits ParagraphItem, SectionCardItem, FieldRowItem, SealItem {}

    private record ParagraphItem(List<String> lines)                    implements RenderItem {}
    private record SectionCardItem(String title)                        implements RenderItem {}
    private record FieldRowItem(String label, List<String> valueLines,
                                int rowIndex)                           implements RenderItem {}

    /**
     * Verification seal — always the last item in the render queue.
     * Draws only a QR code and a short prompt text; no personal metadata.
     */
    private record SealItem(String verifyUrl,
                            BufferedImage qrImage)                      implements RenderItem {}

    // ─────────────────────────────────────────────────────────────────────────
    // Entry point
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public byte[] generate(Document document) {
        try (PDDocument pdf = new PDDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            final PDFont regular = loadFont(pdf, "/fonts/FreeSans.ttf");
            final PDFont bold    = loadFont(pdf, "/fonts/FreeSansBold.ttf");

            final String dateStr = document.getCreatedDate() != null
                    ? document.getCreatedDate().format(DATE_FMT)
                    : LocalDateTime.now().format(DATE_FMT);
            final String docRef  = "DOC-" + String.format("%06d", document.getId());

            final List<RenderItem> queue =
                    buildRenderQueue(document, regular, docRef);
            final int totalPages = calculateTotalPages(queue);

            // ── Render pass ────────────────────────────────────────────────
            int     pageNum   = 1;
            int     queueIdx  = 0;
            boolean firstPage = true;

            while (queueIdx < queue.size() || firstPage) {
                final PDPage page = new PDPage(PDRectangle.A4);
                pdf.addPage(page);

                try (PDPageContentStream cs = new PDPageContentStream(pdf, page)) {
                    drawWatermarkStamp(cs, bold);
                    drawHeader(cs, bold, regular, docRef, dateStr);
                    drawFooter(cs, regular, pageNum, totalPages);

                    float y = CONTENT_TOP;

                    if (firstPage) {
                        y = drawTitleBlock(cs, bold, regular, document, docRef, y);
                        firstPage = false;
                    } else {
                        y = drawContinuationHeader(cs, bold, regular, document, docRef, y);
                    }

                    while (queueIdx < queue.size()) {
                        final RenderItem item = queue.get(queueIdx);
                        if (y - itemHeight(item) < CONTENT_BOT) break;
                        y = drawItem(cs, regular, bold, pdf, item, y);
                        queueIdx++;
                    }

                    // Bottom border closes the field table on the last page
                    if (queueIdx >= queue.size()
                            && !queue.isEmpty()
                            && queue.get(queue.size() - 1) instanceof FieldRowItem) {
                        hline(cs, MARGIN_X, PAGE_W - MARGIN_X, y, BORDER, 0.5f);
                    }
                }

                pageNum++;
                if (queueIdx >= queue.size()) break;
            }

            pdf.save(out);
            return out.toByteArray();

        } catch (Exception e) {
            throw new IllegalStateException(
                    "PDF generation failed for document id=" + document.getId(), e);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Render queue construction
    // ─────────────────────────────────────────────────────────────────────────

    private List<RenderItem> buildRenderQueue(Document document, PDFont regular,
                                              String docRef) throws Exception {
        final List<RenderItem>    queue       = new ArrayList<>();
        final Map<String, String> fieldValues = buildFieldValuesMap(document);
        final Map<String, String> labelMap    = buildLabelMap(document);

        final String  rawBody = document.getTemplate().getBody();
        final boolean hasBody = rawBody != null && !rawBody.isBlank();

        if (hasBody) {
            // ── Narrative mode: render body as flowing paragraphs only ───────
            // Field table is intentionally omitted — the body text is the document.
            final String rendered = bodyRenderer.render(rawBody, fieldValues);
            for (final String paragraph : rendered.split("\n{2,}")) {
                final String normalized = paragraph.replace("\n", " ").trim();
                if (normalized.isEmpty()) continue;
                queue.add(new ParagraphItem(
                        wrapText(normalized, regular, F_BODY, USABLE_W)));
            }
        } else {
            // ── Legacy mode: field table only (backward-compatible) ──────────
            queue.add(new SectionCardItem("СВЕДЕНИЯ О ДОКУМЕНТЕ"));
            addFieldRows(queue, document, labelMap, regular);
        }

        // Verification seal — always last
        final String verifyUrl = baseUrl + "/documents/verify/" + document.getId();
        queue.add(new SealItem(verifyUrl, generateQrImage(verifyUrl, 240)));

        return queue;
    }

    private void addFieldRows(List<RenderItem> queue, Document document,
                              Map<String, String> labelMap,
                              PDFont regular) throws Exception {
        int rowIndex = 0;
        for (final DocumentFieldValue fv : document.getFieldValues()) {
            final String label = labelMap.getOrDefault(
                    fv.getFieldKey(), fv.getFieldKey());
            final String rawVal = fv.getFieldValue() != null ? fv.getFieldValue() : "—";
            queue.add(new FieldRowItem(
                    label,
                    wrapText(rawVal, regular, F_VALUE, COL_VALUE_W),
                    rowIndex++));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Item height
    // ─────────────────────────────────────────────────────────────────────────

    private float itemHeight(RenderItem item) {
        return switch (item) {
            case ParagraphItem   p       -> p.lines().size() * (F_BODY + BODY_LEAD) + PARA_GAP;
            case SectionCardItem ignored -> SECTION_CARD_H + SP3;
            case FieldRowItem    f       -> rowHeight(f.valueLines().size());
            case SealItem        ignored -> SEAL_H;
        };
    }

    private float rowHeight(int valueLineCount) {
        final float textH = F_VALUE + (valueLineCount - 1) * (F_VALUE + LINE_LEAD);
        return Math.max(MIN_ROW_H, textH + ROW_PAD_V * 2);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Item draw dispatch
    // ─────────────────────────────────────────────────────────────────────────

    private float drawItem(PDPageContentStream cs, PDFont regular, PDFont bold,
                           PDDocument pdf, RenderItem item, float y) throws Exception {
        return switch (item) {
            case ParagraphItem   p -> drawBodyParagraph(cs, regular, p.lines(), y);
            case SectionCardItem s -> drawSectionCard(cs, bold, s.title(), y);
            case FieldRowItem    f -> {
                final float h = rowHeight(f.valueLines().size());
                drawFieldRow(cs, regular, bold,
                             f.label(), f.valueLines(), y, h, f.rowIndex());
                yield y - h;
            }
            case SealItem        s -> drawVerificationSeal(cs, regular, bold, pdf, s, y);
        };
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Page sections
    // ─────────────────────────────────────────────────────────────────────────

    private void drawHeader(PDPageContentStream cs, PDFont bold, PDFont regular,
                            String docRef, String dateStr) throws Exception {
        fill(cs, NAVY, 0f, PAGE_H - HEADER_H, PAGE_W, HEADER_H);
        fill(cs, BLUE, 0f, PAGE_H - HEADER_H, PAGE_W, 2.5f);

        final float brandY  = PAGE_H - HEADER_H + 40f;
        final float legalW  = bold.getStringWidth("Legal") / 1000f * F_BRAND;
        text(cs, bold,    F_BRAND, "Legal", MARGIN_X,          brandY, WHITE);
        text(cs, bold,    F_BRAND, "Ease",  MARGIN_X + legalW, brandY, BLUE);
        text(cs, regular, 7f, "LEGAL DOCUMENTATION PLATFORM  ·  KAZAKHSTAN",
                MARGIN_X, PAGE_H - HEADER_H + 22f, WHITE_D);

        final String typeStr  = "ЮРИДИЧЕСКИЙ ДОКУМЕНТ";
        final String metaStr  = docRef + "   ·   " + dateStr;
        final float  typeStrW = bold.getStringWidth(typeStr)    / 1000f * 8f;
        final float  metaStrW = regular.getStringWidth(metaStr) / 1000f * 7f;
        final float  rightX   = PAGE_W - MARGIN_X;
        text(cs, bold,    8f, typeStr, rightX - typeStrW, brandY,                   WHITE_D);
        text(cs, regular, 7f, metaStr, rightX - metaStrW, PAGE_H - HEADER_H + 22f, WHITE_D);
    }

    /** Minimal footer — page number only. No personal data, no URLs. */
    private void drawFooter(PDPageContentStream cs, PDFont regular,
                            int pageNum, int totalPages) throws Exception {
        hline(cs, MARGIN_X, PAGE_W - MARGIN_X, FOOTER_H, BORDER, 0.5f);
        final float textY    = FOOTER_H - SP2 - F_FOOTER;
        final String pageStr = "Страница " + pageNum + " из " + totalPages;
        final float  pW      = regular.getStringWidth(pageStr) / 1000f * F_FOOTER;
        text(cs, regular, F_FOOTER, pageStr, (PAGE_W - pW) / 2f, textY, MUTED);
    }

    private float drawTitleBlock(PDPageContentStream cs, PDFont bold, PDFont regular,
                                 Document document, String docRef, float y) throws Exception {
        y -= SP1;
        y  = line(cs, bold,    F_TITLE, document.getTitle(), MARGIN_X, y, NAVY);
        y -= SP2;
        y  = line(cs, regular, F_META,
                  "Шаблон: " + document.getTemplate().getTitle() + "   ·   " + docRef,
                  MARGIN_X, y, MUTED);
        y -= SP4;
        return y;
    }

    private float drawContinuationHeader(PDPageContentStream cs, PDFont bold, PDFont regular,
                                         Document document, String docRef, float y) throws Exception {
        y -= SP1;
        y  = line(cs, bold,    F_META, document.getTitle(),           MARGIN_X, y, NAVY);
        y -= SP1;
        y  = line(cs, regular, F_META, docRef + "   ·   Продолжение", MARGIN_X, y, MUTED);
        y -= SP3;
        hline(cs, MARGIN_X, PAGE_W - MARGIN_X, y, BORDER, 0.4f);
        y -= SP3;
        return y;
    }

    private float drawSectionCard(PDPageContentStream cs, PDFont bold,
                                  String title, float y) throws Exception {
        final float cardBottom = y - SECTION_CARD_H;
        fill(cs, BG_CARD, MARGIN_X, cardBottom, USABLE_W, SECTION_CARD_H);
        fill(cs, NAVY,    MARGIN_X, cardBottom, 3f,       SECTION_CARD_H);
        text(cs, bold, F_SECTION, title, MARGIN_X + SP2 + 3f, cardBottom + ROW_PAD_V + 1f, NAVY);
        return cardBottom - SP3;
    }

    private float drawBodyParagraph(PDPageContentStream cs, PDFont regular,
                                    List<String> lines, float y) throws Exception {
        for (final String l : lines) {
            text(cs, regular, F_BODY, l, MARGIN_X, y, C_TEXT);
            y -= (F_BODY + BODY_LEAD);
        }
        return y - PARA_GAP;
    }

    private void drawFieldRow(PDPageContentStream cs, PDFont regular, PDFont bold,
                              String label, List<String> valueLines,
                              float topY, float rowH, int rowIndex) throws Exception {
        final float rowBottom = topY - rowH;
        if (rowIndex % 2 == 0) {
            fill(cs, BG_CARD, MARGIN_X, rowBottom, USABLE_W, rowH);
        }
        hline(cs, MARGIN_X, PAGE_W - MARGIN_X, topY, BORDER, 0.3f);
        vline(cs, MARGIN_X + COL_LABEL_W + COL_GAP / 2f,
              rowBottom + ROW_PAD_V * 0.5f, topY - ROW_PAD_V * 0.5f, BORDER, 0.3f);

        final float firstLine = topY - ROW_PAD_V - F_VALUE;
        text(cs, regular, F_LABEL, label, MARGIN_X + SP2,
             firstLine + (F_VALUE - F_LABEL) * 0.5f, MUTED);

        float vY = firstLine;
        for (final String valueLine : valueLines) {
            text(cs, regular, F_VALUE, valueLine, COL_VALUE_X, vY, C_TEXT);
            vY -= (F_VALUE + LINE_LEAD);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Verification seal  (last page, last item)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Draws the verification seal block:
     * <pre>
     *  ▌ ВЕРИФИКАЦИЯ ДОКУМЕНТА
     *  ┌────────────────────────────────────────────┬──────────────────┐
     *  │ Отсканируйте QR-код для проверки           │                  │
     *  │ подлинности документа на платформе         │   QR CODE 80×80  │
     *  │ LegalEase.                                 │                  │
     *  │                                            │ Сканируйте для   │
     *  │                                            │ проверки         │
     *  └────────────────────────────────────────────┴──────────────────┘
     * </pre>
     * No URLs, no personal metadata — per UX requirements.
     */
    private float drawVerificationSeal(PDPageContentStream cs, PDFont regular, PDFont bold,
                                       PDDocument pdf, SealItem item, float y) throws Exception {
        y = drawSectionCard(cs, bold, "ВЕРИФИКАЦИЯ ДОКУМЕНТА", y);

        final float contentTop    = y;
        final float sealBottom    = contentTop - SEAL_CONTENT_H;
        final float rightEdge     = PAGE_W - MARGIN_X;

        // Background
        fill(cs, BG_CARD, MARGIN_X, sealBottom, USABLE_W, SEAL_CONTENT_H);
        hline(cs, MARGIN_X, rightEdge, contentTop, BORDER, 0.4f);
        hline(cs, MARGIN_X, rightEdge, sealBottom, BORDER, 0.5f);

        // ── Right: QR code ────────────────────────────────────────────────
        final float qrX = rightEdge - QR_SIZE - SP2;
        final float qrY = sealBottom + (SEAL_CONTENT_H - QR_SIZE) / 2f;

        if (item.qrImage() != null) {
            try {
                final PDImageXObject qrXObj = LosslessFactory.createFromImage(pdf, item.qrImage());
                cs.drawImage(qrXObj, qrX, qrY, QR_SIZE, QR_SIZE);
            } catch (Exception ex) {
                log.warn("Could not embed QR code: {}", ex.getMessage());
            }
        }

        // QR caption  (centered under the QR image)
        final String caption = "Сканируйте для проверки";
        final float  capW    = regular.getStringWidth(caption) / 1000f * (F_META - 1f);
        text(cs, regular, F_META - 1f, caption,
             qrX + (QR_SIZE - capW) / 2f, qrY - SP1 - (F_META - 1f), MUTED);

        // Vertical divider
        vline(cs, qrX - SP3, sealBottom + SP2, contentTop - SP2, BORDER, 0.4f);

        // ── Left: short explanatory text ─────────────────────────────────
        final float leftMaxW = qrX - SP3 - SP2 - MARGIN_X - SP2;
        final String[] lines = {
            "Отсканируйте QR-код для",
            "проверки подлинности",
            "документа на платформе",
            "LegalEase."
        };

        float textY = contentTop - SP3 - F_META;
        for (final String l : lines) {
            if (textY < sealBottom + SP1) break;
            text(cs, regular, F_META, l, MARGIN_X + SP2, textY, C_TEXT);
            textY -= (F_META + SP1 + 1f);
        }

        return sealBottom - SP2;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Watermark stamp  (drawn first on every page — sits behind all content)
    // ─────────────────────────────────────────────────────────────────────────

    private void drawWatermarkStamp(PDPageContentStream cs, PDFont bold) throws Exception {
        cs.saveGraphicsState();

        final PDExtendedGraphicsState gs = new PDExtendedGraphicsState();
        gs.setNonStrokingAlphaConstant(WATERMARK_ALPHA);
        gs.setStrokingAlphaConstant(WATERMARK_ALPHA);
        cs.setGraphicsStateParameters(gs);

        cs.beginText();
        cs.setFont(bold, WATERMARK_SIZE);
        cs.setNonStrokingColor(MUTED[0], MUTED[1], MUTED[2]);
        final float angle = (float) Math.toRadians(33);
        final float cos   = (float) Math.cos(angle);
        final float sin   = (float) Math.sin(angle);
        cs.setTextMatrix(new Matrix(cos, sin, -sin, cos,
                PAGE_W / 2f - 88f, PAGE_H / 2f - 10f));
        cs.showText("LegalEase");
        cs.endText();

        cs.restoreGraphicsState();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Page-count pre-calculation
    // ─────────────────────────────────────────────────────────────────────────

    private int calculateTotalPages(List<RenderItem> queue) {
        final float FIRST_PAGE_OVERHEAD =
                SP1 + (F_TITLE + LINE_LEAD + SP2) + (F_META + LINE_LEAD + SP4);
        final float CONT_PAGE_OVERHEAD  =
                SP1 + (F_META + LINE_LEAD + SP1) + (F_META + LINE_LEAD + SP3) + SP3;

        int   pages = 1;
        float y     = CONTENT_TOP - FIRST_PAGE_OVERHEAD;

        for (final RenderItem item : queue) {
            final float h = itemHeight(item);
            if (y - h < CONTENT_BOT) {
                pages++;
                y = CONTENT_TOP - CONT_PAGE_OVERHEAD;
            }
            y -= h;
        }

        return pages;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // QR code generation
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Generates a QR code image at {@code pixelSize × pixelSize}.
     * Returns {@code null} on failure — the PDF omits the QR gracefully.
     */
    private static BufferedImage generateQrImage(String content, int pixelSize) {
        try {
            final Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.CHARACTER_SET,    "UTF-8");
            hints.put(EncodeHintType.MARGIN,           1);
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);

            final BitMatrix matrix = new QRCodeWriter()
                    .encode(content, BarcodeFormat.QR_CODE, pixelSize, pixelSize, hints);
            return MatrixToImageWriter.toBufferedImage(matrix);
        } catch (Exception e) {
            log.warn("QR generation failed for '{}': {}", content, e.getMessage());
            return null;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Data helpers
    // ─────────────────────────────────────────────────────────────────────────

    private Map<String, String> buildFieldValuesMap(Document document) {
        final Map<String, String> map = new LinkedHashMap<>();
        for (final DocumentFieldValue fv : document.getFieldValues()) {
            if (fv.getFieldValue() != null) {
                map.put(fv.getFieldKey(), fv.getFieldValue());
            }
        }
        return map;
    }

    private Map<String, String> buildLabelMap(Document document) {
        return document.getTemplate().getTemplateFields().stream()
                .collect(Collectors.toMap(
                        TemplateField::getFieldKey,
                        TemplateField::getLabel,
                        (a, b) -> a));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Text wrapping
    // ─────────────────────────────────────────────────────────────────────────

    private List<String> wrapText(String raw, PDFont font, float size,
                                  float maxWidth) throws Exception {
        if (raw == null || raw.isBlank()) return List.of("—");
        final List<String> result = new ArrayList<>();
        for (final String para : raw.split("\n", -1)) {
            result.addAll(wrapParagraph(para.isBlank() ? "—" : para, font, size, maxWidth));
        }
        return result.isEmpty() ? List.of(raw) : result;
    }

    private List<String> wrapParagraph(String para, PDFont font, float size,
                                       float maxWidth) throws Exception {
        final List<String>  lines = new ArrayList<>();
        final StringBuilder cur   = new StringBuilder();
        for (final String word : para.split("\\s+")) {
            if (word.isEmpty()) continue;
            final String candidate = cur.isEmpty() ? word : cur + " " + word;
            if (font.getStringWidth(candidate) / 1000f * size > maxWidth && !cur.isEmpty()) {
                lines.add(cur.toString());
                cur.setLength(0);
                cur.append(word);
            } else {
                if (!cur.isEmpty()) cur.append(' ');
                cur.append(word);
            }
        }
        if (!cur.isEmpty()) lines.add(cur.toString());
        return lines.isEmpty() ? List.of(para) : lines;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Drawing primitives
    // ─────────────────────────────────────────────────────────────────────────

    private float line(PDPageContentStream cs, PDFont font, float size,
                       String str, float x, float y, float[] color) throws Exception {
        text(cs, font, size, str, x, y, color);
        return y - (size + LINE_LEAD);
    }

    private void text(PDPageContentStream cs, PDFont font, float size,
                      String str, float x, float y, float[] color) throws Exception {
        if (str == null || str.isEmpty()) return;
        cs.beginText();
        cs.setFont(font, size);
        cs.setNonStrokingColor(color[0], color[1], color[2]);
        cs.newLineAtOffset(x, y);
        cs.showText(str);
        cs.endText();
    }

    private void hline(PDPageContentStream cs, float x1, float x2, float y,
                       float[] color, float width) throws Exception {
        cs.setStrokingColor(color[0], color[1], color[2]);
        cs.setLineWidth(width);
        cs.moveTo(x1, y);
        cs.lineTo(x2, y);
        cs.stroke();
    }

    private void vline(PDPageContentStream cs, float x, float y1, float y2,
                       float[] color, float width) throws Exception {
        cs.setStrokingColor(color[0], color[1], color[2]);
        cs.setLineWidth(width);
        cs.moveTo(x, y1);
        cs.lineTo(x, y2);
        cs.stroke();
    }

    private void fill(PDPageContentStream cs, float[] color,
                      float x, float y, float w, float h) throws Exception {
        cs.setNonStrokingColor(color[0], color[1], color[2]);
        cs.addRect(x, y, w, h);
        cs.fill();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Font loading
    // ─────────────────────────────────────────────────────────────────────────

    private PDFont loadFont(PDDocument doc, String classpathResource) {
        try (InputStream is = getClass().getResourceAsStream(classpathResource)) {
            if (is != null) return PDType0Font.load(doc, is);
        } catch (Exception e) {
            log.warn("Could not load font {}: {}", classpathResource, e.getMessage());
        }
        log.warn("Font {} not found — falling back to Helvetica (no Cyrillic support)",
                classpathResource);
        try {
            return new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot load fallback font", e);
        }
    }

    private static float[] rgb(int r, int g, int b) {
        return new float[]{ r / 255f, g / 255f, b / 255f };
    }
}
