package kz.legeal.ease.backend.service.impl;

import kz.legeal.ease.backend.domain.VerificationCode;
import kz.legeal.ease.backend.enums.VerificationType;
import kz.legeal.ease.backend.exception.verification.InvalidVerificationCodeException;
import kz.legeal.ease.backend.exception.verification.VerificationCodeExpiredException;
import kz.legeal.ease.backend.repository.VerificationRepository;
import kz.legeal.ease.backend.service.VerificationService;
import kz.legeal.ease.backend.util.CodeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationServiceImpl implements VerificationService {

    private final VerificationRepository repository;

    @Override
    @Transactional
    public String generate(String email, VerificationType type) {
        final var rawCode = CodeUtils.generateVerificationCode();
        final var code = VerificationCode.builder()
                .email(email)
                .code(rawCode)
                .type(type)
                .used(false)
                .expiredAt(LocalDateTime.now().plusMinutes(15))
                .build();

        repository.save(code);
        log.info("Generated {} verification code for email {}", type, email);

        return rawCode;
    }

    @Override
    @Transactional
    public void verify(String email, String activationCode, VerificationType type) {
        final var code = repository
                .findTopByEmailAndTypeAndUsedFalseOrderByCreatedDateDesc(email, type)
                .orElseThrow(VerificationCodeExpiredException::new);

        if (code.isExpired()) throw new VerificationCodeExpiredException();

        if (!code.getCode().equals(activationCode)) throw new InvalidVerificationCodeException();

        code.setUsed(true);
        repository.save(code);
        log.info("{} verification successful for email {}", type, email);
    }
}
