package kz.legeal.ease.backend.service;

import kz.legeal.ease.backend.enums.VerificationType;

public interface VerificationService {

    void generate(String email, VerificationType type);

    void verify(String email, String activationCode, VerificationType type);
}
