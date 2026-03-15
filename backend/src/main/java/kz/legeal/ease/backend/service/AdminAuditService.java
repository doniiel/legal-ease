package kz.legeal.ease.backend.service;

import kz.legeal.ease.backend.dto.AuditLogDto;
import kz.legeal.ease.backend.enums.AuditAction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminAuditService {

    /**
     * Search audit logs with optional filters. All filters are nullable — omitting them returns all records.
     *
     * @param userId     filter by the acting user
     * @param action     filter by action type
     * @param entityType filter by entity type string (e.g. "Document")
     * @param entityId   filter by entity primary key
     * @param pageable   pagination
     */
    Page<AuditLogDto> search(Long userId, AuditAction action, String entityType, Long entityId, Pageable pageable);

    AuditLogDto getById(Long id);
}
