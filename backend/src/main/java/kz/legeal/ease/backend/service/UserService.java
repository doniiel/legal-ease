package kz.legeal.ease.backend.service;

import kz.legeal.ease.backend.domain.User;
import kz.legeal.ease.backend.request.RegisterRequest;
import kz.legeal.ease.backend.request.VerificationRequest;

public interface UserService {

    void activateUser(User user);

    User createUser(RegisterRequest request);

    void confirmAccount(String email, String code);
}
