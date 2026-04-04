package kz.legeal.ease.backend;

import kz.legeal.ease.backend.domain.Document;
import kz.legeal.ease.backend.domain.DocumentFieldValue;
import kz.legeal.ease.backend.domain.Template;
import kz.legeal.ease.backend.domain.TemplateField;
import kz.legeal.ease.backend.enums.DocumentStatus;
import kz.legeal.ease.backend.enums.TemplateFieldType;
import kz.legeal.ease.backend.enums.TemplateStatus;
import kz.legeal.ease.backend.service.document.TemplateBodyRenderer;
import kz.legeal.ease.backend.service.document.impl.DocumentPdfServiceImpl;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Generates real PDFs from DocumentPdfServiceImpl using the task's test data.
 * No Spring context — both services are plain POJOs.
 *
 * Run:  ./gradlew test --tests "kz.legeal.ease.backend.PdfGenerationTest"
 *
 * Outputs (in backend/):
 *   test-document.pdf        — narrative mode (body + QR seal + watermark)
 *   test-document-legacy.pdf — legacy mode (field table + QR seal + watermark)
 */
class PdfGenerationTest {

    // ── Test 1: Full narrative PDF ────────────────────────────────────────────

    @Test
    void generateNarrativePdf() throws Exception {
        final Template template = Template.builder()
                .id(1L)
                .title("Договор купли-продажи")
                .status(TemplateStatus.PUBLISHED)
                .body(
                    "Настоящий договор заключён {{date}} между {{client_name}} " +
                    "и {{company_name}}.\n\n" +
                    "Предмет договора:\n" +
                    "{{item_description}} стоимостью {{price}} тенге.\n\n" +
                    "Стороны договорились о следующем: Продавец обязуется передать Покупателю " +
                    "указанное имущество в срок, предусмотренный настоящим договором. " +
                    "Покупатель обязуется принять имущество и уплатить за него обусловленную " +
                    "настоящим договором цену.\n\n" +
                    "Настоящий договор составлен в двух экземплярах, имеющих одинаковую " +
                    "юридическую силу, по одному для каждой из сторон."
                )
                .templateFields(List.of(
                        field(1L, "date",             "Дата договора",         0),
                        field(2L, "client_name",      "Имя клиента",           1),
                        field(3L, "company_name",     "Наименование компании", 2),
                        field(4L, "item_description", "Предмет договора",      3),
                        field(5L, "price",            "Цена (тенге)",          4)
                ))
                .build();

        final Document document = Document.builder()
                .id(42L)
                .title("Договор купли-продажи №42")
                .template(template)
                .status(DocumentStatus.COMPLETED)
                .fieldValues(List.of(
                        value("date",             "28.03.2026"),
                        value("client_name",      "Daniyal"),
                        value("company_name",     "ТОО \"Альфа\""),
                        value("item_description", "Автомобиль Toyota Camry 2020 года"),
                        value("price",            "15 000 000")
                ))
                .build();
        document.setCreatedDate(LocalDateTime.of(2026, 3, 28, 14, 30));
        document.setCreatedBy("daniyal@example.com");

        final byte[] pdf = new DocumentPdfServiceImpl(new TemplateBodyRenderer())
                .generate(document);

        // ── Structural assertions ─────────────────────────────────────────
        assertThat(pdf).isNotEmpty();
        assertThat(new String(pdf, 0, 4)).isEqualTo("%PDF");

        // ── Hash assertion ────────────────────────────────────────────────
        final String hash = HexFormat.of().formatHex(
                MessageDigest.getInstance("SHA-256").digest(pdf));
        assertThat(hash).hasSize(64).matches("[0-9a-f]+");
        System.out.println("SHA-256: " + hash);

        // ── Save ──────────────────────────────────────────────────────────
        final Path out = Path.of("test-document.pdf").toAbsolutePath();
        Files.write(out, pdf);
        System.out.printf("Saved → %s  (%.1f KB)%n", out, pdf.length / 1024.0);
    }

    // ── Test 2: Legacy mode (no template body) ────────────────────────────────

    @Test
    void generateLegacyFallbackPdf() throws Exception {
        final Template template = Template.builder()
                .id(2L)
                .title("Стандартная заявка")
                .status(TemplateStatus.PUBLISHED)
                // no body → legacy field-table mode
                .templateFields(List.of(
                        field(10L, "client_name", "Имя клиента", 0),
                        field(11L, "date",        "Дата",        1)
                ))
                .build();

        final Document document = Document.builder()
                .id(99L)
                .title("Заявка №99")
                .template(template)
                .status(DocumentStatus.COMPLETED)
                .fieldValues(List.of(
                        value("client_name", "Daniyal"),
                        value("date",        "28.03.2026")
                ))
                .build();
        document.setCreatedDate(LocalDateTime.of(2026, 3, 28, 14, 30));

        final byte[] pdf = new DocumentPdfServiceImpl(new TemplateBodyRenderer())
                .generate(document);

        assertThat(pdf).isNotEmpty();
        assertThat(new String(pdf, 0, 4)).isEqualTo("%PDF");

        final Path out = Path.of("test-document-legacy.pdf").toAbsolutePath();
        Files.write(out, pdf);
        System.out.printf("Saved → %s  (%.1f KB)%n", out, pdf.length / 1024.0);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static TemplateField field(long id, String key, String label, int order) {
        return TemplateField.builder()
                .id(id).fieldKey(key).label(label)
                .fieldType(TemplateFieldType.TEXT).required(true).orderNum(order)
                .build();
    }

    private static DocumentFieldValue value(String key, String val) {
        return DocumentFieldValue.builder().fieldKey(key).fieldValue(val).build();
    }
}
