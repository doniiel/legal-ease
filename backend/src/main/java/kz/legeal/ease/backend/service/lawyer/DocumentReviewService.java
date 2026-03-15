package kz.legeal.ease.backend.service.lawyer;

import kz.legeal.ease.backend.dto.document.DocumentReviewDto;
import kz.legeal.ease.backend.request.DocumentReviewRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Lawyer workflow for reviewing user documents.
 * A lawyer may only review documents created from their own templates.
 */
public interface DocumentReviewService {

    /**
     * Submit or update a review for a document.
     * If the lawyer has already reviewed this document, the existing review is updated.
     * The document must have been created from one of the lawyer's templates.
     */
    DocumentReviewDto submitReview(Long documentId, DocumentReviewRequest request);

    /** All reviews for a specific document (ordered by newest first). */
    List<DocumentReviewDto> getReviewsForDocument(Long documentId);

    /** Paginated list of all reviews submitted by the current lawyer. */
    Page<DocumentReviewDto> getMyReviews(Pageable pageable);

    /** Delete a review owned by the current lawyer. */
    void deleteReview(Long reviewId);
}
