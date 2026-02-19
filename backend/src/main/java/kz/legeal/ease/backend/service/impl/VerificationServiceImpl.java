package kz.legeal.ease.backend.service.impl;

import kz.legeal.ease.backend.domain.VerificationCode;
import kz.legeal.ease.backend.enums.VerificationType;
import kz.legeal.ease.backend.repository.VerificationRepository;
import kz.legeal.ease.backend.service.VerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationServiceImpl implements VerificationService {

    private final VerificationRepository repository;

    @Override
    public void generate(String email, VerificationType type) {
        final var code = generateCode();
        final var code = VerificationCode.builder()
                .email(email)
                .code(code)
                .build();
    }

    @Override
    @Transactional
    public void verify(String email, String activationCode, VerificationType type) {
        final var code = repository.findTopByEmailAndTypeAndUsedFalseOrderByCreatedDateDesc(email, VerificationType.REGISTER)
                .orElseThrow(() -> new IllegalArgumentException("Code not found"));

        if (code.isExpired()) {
            throw new IllegalArgumentException("Code expired");
        }

        if (!code.getCode().equals(email)) {
            throw new IllegalArgumentException("Invalid code");
        }

        code.setUsed(true);

        repository.save(code);
    }
}
