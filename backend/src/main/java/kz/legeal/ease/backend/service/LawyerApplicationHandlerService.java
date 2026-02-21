package kz.legeal.ease.backend.service;

import kz.legeal.ease.backend.dto.LawyerApplicationDto;
import kz.legeal.ease.backend.request.criteria.LawyerRequestSearchCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LawyerApplicationHandlerService {

    LawyerApplicationDto getById(Long id);

    Page<LawyerApplicationDto> getRequestsHistory(Pageable pageable, LawyerRequestSearchCriteria criteria);

    void approveRequest(Long requestId);

    void rejectRequest(Long requestId, String reason);

    void deleteRequest(Long requestId);
}
