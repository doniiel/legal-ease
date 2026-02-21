package kz.legeal.ease.backend.exception.verification;

import kz.legeal.ease.backend.exception.BaseException;
import org.springframework.http.HttpStatus;

public class TooManyVerificationAttemptsException extends BaseException {
    public TooManyVerificationAttemptsException() {
        super("Too many attempts. Please request a new verification code", HttpStatus.TOO_MANY_REQUESTS, "VERIFY_004");
    }
}