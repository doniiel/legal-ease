package kz.legeal.ease.backend.service.document;

import kz.legeal.ease.backend.dto.CompleteDocumentResponseDto;
import kz.legeal.ease.backend.dto.document.DocumentDto;
import kz.legeal.ease.backend.dto.document.DocumentPreviewDto;
import kz.legeal.ease.backend.dto.document.DocumentVersionDto;
import kz.legeal.ease.backend.dto.document.PresignedUrlResponse;
import kz.legeal.ease.backend.request.document.CreateDocumentRequest;
import kz.legeal.ease.backend.request.document.UpdateDocumentRequest;
import kz.legeal.ease.backend.service.rule.result.RuleEngineResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Core service for the user-facing document lifecycle.
 *
 * <h2>Lifecycle</h2>
 * <pre>
 * createDocument()   → status = DRAFT
 *       ↓
 * update() (N times) → still DRAFT
 *       ↓
 * getSuggestions()   → FieldSuggestion chain (conditional + required-doc + AI hints)
 *       ↓
 * validate()         → runs Complete chain WITHOUT generating PDF
 *                       If passes: status = VALIDATED
 *                       If fails:  status stays DRAFT, returns errors
 *       ↓
 * complete()         → requires VALIDATED status
 *                       Generates versioned PDF → uploads to S3 → status = COMPLETED
 *       ↓
 * archive()          → COMPLETED → ARCHIVED (all share links revoked)
 * restore()          → ARCHIVED → DRAFT (version history preserved)
 * </pre>
 */
public interface DocumentService {

    DocumentDto createDocument(CreateDocumentRequest request);

    Page<DocumentPreviewDto> getMyDocuments(Pageable pageable);

    Page<DocumentPreviewDto> getMyDocumentsIncludingArchived(Pageable pageable);

    DocumentDto getMyDocumentById(Long docId);

    DocumentDto update(Long docId, UpdateDocumentRequest req);

    /**
     * Run the full rule engine validation chain against the document.
     * If all validation rules pass: status → VALIDATED.
     * If any rule fails: status stays DRAFT, validation errors are returned in the result.
     */
    RuleEngineResult validate(Long docId);

    /**
     * Complete the document (status must be VALIDATED or DRAFT with no errors).
     * Generates a versioned PDF, uploads to S3, and sets status = COMPLETED.
     */
    CompleteDocumentResponseDto complete(Long docId);

    RuleEngineResult getSuggestions(Long docId);

    /**
     * Run AI analysis on the document without completing it.
     * Returns risk items, field suggestions, and AI summary.
     */
    RuleEngineResult analyzeWithAi(Long docId);

    /** Archive a COMPLETED document. All active share links are revoked. */
    DocumentDto archive(Long docId);

    /** Restore an ARCHIVED document back to DRAFT for re-editing. */
    DocumentDto restore(Long docId);

    void deleteDocument(Long docId);

    /** Stream the PDF of a completed document as raw bytes. */
    byte[] downloadDocument(Long docId);

    /** Generate a time-limited presigned URL for direct PDF download. */
    PresignedUrlResponse getPresignedUrl(Long docId);

    /**
     * Re-generate the PDF for a COMPLETED document and replace the stored file in S3.
     * Useful when the template renderer is updated after the document was completed.
     */
    DocumentDto regeneratePdf(Long docId);

    /** List all immutable PDF versions for a document owned by the current user. */
    List<DocumentVersionDto> getVersions(Long docId);

    /** Download a specific immutable version of a document's PDF. */
    byte[] downloadVersion(Long docId, int version);
}
