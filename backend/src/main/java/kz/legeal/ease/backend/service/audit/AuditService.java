package kz.legeal.ease.backend.service.audit;

import kz.legeal.ease.backend.enums.AuditAction;

public interface AuditService {

    /**
     * Record an audit event asynchronously so it never blocks the calling thread.
     *
     * @param userId     nullable — system events may have no user
     * @param action     the action that occurred
     * @param entityType e.g. "Document"
     * @param entityId   nullable — entity primary key
     * @param metadata   optional free-form JSON string
     */
    void log(Long userId, AuditAction action, String entityType, Long entityId, String metadata);
}
