package kz.legeal.ease.backend.service.document;

import kz.legeal.ease.backend.domain.Document;

/**
 * Generates a PDF representation of a completed legal document.
 */
public interface DocumentPdfService {

    /**
     * Render the document's filled fields into a PDF and return the raw bytes.
     *
     * <p>Must be called inside a transaction so that lazy-loaded
     * {@code template.templateFields} and {@code fieldValues} are accessible.
     *
     * @param document a COMPLETED document with all field values populated
     * @return raw PDF bytes ready for upload or streaming
     */
    byte[] generate(Document document);
}
