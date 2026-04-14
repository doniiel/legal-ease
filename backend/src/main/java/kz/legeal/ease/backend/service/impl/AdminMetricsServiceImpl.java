package kz.legeal.ease.backend.service.impl;

import kz.legeal.ease.backend.dto.SystemMetricsDto;
import kz.legeal.ease.backend.enums.DocumentStatus;
import kz.legeal.ease.backend.repository.AuditLogRepository;
import kz.legeal.ease.backend.repository.DocumentRepository;
import kz.legeal.ease.backend.repository.LawyerApplicationRepository;
import kz.legeal.ease.backend.repository.TemplateRepository;
import kz.legeal.ease.backend.repository.UserRepository;
import kz.legeal.ease.backend.service.AdminMetricsService;
import kz.legeal.ease.backend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminMetricsServiceImpl implements AdminMetricsService {

    private final UserRepository              userRepository;
    private final TemplateRepository          templateRepository;
    private final DocumentRepository          documentRepository;
    private final LawyerApplicationRepository applicationRepository;
    private final AuditLogRepository          auditLogRepository;

    @Override
    @Transactional(readOnly = true)
    public SystemMetricsDto getMetrics() {
        SecurityUtils.requireRole(SecurityUtils.requireCurrentUser(), "ADMIN");
        return new SystemMetricsDto(
                userRepository.countByDeletedFalse(),
                userRepository.countActiveByRoleCode("LAWYER"),
                templateRepository.count(),
                documentRepository.countByStatus(DocumentStatus.DRAFT),
                documentRepository.countByStatus(DocumentStatus.VALIDATED),
                documentRepository.countByStatus(DocumentStatus.COMPLETED),
                documentRepository.countByStatus(DocumentStatus.ARCHIVED),
                applicationRepository.count(),
                auditLogRepository.count()
        );
    }
}
