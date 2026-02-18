package kz.legeal.ease.backend.service.impl;

import kz.legeal.ease.backend.domain.LawyerApplication;
import kz.legeal.ease.backend.domain.User;
import kz.legeal.ease.backend.enums.Status;
import kz.legeal.ease.backend.repository.LawyerApplicationRepository;
import kz.legeal.ease.backend.service.LawyerApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class LawyerApplicationServiceImpl implements LawyerApplicationService {

    private final LawyerApplicationRepository repository;

    @Override
    public LawyerApplication findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));
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
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));

        if (!application.isPending()) throw new IllegalStateException("Request already processed");

        application.setStatus(Status.APPROVED);
        application.setReviewer(amdinuser);
        application.setReviewedAt(LocalDateTime.now());
        repository.save(application);

        return application;
    }

    @Override
    @Transactional
    public LawyerApplication rejectApplication(Long requestId, User amdinuser) {
        final var application = repository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));

        if (!application.isPending()) throw new IllegalStateException("Request already processed");

        application.setStatus(Status.REJECTED);
        application.setReviewer(amdinuser);
        application.setReviewedAt(LocalDateTime.now());
        repository.save(application);

        return application;
    }


    @Override
    @Transactional
    public LawyerApplication deleteApplication(Long requestId, User adminuser) {
        final var application = repository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));

        if (application.isApproved()) throw new IllegalStateException("Cannot delete approved lawyer request");

        application.setStatus(Status.DELETED);
        application.setReviewer(adminuser);
        application.setReviewedAt(LocalDateTime.now());
        repository.save(application);

        return application;
    }
}
