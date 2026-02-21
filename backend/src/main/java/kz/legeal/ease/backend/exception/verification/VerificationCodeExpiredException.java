package kz.legeal.ease.backend.exception.verification;

import kz.legeal.ease.backend.exception.BaseException;
import org.springframework.http.HttpStatus;

public class VerificationCodeExpiredException extends BaseException {
    public VerificationCodeExpiredException() {
        super("Verification code has expired. Please request a new one", HttpStatus.BAD_REQUEST, "VERIFY_002");
    }
}