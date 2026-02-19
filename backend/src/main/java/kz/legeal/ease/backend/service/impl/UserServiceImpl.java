package kz.legeal.ease.backend.service.impl;

import kz.legeal.ease.backend.domain.User;
import kz.legeal.ease.backend.repository.UserRepository;
import kz.legeal.ease.backend.request.RegisterRequest;
import kz.legeal.ease.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public void activateUser(User user) {
        user.setActive(true);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public User createUser(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        final var fio = request.getFirstName().trim() + " " + request.getMiddleName().trim() + " " + request.getLastName().trim();

        final var user = User.builder()
                .fio(fio)
                .email(request.getEmail())
                .phone(request.getPhone())
                .iin(request.getIin())
                .password(request.getPassword())
                .active(false)
                .deleted(false)
                .gender(request.getGender())
                .lastPasswordModifiedDate(LocalDateTime.now())
                .build();

        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void confirmAccount(String email, String code) {
        final var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User with email not exists"));


    }
}
