package kz.legeal.ease.backend.service.impl;

import kz.legeal.ease.backend.dto.SystemMetricsDto;
import kz.legeal.ease.backend.enums.Status;
import kz.legeal.ease.backend.repository.DocumentRepository;
import kz.legeal.ease.backend.repository.LawyerApplicationRepository;
import kz.legeal.ease.backend.repository.TemplateRepository;
import kz.legeal.ease.backend.repository.UserRepository;
import kz.legeal.ease.backend.service.AdminMetricsService;
import kz.legeal.ease.backend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminMetricsServiceImpl implements AdminMetricsService {

    private final UserRepository              userRepository;
    private final TemplateRepository          templateRepository;
    private final DocumentRepository          documentRepository;
    private final LawyerApplicationRepository applicationRepository;

    @Override
    @Transactional(readOnly = true)
    public SystemMetricsDto getMetrics() {
        SecurityUtils.requireRole(SecurityUtils.requireCurrentUser(), "ADMIN");

        LocalDate today = LocalDate.now();
        LocalDateTime monthStart = today.withDayOfMonth(1).atStartOfDay();
        LocalDateTime monthEnd   = monthStart.plusMonths(1);

        return new SystemMetricsDto(
                userRepository.countByDeletedFalse(),
                userRepository.countActiveByRoleCode("LAWYER"),
                templateRepository.count(),
                documentRepository.countByDeleted(false),
                applicationRepository.countByStatus(Status.PENDING),
                documentRepository.countCreatedBetween(monthStart, monthEnd),
                userRepository.countActiveByRoleCode("USER")
        );
    }
}
