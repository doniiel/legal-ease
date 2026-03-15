package kz.legeal.ease.backend.service.audit.impl;

import kz.legeal.ease.backend.domain.AuditLog;
import kz.legeal.ease.backend.enums.AuditAction;
import kz.legeal.ease.backend.repository.AuditLogRepository;
import kz.legeal.ease.backend.service.audit.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;

    @Override
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(Long userId, AuditAction action, String entityType, Long entityId, String metadata) {
        try {
            final var entry = AuditLog.builder()
                    .userId(userId)
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .metadata(metadata)
                    .build();
            auditLogRepository.save(entry);
        } catch (Exception e) {
            // Audit must never propagate failures to callers
            log.error("Failed to write audit log: action={} entity={} id={}: {}",
                    action, entityType, entityId, e.getMessage());
        }
    }
}
