package kz.legeal.ease.backend.service.impl;

import kz.legeal.ease.backend.domain.LawyerApplication;
import kz.legeal.ease.backend.dto.LawyerApplicationDto;
import kz.legeal.ease.backend.enums.Role;
import kz.legeal.ease.backend.mapper.LawyerApplicationMapper;
import kz.legeal.ease.backend.repository.specification.GenericSpecificationBuilder;
import kz.legeal.ease.backend.request.criteria.LawyerRequestSearchCriteria;
import kz.legeal.ease.backend.service.*;
import kz.legeal.ease.backend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
public class LawyerApplicationHandlerServiceImpl implements LawyerApplicationHandlerService {

    private final LawyerApplicationMapper mapper;
    private final UserService userService;
    private final UserRoleService userRoleService;
    private final LawyerApplicationService lawyerApplicationService;
    private final NotificationService notificationService;

    @Override
    @Transactional(readOnly = true)
    public LawyerApplicationDto getById(Long id) {
        log.debug("Fetching lawyer request by id={}", id);

        final var application = lawyerApplicationService.findById(id);
        return mapper.toDto(application);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LawyerApplicationDto> getRequestsHistory(Pageable pageable, LawyerRequestSearchCriteria criteria) {
        log.debug("Fetching lawyer requests history with filters: {}", criteria);

        final var specification = new GenericSpecificationBuilder<LawyerApplication>()
                .eq("status", criteria.getStatus())
                .gte("createdDate", criteria.getCreatedFrom())
                .lte("createdDate", criteria.getCreatedTo())
                .gte("reviewedAt", criteria.getReviewedFrom())
                .lte("reviewedAt", criteria.getReviewedTo())
                .like("licenseNumber", criteria.getLicenseNumber())
                .eq("user.id", criteria.getUserId())
                .like("reviewer.fio", criteria.getReviewerFio())
                .build();

        return lawyerApplicationService.findAll(specification, pageable)
                .map(mapper::toDto);
    }

    @Override
    @Transactional
    public void approveRequest(Long requestId) {
        final var admin = SecurityUtils.getCurrentUser()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN));

        log.info("Admin id={} approving lawyer request id={}", admin.getId(), requestId);

        final var application = lawyerApplicationService.approveApplication(requestId, admin);
        final var user = application.getUser();

        userService.activateUser(user);
        userRoleService.assignRole(user, Role.LAWYER.name());

        notificationService.sendLawyerApproved(user.getEmail());

        log.info("Lawyer request id={} approved successfully", requestId);
    }

    @Override
    @Transactional
    public void rejectRequest(Long requestId, String reason) {
        final var admin = SecurityUtils.getCurrentUser()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN));

        log.info("Admin id={} rejecting lawyer request id={}", admin.getId(), requestId);

        final var application = lawyerApplicationService.rejectApplication(requestId, admin, reason);
        final var user = application.getUser();

        notificationService.sendLawyerRejected(user.getEmail(), reason);

        log.info("Lawyer request id={} rejected", requestId);
    }

    @Override
    @Transactional
    public void deleteRequest(Long requestId) {
        final var admin = SecurityUtils.getCurrentUser()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN));

        log.warn("Admin id={} deleting lawyer request id={}", admin.getId(), requestId);

        lawyerApplicationService.deleteApplication(requestId, admin);
    }
}
