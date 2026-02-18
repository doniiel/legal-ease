package kz.legeal.ease.backend.service;

import kz.legeal.ease.backend.domain.LawyerApplication;
import kz.legeal.ease.backend.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface LawyerApplicationService {

    LawyerApplication findById(Long id);

    Page<LawyerApplication> findAll(Specification<LawyerApplication> specification, Pageable pageable);

    LawyerApplication approveApplication(Long requestId, User adminuser);

    LawyerApplication rejectApplication(Long requestId, User adminuser);

    LawyerApplication deleteApplication(Long requestId, User adminuser);
}
