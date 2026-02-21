package kz.legeal.ease.backend.exception.user;

import kz.legeal.ease.backend.exception.BaseException;
import org.springframework.http.HttpStatus;

public class EmailAlreadyExistsException extends BaseException {
    public EmailAlreadyExistsException(String email) {
        super("Email '" + email + "' is already registered", HttpStatus.CONFLICT, "USER_002");
    }
}
