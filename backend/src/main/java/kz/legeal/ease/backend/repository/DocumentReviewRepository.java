package kz.legeal.ease.backend.repository;

import kz.legeal.ease.backend.domain.DocumentReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface DocumentReviewRepository extends JpaRepository<DocumentReview, Long> {

    List<DocumentReview> findAllByDocumentIdOrderByCreatedDateDesc(Long documentId);

    Page<DocumentReview> findAllByLawyerId(Long lawyerId, Pageable pageable);

    /** Find a review by its own ID, restricted to a specific lawyer. Used for ownership checks on update/delete. */
    Optional<DocumentReview> findByIdAndLawyerId(Long reviewId, Long lawyerId);

    /** Find a lawyer's existing review for a specific document. Used to update rather than duplicate. */
    Optional<DocumentReview> findByDocumentIdAndLawyerId(Long documentId, Long lawyerId);

    boolean existsByDocumentIdAndLawyerId(Long documentId, Long lawyerId);

    /** All reviews for documents created from the given lawyer's templates. */
    @Query("""
            SELECT r FROM DocumentReview r
            WHERE r.document.template.id IN
                (SELECT t.id FROM Template t WHERE t.lawyer.id = :lawyerId AND t.active = true)
            ORDER BY r.createdDate DESC
            """)
    Page<DocumentReview> findAllByTemplateOwner(Long lawyerId, Pageable pageable);
}
