package kz.legeal.ease.backend.service;

import kz.legeal.ease.backend.domain.User;
import kz.legeal.ease.backend.request.RegisterRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    void activateUser(User user);

    User createUser(RegisterRequest request);

    void confirmAccount(String email, String code);

    User changePassword(String email, String code, String newPassword);

    void blockUser(User user);

    void unblockUser(User user);

    User findById(Long id);

    Page<User> findAll(Pageable pageable);

}
