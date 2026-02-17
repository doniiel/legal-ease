package kz.legeal.ease.backend.service.impl;

import kz.legeal.ease.backend.domain.User;
import kz.legeal.ease.backend.repository.UserRepository;
import kz.legeal.ease.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public void activateUser(User user) {
        user.setActive(true);
        userRepository.save(user);
    }
}
