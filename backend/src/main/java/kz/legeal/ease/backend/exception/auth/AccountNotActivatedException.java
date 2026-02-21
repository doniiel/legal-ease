package kz.legeal.ease.backend.exception.auth;

import kz.legeal.ease.backend.exception.BaseException;
import org.springframework.http.HttpStatus;

public class AccountNotActivatedException extends BaseException {
    public AccountNotActivatedException(String email) {
        super("Account with email '" + email + "' is not activated", HttpStatus.FORBIDDEN, "AUTH_002");
    }
}