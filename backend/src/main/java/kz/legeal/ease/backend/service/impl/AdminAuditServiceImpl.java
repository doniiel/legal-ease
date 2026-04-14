package kz.legeal.ease.backend.service.impl;

import kz.legeal.ease.backend.dto.AuditLogDto;
import kz.legeal.ease.backend.enums.AuditAction;
import kz.legeal.ease.backend.exception.NotFoundException;
import kz.legeal.ease.backend.repository.AuditLogRepository;
import kz.legeal.ease.backend.service.AdminAuditService;
import kz.legeal.ease.backend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminAuditServiceImpl implements AdminAuditService {

    private final AuditLogRepository auditLogRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogDto> search(Long userId, AuditAction action, String entityType, Long entityId, Pageable pageable) {
        SecurityUtils.requireRole(SecurityUtils.requireCurrentUser(), "ADMIN");
        return auditLogRepository.search(userId, action, entityType, entityId, pageable)
                .map(log -> new AuditLogDto(
                        log.getId(),
                        log.getUserId(),
                        log.getAction(),
                        log.getEntityType(),
                        log.getEntityId(),
                        log.getMetadata(),
                        log.getCreatedAt()
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public AuditLogDto getById(Long id) {
        SecurityUtils.requireRole(SecurityUtils.requireCurrentUser(), "ADMIN");
        final var log = auditLogRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("AuditLog", id));
        return new AuditLogDto(
                log.getId(),
                log.getUserId(),
                log.getAction(),
                log.getEntityType(),
                log.getEntityId(),
                log.getMetadata(),
                log.getCreatedAt()
        );
    }
}
