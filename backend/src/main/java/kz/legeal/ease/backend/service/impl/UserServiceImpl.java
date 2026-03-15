package kz.legeal.ease.backend.service.impl;

import kz.legeal.ease.backend.domain.User;
import kz.legeal.ease.backend.enums.VerificationType;
import kz.legeal.ease.backend.exception.BusinessRuleException;
import kz.legeal.ease.backend.exception.NotFoundException;
import org.springframework.http.HttpStatus;
import kz.legeal.ease.backend.repository.UserRepository;
import kz.legeal.ease.backend.request.RegisterRequest;
import kz.legeal.ease.backend.service.UserService;
import kz.legeal.ease.backend.service.VerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final VerificationService verificationService;
    private final PasswordEncoder passwordEncoder;
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
        if (userRepository.existsByEmail(request.getEmail()))
            throw new BusinessRuleException(
                    "Email '" + request.getEmail() + "' is already registered", "USER_002", HttpStatus.CONFLICT);

        final var fio = buildFio(request);

        final var user = User.builder()
                .fio(fio)
                .email(request.getEmail())
                .phone(request.getPhone())
                .iin(request.getIin())
                .password(passwordEncoder.encode(request.getPassword()))
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
        verificationService.verify(email, code, VerificationType.REGISTER);
        final var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User", email));

        user.setActive(true);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public User changePassword(String email, String code, String newPassword) {
        verificationService.verify(email, code, VerificationType.RESET_PASSWORD);

        final var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User", email));

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setLastPasswordModifiedDate(LocalDateTime.now());
        userRepository.save(user);
        log.info("Password changed for user email={}", email);
        return user;
    }

    @Override
    @Transactional
    public void blockUser(User user) {
        user.setActive(false);
        userRepository.save(user);
        log.info("User id={} blocked", user.getId());
    }

    @Override
    @Transactional
    public void unblockUser(User user) {
        user.setActive(true);
//        user.setFailedLoginAttempts(0);
//        user.setLockedUntil(null);
        userRepository.save(user);
        log.info("User id={} unblocked", user.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findByIdWithRoles(id)
                .orElseThrow(() -> new NotFoundException("User", id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<User> findAll(Pageable pageable) {
        return userRepository.findAllByDeletedFalse(pageable);
    }

    private String buildFio(RegisterRequest request) {
        final var parts = new ArrayList<String>();
        if (StringUtils.hasText(request.getLastName())) parts.add(request.getLastName().trim());
        if (StringUtils.hasText(request.getFirstName())) parts.add(request.getFirstName().trim());
        if (StringUtils.hasText(request.getMiddleName())) parts.add(request.getMiddleName().trim());
        return String.join(" ", parts);
    }
}
