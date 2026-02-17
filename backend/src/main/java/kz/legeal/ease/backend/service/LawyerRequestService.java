package kz.legeal.ease.backend.service;

import kz.legeal.ease.backend.request.LawyerRequestDto;
import kz.legeal.ease.backend.request.criteria.LawyerRequestSearchCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LawyerRequestService {

    Page<LawyerRequestDto> getPendingRequests(Pageable pageable, LawyerRequestSearchCriteria criteria);

    Page<LawyerRequestDto> getRequestsHistory(Pageable pageable, LawyerRequestSearchCriteria criteria);

    void approveRequest(Long requestId);

    void rejectRequest(Long requestId);

    void deleteRequest(Long requestId);
}
