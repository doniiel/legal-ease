package kz.legeal.ease.backend.service.lawyer;

import kz.legeal.ease.backend.dto.document.DocumentDto;
import kz.legeal.ease.backend.dto.document.DocumentPreviewDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Lawyer-facing read-only view of documents created from their templates.
 */
public interface LawyerDocumentService {

    /** All non-deleted documents from any of the lawyer's templates. */
    Page<DocumentPreviewDto> getDocumentsByMyTemplates(Pageable pageable);

    /** Documents scoped to a specific template owned by the lawyer. */
    Page<DocumentPreviewDto> getDocumentsByTemplate(Long templateId, Pageable pageable);

    /** Fetch full document detail. Lawyer must own the document's template. */
    DocumentDto getDocumentById(Long documentId);
}
