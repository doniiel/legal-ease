package kz.legeal.ease.backend.exception.auth;

import kz.legeal.ease.backend.exception.BaseException;
import org.springframework.http.HttpStatus;

public class TokenRevokedException extends BaseException {
    public TokenRevokedException() {
        super("Token has been revoked", HttpStatus.UNAUTHORIZED, "AUTH_006");
    }
}
