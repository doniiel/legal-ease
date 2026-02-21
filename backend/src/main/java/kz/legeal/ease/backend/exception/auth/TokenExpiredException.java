package kz.legeal.ease.backend.exception.auth;

import kz.legeal.ease.backend.exception.BaseException;
import org.springframework.http.HttpStatus;

public class TokenExpiredException extends BaseException {
    public TokenExpiredException() {
        super("Token has expired", HttpStatus.UNAUTHORIZED, "AUTH_004");
    }
}
