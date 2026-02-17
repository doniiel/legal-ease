package kz.legeal.ease.backend.service;

import kz.legeal.ease.backend.domain.LawyerApplication;
import kz.legeal.ease.backend.domain.User;

public interface LawyerApplicationService {

    LawyerApplication approveApplication(Long requestId, User adminuser);

    LawyerApplication rejectApplication(Long requestId, User adminuser);

    LawyerApplication deleteApplication(Long requestId, User adminuser);
}
