package kz.legeal.ease.backend.service;

import kz.legeal.ease.backend.dto.LawyerRequestDto;
import kz.legeal.ease.backend.request.criteria.LawyerRequestSearchCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LawyerRequestService {

    LawyerRequestDto getById(Long id);

    Page<LawyerRequestDto> getRequestsHistory(Pageable pageable, LawyerRequestSearchCriteria criteria);

    void approveRequest(Long requestId);

    void rejectRequest(Long requestId);

    void deleteRequest(Long requestId);
}
