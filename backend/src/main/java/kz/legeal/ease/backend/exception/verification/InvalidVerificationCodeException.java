package kz.legeal.ease.backend.exception.verification;

import kz.legeal.ease.backend.exception.BaseException;
import org.springframework.http.HttpStatus;

public class InvalidVerificationCodeException extends BaseException {
    public InvalidVerificationCodeException() {
        super("Invalid verification code", HttpStatus.BAD_REQUEST, "VERIFY_003");
    }
}
