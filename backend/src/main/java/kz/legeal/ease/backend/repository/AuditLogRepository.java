package kz.legeal.ease.backend.repository;

import kz.legeal.ease.backend.domain.AuditLog;
import kz.legeal.ease.backend.enums.AuditAction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    Page<AuditLog> findAllByUserId(Long userId, Pageable pageable);

    Page<AuditLog> findAllByEntityTypeAndEntityId(String entityType, Long entityId, Pageable pageable);

    Page<AuditLog> findAllByAction(AuditAction action, Pageable pageable);

    @Query(value = """
            SELECT a FROM AuditLog a
            WHERE (:userId IS NULL OR a.userId = :userId)
              AND (:action IS NULL OR a.action = :action)
              AND (:entityType IS NULL OR a.entityType = :entityType)
              AND (:entityId IS NULL OR a.entityId = :entityId)
            """,
            countQuery = """
            SELECT COUNT(a) FROM AuditLog a
            WHERE (:userId IS NULL OR a.userId = :userId)
              AND (:action IS NULL OR a.action = :action)
              AND (:entityType IS NULL OR a.entityType = :entityType)
              AND (:entityId IS NULL OR a.entityId = :entityId)
            """)
    Page<AuditLog> search(Long userId, AuditAction action, String entityType, Long entityId, Pageable pageable);

    long countByEntityType(String entityType);
}
