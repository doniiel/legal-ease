package kz.legeal.ease.backend.exception.auth;

import kz.legeal.ease.backend.exception.BaseException;
import org.springframework.http.HttpStatus;

public class InvalidCredentialsException extends BaseException {
    public InvalidCredentialsException() {
        super("Invalid email or password", HttpStatus.UNAUTHORIZED, "AUTH_001");
    }
}
