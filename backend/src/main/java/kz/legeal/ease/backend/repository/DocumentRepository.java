package kz.legeal.ease.backend.repository;

import kz.legeal.ease.backend.domain.Document;
import kz.legeal.ease.backend.enums.DocumentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    @Query("SELECT d FROM Document d WHERE d.user.id = :userId AND d.deleted = false AND d.status != 'ARCHIVED'")
    Page<Document> findAllByUserIdAndNotDeleted(Long userId, Pageable pageable);

    @Query("SELECT d FROM Document d WHERE d.user.id = :userId AND d.deleted = false")
    Page<Document> findAllByUserIdIncludingArchived(Long userId, Pageable pageable);

    @Query("SELECT d FROM Document d WHERE d.id = :id AND d.user.id = :userId AND d.deleted = false")
    Optional<Document> findByIdAndUserIdAndNotDeleted(Long id, Long userId);

    boolean existsByTemplateIdAndUserIdAndStatus(Long templateId, Long userId, DocumentStatus status);

    /** All non-deleted documents created from a specific lawyer's template. */
    @Query("""
            SELECT d FROM Document d
            WHERE d.template.id IN (SELECT t.id FROM Template t WHERE t.lawyer.id = :lawyerId AND t.active = true)
              AND d.deleted = false
            ORDER BY d.createdDate DESC
            """)
    Page<Document> findAllByTemplateOwnerLawyerId(Long lawyerId, Pageable pageable);

    /** Non-deleted documents for a specific template owned by a lawyer. */
    @Query("""
            SELECT d FROM Document d
            WHERE d.template.id = :templateId
              AND d.template.lawyer.id = :lawyerId
              AND d.deleted = false
            """)
    Page<Document> findAllByTemplateIdAndLawyerId(Long templateId, Long lawyerId, Pageable pageable);

    /** Count of non-deleted documents per status for metrics. */
    @Query("SELECT COUNT(d) FROM Document d WHERE d.deleted = false AND d.status = :status")
    long countByStatus(DocumentStatus status);

    long countByDeleted(boolean deleted);
}
