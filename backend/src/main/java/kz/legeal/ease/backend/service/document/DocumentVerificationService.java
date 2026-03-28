package kz.legeal.ease.backend.service.document;

import kz.legeal.ease.backend.dto.document.DocumentVerificationResponse;

/**
 * Public verification service — no authentication required.
 *
 * <p>Determines whether a document with the given ID was properly finalized
 * through the LegalEase platform by checking for a stored content hash.
 */
public interface DocumentVerificationService {

    /**
     * Verify the document identified by {@code documentId}.
     *
     * @param documentId the numeric document ID (from the QR code URL)
     * @return verification result — never throws; returns {@code INVALID} for
     *         non-existent or deleted documents
     */
    DocumentVerificationResponse verify(Long documentId);
}
