package kz.legeal.ease.backend.service;

import kz.legeal.ease.backend.domain.LawyerApplication;
import kz.legeal.ease.backend.domain.User;
import kz.legeal.ease.backend.dto.LawyerApplicationPreviewDto;
import kz.legeal.ease.backend.request.LawyerApplicationRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface LawyerApplicationService {

    LawyerApplicationPreviewDto getMyApplication();

    LawyerApplication findById(Long id);

    Page<LawyerApplication> findAll(Specification<LawyerApplication> specification, Pageable pageable);

    LawyerApplication approveApplication(Long requestId, User adminuser);

    LawyerApplication rejectApplication(Long requestId, User adminuser, String reason);

    LawyerApplication deleteApplication(Long requestId, User adminuser);

    LawyerApplicationPreviewDto submitApplication(LawyerApplicationRequest request);
}
