package kz.legeal.ease.backend.dto.document;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

/**
 * Response for the public document verification endpoint.
 *
 * <p>Returned by {@code GET /open-api/documents/verify/{id}}.
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DocumentVerificationResponse {

    /** {@code VALID} or {@code INVALID}. */
    private final String status;

    /** Formatted as {@code DOC-000042}. */
    private final String documentId;

    /** Date the document was completed. {@code null} if document not found. */
    private final LocalDate createdAt;

    /** Full name of the document owner ({@code user.fio}). {@code null} if not found. */
    private final String createdBy;

    /**
     * {@code true} if a SHA-256 hash was stored at completion time, meaning the
     * document was properly finalized through the platform.
     */
    private final boolean hashValid;
}
