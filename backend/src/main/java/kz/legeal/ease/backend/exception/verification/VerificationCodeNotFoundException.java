package kz.legeal.ease.backend.exception.verification;

import kz.legeal.ease.backend.exception.BaseException;
import org.springframework.http.HttpStatus;

public class VerificationCodeNotFoundException extends BaseException {
    public VerificationCodeNotFoundException() {
        super("Verification code not found or already used", HttpStatus.BAD_REQUEST, "VERIFY_001");
    }
}
