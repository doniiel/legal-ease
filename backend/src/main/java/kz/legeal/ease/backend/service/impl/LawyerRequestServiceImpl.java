package kz.legeal.ease.backend.service.impl;

import kz.legeal.ease.backend.enums.Role;
import kz.legeal.ease.backend.request.LawyerRequestDto;
import kz.legeal.ease.backend.request.criteria.LawyerRequestSearchCriteria;
import kz.legeal.ease.backend.service.LawyerApplicationService;
import kz.legeal.ease.backend.service.LawyerRequestService;
import kz.legeal.ease.backend.service.UserRoleService;
import kz.legeal.ease.backend.service.UserService;
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
public class LawyerRequestServiceImpl implements LawyerRequestService {

    private final UserService userService;
    private final UserRoleService userRoleService;
    private final LawyerApplicationService lawyerApplicationService;

    @Override
    @Transactional(readOnly = true)
    public Page<LawyerRequestDto> getPendingRequests(Pageable pageable, LawyerRequestSearchCriteria criteria) {
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LawyerRequestDto> getRequestsHistory(Pageable pageable, LawyerRequestSearchCriteria criteria) {
        return null;
    }

    @Override
    @Transactional
    public void approveRequest(Long requestId) {
        final var admin = SecurityUtils.getCurrentUser()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN));
        final var application = lawyerApplicationService.approveApplication(requestId, admin);
        final var user = application.getUser();

        userService.activateUser(user);
        userRoleService.assignRole(user, Role.LAWYER.name());

        log.info("Lawyer request {} approved successfully", requestId);
    }

    @Override
    @Transactional
    public void rejectRequest(Long requestId) {
        final var admin = SecurityUtils.getCurrentUser()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN));
        lawyerApplicationService.approveApplication(requestId, admin);
    }

    @Override
    @Transactional
    public void deleteRequest(Long requestId) {
        final var admin = SecurityUtils.getCurrentUser()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN));
        lawyerApplicationService.deleteApplication(requestId, admin);
    }
}
