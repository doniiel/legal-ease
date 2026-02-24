package kz.legeal.ease.backend.service.impl;

import kz.legeal.ease.backend.domain.LawyerApplication;
import kz.legeal.ease.backend.domain.User;
import kz.legeal.ease.backend.dto.LawyerApplicationPreviewDto;
import kz.legeal.ease.backend.enums.Status;
import kz.legeal.ease.backend.exception.application.ApplicationAlreadyExistsException;
import kz.legeal.ease.backend.exception.application.ApplicationAlreadyProcessedException;
import kz.legeal.ease.backend.exception.application.ApplicationCannotBeDeletedException;
import kz.legeal.ease.backend.exception.application.ApplicationNotFoundException;
import kz.legeal.ease.backend.mapper.LawyerApplicationMapper;
import kz.legeal.ease.backend.repository.LawyerApplicationRepository;
import kz.legeal.ease.backend.request.LawyerApplicationRequest;
import kz.legeal.ease.backend.service.LawyerApplicationService;
import kz.legeal.ease.backend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LawyerApplicationServiceImpl implements LawyerApplicationService {

    private final LawyerApplicationRepository repository;
    private final LawyerApplicationMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public LawyerApplicationPreviewDto getMyApplication() {
        final var currentUser = SecurityUtils.getCurrentUser()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        final var application = repository.findTopByUserOrderByCreatedDateDesc(currentUser)
                .orElseThrow(() -> new ApplicationNotFoundException(currentUser.getEmail()));

        return mapper.toPreviewDto(application);
    }

    @Override
    @Transactional(readOnly = true)
    public LawyerApplication findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ApplicationNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LawyerApplication> findAll(Specification<LawyerApplication> specification, Pageable pageable) {
        return repository.findAll(specification, pageable);
    }

    @Override
    @Transactional
    public LawyerApplication approveApplication(Long requestId, User amdinuser) {
        final var application = repository.findById(requestId)
                .orElseThrow(() -> new ApplicationNotFoundException(requestId));

        if (!application.isPending()) throw new ApplicationAlreadyProcessedException(requestId);

        application.setStatus(Status.APPROVED);
        application.setReviewer(amdinuser);
        application.setReviewedAt(LocalDateTime.now());
        repository.save(application);

        return application;
    }

    @Override
    @Transactional
    public LawyerApplication rejectApplication(Long requestId, User amdinuser, String reason) {
        final var application = repository.findById(requestId)
                .orElseThrow(() -> new ApplicationNotFoundException(requestId));

        if (!application.isPending()) throw new ApplicationAlreadyProcessedException(requestId);

        application.setStatus(Status.REJECTED);
        application.setReviewer(amdinuser);
        application.setReviewedAt(LocalDateTime.now());
        application.setRejectionReason(reason);
        repository.save(application);

        return application;
    }


    @Override
    @Transactional
    public LawyerApplication deleteApplication(Long requestId, User adminuser) {
        final var application = repository.findById(requestId)
                .orElseThrow(() -> new ApplicationNotFoundException(requestId));

        if (application.isApproved()) throw new ApplicationCannotBeDeletedException(requestId);

        application.setStatus(Status.DELETED);
        application.setReviewer(adminuser);
        application.setReviewedAt(LocalDateTime.now());
        repository.save(application);

        return application;
    }

    @Override
    @Transactional
    public LawyerApplicationPreviewDto submitApplication(LawyerApplicationRequest request) {
        final var currentUser = SecurityUtils.getCurrentUser()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        final var alreadyExists = repository.existsByUserAndStatusIn(
                currentUser, List.of(Status.PENDING, Status.APPROVED)
        );

        if (alreadyExists) throw new ApplicationAlreadyExistsException();

        final var application = LawyerApplication.builder()
                .user(currentUser)
                .licenseNumber(request.getLicenseNum())
                .status(Status.PENDING)
                .submittedAt(LocalDateTime.now())
                .build();

        final var savedApplication = repository.save(application);
        return mapper.toPreviewDto(savedApplication);
    }

    @Override
    @Transactional
    public void archiveActiveApplication(User user, User admin) {
        repository.findTopByUserAndStatusInOrderByCreatedDateDesc(
                user, List.of(Status.PENDING, Status.APPROVED)
        ).ifPresentOrElse(
                application -> {
                    application.setStatus(Status.DELETED);
                    application.setReviewer(admin);
                    application.setReviewedAt(LocalDateTime.now());
                    repository.save(application);
                    log.info("Archived application id={} for user id={}", application.getId(), user.getId());
                },
                () -> log.debug("No active application found for user id={}", user.getId())
        );
    }
}
