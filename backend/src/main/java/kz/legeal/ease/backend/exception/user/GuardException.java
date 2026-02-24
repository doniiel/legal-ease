package kz.legeal.ease.backend.exception.user;

import kz.legeal.ease.backend.exception.BaseException;
import org.springframework.http.HttpStatus;

public class GuardException extends BaseException {
    public GuardException(String message) {
        super(message, HttpStatus.NOT_MODIFIED, "USER_004");
    }
}
