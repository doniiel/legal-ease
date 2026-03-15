package kz.legeal.ease.backend.repository;

import kz.legeal.ease.backend.domain.DocumentShare;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DocumentShareRepository extends JpaRepository<DocumentShare, Long> {

    Optional<DocumentShare> findByToken(String token);

    List<DocumentShare> findAllByDocumentId(Long documentId);

    /** Delete all share links that have passed their expiry time. */
    @Modifying
    @Query("DELETE FROM DocumentShare s WHERE s.expiresAt < :now")
    int deleteExpiredBefore(LocalDateTime now);

    /** Revoke all active share links for a document (used when document is archived/deleted). */
    @Modifying
    @Query("DELETE FROM DocumentShare s WHERE s.document.id = :documentId")
    int deleteAllByDocumentId(Long documentId);
}
